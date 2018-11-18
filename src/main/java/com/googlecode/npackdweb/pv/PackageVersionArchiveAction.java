package com.googlecode.npackdweb.pv;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        String version = req.getParameter("version");
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
            if (f.length() == 0) {
                throw new IOException("Empty file name");
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
