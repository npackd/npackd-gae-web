package com.googlecode.npackdweb.pv;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.ResponseTooLargeException;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.primitives.Bytes;
import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Recognize the binary type and assign the scripts accordingly.
 */
public class PackageVersionRecognizeAction extends Action {

    private static enum BinaryType {

        NSIS, SEVENZIP, ZIP, INNOSETUP, MSI, OTHER_EXE, UNKNOWN
    };

    /**
     * -
     */
    public PackageVersionRecognizeAction() {
        super("^/package-version/recognize$", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String package_ = req.getParameter("package");
        String version = req.getParameter("version");
        Objectify ofy = DefaultServlet.getObjectify();
        Package pa = ofy.get(new Key<Package>(Package.class, package_));
        Page page;
        if (!pa.isCurrentUserPermittedToModify()) {
            page =
                    new MessagePage(
                            "You do not have permission to modify this package");
        } else {
            PackageVersion p =
                    ofy.get(new Key<PackageVersion>(PackageVersion.class,
                                    package_ + "@" + version));
            PackageVersion oldp = p.copy();
            String err = recognize(p);
            if (err != null) {
                page = new MessagePage(err);
            } else {
                p.addTag("untested");
                NWUtils.savePackageVersion(ofy, oldp, p, true, true);
                resp.sendRedirect("/p/" + p.package_ + "/" + p.version);
                page = null;
            }
        }
        return page;
    }

    private String recognize(PackageVersion pv) {
        String err = null;

        URL url;
        try {
            url = new URL(pv.url);
        } catch (MalformedURLException e1) {
            err = e1.getMessage();
            return err;
        }

        String path = url.getPath();

        int p = path.lastIndexOf('/');
        String file;
        if (p >= 0) {
            file = path.substring(p + 1);
        } else {
            file = "";
        }

        BinaryType t = BinaryType.UNKNOWN;

        String fileExt = "";
        if (file.length() > 0) {
            p = file.lastIndexOf('.');

            // String fileName;
            if (file.length() - p <= 4) {
                // fileName = file.substring(0, p);
                fileExt = file.substring(p + 1);
            } else {
                // fileName = file;
                fileExt = "";
            }

            /*
             * if (fileName.length() > 0) ret.package_ = fileName;
             */
        }

        long startPosition = 0;

        // limit 32 MiB:
        // https://cloud.google.com/appengine/quotas#UrlFetch
        long segment = 20 * 1024 * 1024;

        // true if the complete file was downloaded, false if only the first
        // part of the file was downloaded
        boolean completeDownload = false;

        URLFetchService s = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse r;
        byte[] content = null;
        try {
            HTTPRequest ht = new HTTPRequest(url);
            ht.setHeader(new HTTPHeader("User-Agent",
                    "NpackdWeb/1 (compatible; MSIE 9.0)"));
            ht.getFetchOptions().setDeadline(20.0);
            ht.setHeader(new HTTPHeader("Range", "bytes=" + startPosition +
                    "-" + (startPosition + segment - 1)));
            r = s.fetch(ht);
            if (r.getResponseCode() == 416) {
                if (startPosition == 0) {
                    throw new IOException(
                            "Empty response with HTTP error code 416");
                }
            }

            content = r.getContent();
            if (r.getResponseCode() != 206 && r.getResponseCode() != 200) {
                throw new IOException("HTTP response code: " +
                        r.getResponseCode());
            }

            if (content.length < segment) {
                completeDownload = true;

                MessageDigest crypt = MessageDigest.getInstance("SHA-1");
                crypt.update(content);
                pv.sha1 = NWUtils.byteArrayToHexString(crypt.digest());
            }

        } catch (IOException e) {
            NWUtils.LOG.log(Level.WARNING, e.getMessage(), e);
        } catch (ResponseTooLargeException e) {
            NWUtils.LOG.log(Level.WARNING, e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e.getMessage());
        }

        byte[] contentLowerCase = null;
        if (content != null) {
            contentLowerCase = new byte[content.length];

            // to lower case
            for (int i = 0; i < content.length; i++) {
                contentLowerCase[i] = (byte) (content[i] | 32);
            }
        }

        switch (fileExt.toLowerCase()) {
            case "msi":
                t = BinaryType.MSI;
                break;
            case "exe":
                // NSIS is the most often used installer builder
                if (contentLowerCase != null) {
                    if (Bytes.
                            indexOf(contentLowerCase, "nsis.sf.net".getBytes()) >=
                            0 ||
                            Bytes.indexOf(contentLowerCase,
                                    "nullsoft.nsis".getBytes()) >= 0) {
                        t = BinaryType.NSIS;
                    } else if (Bytes.indexOf(contentLowerCase,
                            "inno setup".getBytes()) >= 0) {
                        t = BinaryType.INNOSETUP;
                    } else {
                        t = BinaryType.OTHER_EXE;
                    }
                } else {
                    t = BinaryType.OTHER_EXE;
                }
                break;
            case "zip":
                t = BinaryType.ZIP;
                break;
            case "7z":
                t = BinaryType.SEVENZIP;
                break;
            default:
                if (content != null) {
                    if (content.length > 2 && content[0] == 'P' &&
                            content[1] == 'K') {
                        t = BinaryType.ZIP;
                    } else if (content.length > 2 && content[0] == 'M' &&
                            content[1] == 'Z') {
                        if (Bytes.indexOf(contentLowerCase,
                                "nsis.sf.net".getBytes()) >= 0) {
                            t = BinaryType.NSIS;
                        } else if (Bytes.indexOf(contentLowerCase,
                                "inno setup".getBytes()) >= 0) {
                            t = BinaryType.INNOSETUP;
                        } else {
                            t = BinaryType.OTHER_EXE;
                        }
                    } else if (content.length > 2 && content[0] == '7' &&
                            content[1] == 'Z') {
                        t = BinaryType.SEVENZIP;
                    }
                }
        }

        switch (t) {
            // NSIS, SEVENZIP, ZIP, INNOSETUP, MSI, OTHER
            case NSIS:
                pv.oneFile = true;
                pv.addFile(
                        ".Npackd\\Install.bat",
                        "\"%npackd_package_binary%\" /S /D=%CD% && del /f /q \"%npackd_package_binary%\"\r\n");
                pv.addFile(".Npackd\\Uninstall.bat",
                        "uninstall.exe /S _?=%CD%\r\n");
                break;
            case SEVENZIP:
                pv.oneFile = true;
                pv.addFile(
                        ".Npackd\\Install.bat",
                        "\"%sevenzipa%\\7za.exe\" x \"%npackd_package_binary%\" > .Npackd\\Output.txt && type .Npackd\\Output.txt && del /f /q \"%setup%\"\r\n");
                pv.addDependency("org.7-zip.SevenZIPA", "[9.20, 20)",
                        "sevenzipa");
                break;
            case ZIP:
                pv.oneFile = false;
                if (content != null && completeDownload) {
                    try {
                        String d = getCommonZIPDir(content);
                        if (d.length() != 0) {
                            pv.addFile(
                                    ".Npackd\\Install.bat",
                                    "for /f \"delims=\" %%x in ('dir /b " +
                                    d +
                                    "*') do set name=%%x\r\n" +
                                    "cd \"%name%\"\r\n" +
                                    "for /f \"delims=\" %%a in ('dir /b') do (\r\n" +
                                    "  move \"%%a\" ..\r\n" + ")\r\n" +
                                    "cd ..\r\n" + "rmdir \"%name%\"\r\n");
                        }
                    } catch (IOException e1) {
                        NWUtils.LOG.log(Level.WARNING, e1.getMessage(), e1);
                    }
                }
                break;
            case INNOSETUP:
                pv.oneFile = true;
                pv.addFile(".Npackd\\Install.bat",
                        "\"%nih%\\InstallInnoSetup.bat\"\r\n");
                pv.addFile(".Npackd\\Uninstall.bat",
                        "\"%nih%\\UninstallInnoSetup.bat\" unins000.exe");
                pv.addDependency(
                        "com.googlecode.windows-package-manager.NpackdInstallerHelper",
                        "[1.3, 2)", "nih");
                break;
            case MSI:
                pv.oneFile = true;
                pv.addFile(".Npackd\\Install.bat",
                        "call \"%nih%\\InstallMSI.bat\" INSTALLDIR yes\r\n");
                pv.addDependency(
                        "com.googlecode.windows-package-manager.NpackdInstallerHelper",
                        "[1.1, 2)", "nih");
                break;
            case OTHER_EXE:
                pv.oneFile = true;
                pv.addFile(".Npackd\\Install.bat",
                        "\"%npackd_package_binary%\" && del /f /q \"%npackd_package_binary%\"\r\n");
                pv.addFile(".Npackd\\Uninstall.bat",
                        "\"%myun%\\myuninst.exe\" /uninstall \"" + pv.package_ +
                        "\"\r\n");

                pv.addDependency("net.nirsoft.MyUninstaller", "[1.74, 2)",
                        "myun");
                break;
            default:
                pv.oneFile = true;
                break;
        }

        if (!pv.tags.contains("stable")) {
            pv.tags.add("stable");
        }

        return err;
    }

    private String getCommonZIPDir(byte[] content) throws IOException {
        ZipInputStream zis =
                new ZipInputStream(new ByteArrayInputStream(content));
        ZipEntry e;

        String commonPrefix = null;
        while ((e = zis.getNextEntry()) != null) {
            String n = e.getName();
            if (commonPrefix == null) {
                commonPrefix = n;
            } else if (commonPrefix.length() == 0) {
                break;
            } else if (n.indexOf(commonPrefix) == 0) {
                continue;
            } else {
                char[] commonPrefix_ = commonPrefix.toCharArray();
                char[] n_ = n.toCharArray();
                int count = 0;
                for (int i = 0; i < Math.min(commonPrefix_.length, n_.length);
                        i++) {
                    if (commonPrefix_[i] == n_[i]) {
                        count++;
                    } else {
                        break;
                    }
                }
                commonPrefix = commonPrefix.substring(0, count);
            }
        }

        if (commonPrefix == null) {
            commonPrefix = "";
        }

        if (commonPrefix.startsWith("/")) {
            commonPrefix = commonPrefix.substring(1);
        }

        int p = commonPrefix.indexOf('/');
        if (p >= 0 && p == commonPrefix.length() - 1) {
            commonPrefix = commonPrefix.substring(0, p);
        } else {
            commonPrefix = "";
        }

        return commonPrefix;
    }
}
