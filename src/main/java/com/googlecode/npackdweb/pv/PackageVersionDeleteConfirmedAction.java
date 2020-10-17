package com.googlecode.npackdweb.pv;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Delete a package version.
 */
public class PackageVersionDeleteConfirmedAction extends Action {

    /**
     * -
     */
    public PackageVersionDeleteConfirmedAction() {
        super("^/package-version/delete-confirmed$",
                ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getParameter("name");
        List<String> nameParts = NWUtils.split(name, '@');
        PackageVersion p = NWUtils.dsCache.getPackageVersion(nameParts.get(0), nameParts.get(1));
        Package pa = NWUtils.dsCache.getPackage(p.package_, false);
        Page page;
        if (!pa.isCurrentUserPermittedToModify()) {
            page = new MessagePage(
                    "You do not have permission to modify this package");
        } else {
            NWUtils.dsCache.deletePackageVersion(p);
            NWUtils.dsCache.incDataVersion();
            resp.sendRedirect("/p/" + p.package_);
            page = null;
        }
        return page;
    }
}
