package com.googlecode.npackdweb.pv;

import com.google.common.primitives.Bytes;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
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
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

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
        Package pa = NWUtils.dsCache.getPackage(package_, false);
        Page page;
        if (!pa.isCurrentUserPermittedToModify()) {
            page =
                    new MessagePage(
                            "You do not have permission to modify this package");
        } else {
            PackageVersion p = NWUtils.dsCache.getPackageVersion(
                    package_ + "@" + version);
            PackageVersion oldp = p.copy();
            String err = recognize(p);
            if (err != null) {
                page = new MessagePage(err);
            } else {
                p.addTag("untested");
                NWUtils.dsCache.savePackageVersion(oldp, p, true, true);
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

        // in seconds
        final int timeout = 20;

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        CloseableHttpClient httpclient = HttpClientBuilder.create().
                setDefaultRequestConfig(config).build();

        HttpGet ht = new HttpGet(pv.url);
        ht.setHeader("User-Agent",
                "NpackdWeb/1 (compatible; MSIE 9.0)");
        ht.setHeader("Range", "bytes=" + startPosition +
                "-" + (startPosition + segment - 1));

        byte[] content = null;

        // The underlying HTTP connection is still held by the response object
        // to allow the response content to be streamed directly from the network socket.
        // In order to ensure correct deallocation of system resources
        // the user MUST call CloseableHttpResponse#close() from a finally clause.
        // Please note that if response content is not fully consumed the underlying
        // connection cannot be safely re-used and will be shut down and discarded
        // by the connection manager.
        try {
            CloseableHttpResponse r = httpclient.execute(ht);
            try {
                HttpEntity e = r.getEntity();

                if (r.getStatusLine().getStatusCode() == 416) {
                    if (startPosition == 0) {
                        throw new IOException(
                                "Empty response with HTTP error code 416");
                    }
                }

                content = EntityUtils.toByteArray(e);

                if (r.getStatusLine().getStatusCode() != 206 && r.
                        getStatusLine().getStatusCode() != 200) {
                    throw new IOException("HTTP response code: " +
                            r.getStatusLine().getStatusCode());
                }

                if (content.length < segment) {
                    completeDownload = true;

                    MessageDigest crypt = MessageDigest.getInstance("SHA-256");
                    crypt.update(content);
                    pv.sha1 = NWUtils.byteArrayToHexString(crypt.digest());
                }
            } finally {
                r.close();
            }
        } catch (IOException | NoSuchAlgorithmException ex) {
            NWUtils.LOG.log(Level.WARNING, "Error loading file", ex);
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
                        "\"%ncl%\\ncl.exe\" remove-scp --title " +
                        "\"/%npackd_package_name% %npackd_package_version%/i\"> " +
                        ".Npackd\\Output.txt && type .Npackd\\Output.txt\r\n");

                pv.addDependency(
                        "com.googlecode.windows-package-manager.NpackdCL",
                        "[1.25, 2)",
                        "ncl");
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
                // nothing
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
