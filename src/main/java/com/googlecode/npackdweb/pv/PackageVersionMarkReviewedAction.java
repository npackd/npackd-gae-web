package com.googlecode.npackdweb.pv;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.db.Version;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Marks a package version as reviewed.
 */
public class PackageVersionMarkReviewedAction extends Action {

    /**
     * -
     */
    public PackageVersionMarkReviewedAction() {
        super("^/package-version/mark-reviewed",
                ActionSecurityType.ADMINISTRATOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String package_ = req.getParameter("package");
        String err = Package.checkName(package_);

        String version = req.getParameter("version");
        Version v = null;
        if (err == null) {
            try {
                v = Version.parse(version);
                v.normalize();
            } catch (NumberFormatException e) {
                err = e.getMessage();
            }
        }

        PackageVersion pv = null;
        if (err == null) {
            pv = NWUtils.dsCache.getPackageVersion(package_ + "@" + v.
                    toString());
            if (pv == null) {
                err = "Cannot find the package version";
            }
        }

        Page page;
        if (err == null) {
            NWUtils.dsCache.savePackageVersion(pv, pv, false, true);
            resp.sendRedirect("/p/" + pv.package_ + "/" + pv.version);
            page = null;
        } else {
            page =
                    new MessagePage(
                            "You do not have permission to modify this package");
        }

        return page;
    }
}
