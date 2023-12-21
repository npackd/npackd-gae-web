package com.googlecode.npackdweb.pv;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.db.Version;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

/**
 * Uploads the binary to archive.org.
 */
public class PackageVersionArchiveAction extends Action {

    /**
     * -
     */
    public PackageVersionArchiveAction() {
        super("^/package-version/archive$",
                ActionSecurityType.ADMINISTRATOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String package_ = req.getParameter("package");
        String err = Package.checkName(package_);
        if (err != null) {
            throw new IOException(err);
        }

        String version = req.getParameter("version");

        // NumberFormatException in case of an error
        Version v = Version.parse(version);
        v.normalize();

        PackageVersion p = NWUtils.dsCache.getPackageVersion(
                package_ + "@" + version);
        PackageVersion oldp = p.copy();
        Page page;
        try {
            String f = new URL(p.url).getPath();
            int pos = f.lastIndexOf('/');
            if (pos >= 0) {
                f = f.substring(pos + 1);
            }
            if (f.isEmpty()) {
                throw new IOException("Empty file name");
            }

            if (!f.contains(version)) {
                String fileExt;
                String fileName;

                pos = f.lastIndexOf('.');

                // String fileName;
                if ((pos > 0) && (f.length() - pos <= 4)) {
                    fileName = f.substring(0, pos);
                    fileExt = f.substring(pos);
                } else {
                    fileName = f;
                    fileExt = "";
                }

                f = fileName + '-' + v + fileExt;

                /*
                 * if (fileName.length() > 0) ret.package_ = fileName;
                 */
            }

            NWUtils.archive(p.url,
                    "http://s3.us.archive.org/npackd/" + package_ + "/" + f,
                    NWUtils.dsCache.getSetting("ArchiveAccessKey", ""),
                    NWUtils.dsCache.getSetting("ArchiveSecretKey", ""));

            p.url = "https://archive.org/download/npackd/" + package_ + "/" + f;
            NWUtils.dsCache.savePackageVersion(oldp, p, true, true);
            resp.sendRedirect("/p/" + p.package_ + "/" + p.version);
            page = null;
        } catch (IOException e) {
            page =
                    new MessagePage("Upload to archive.org failed: " +
                            e.getMessage());
        }
        return page;
    }
}
