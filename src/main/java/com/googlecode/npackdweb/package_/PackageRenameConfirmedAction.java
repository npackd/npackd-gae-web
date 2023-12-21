package com.googlecode.npackdweb.package_;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Rename a package.
 */
public class PackageRenameConfirmedAction extends Action {

    /**
     * -
     */
    public PackageRenameConfirmedAction() {
        super("^/package/rename-confirmed", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getParameter("name").trim();
        String newName = req.getParameter("new-name").trim();

        String err = Package.checkName(name);

        if (err == null) {
            err = Package.checkName(newName);
        }

        Package p = null;
        Package copy;

        if (err == null) {
            p = NWUtils.dsCache.getPackage(name, true);
            if (p == null) {
                err = "Unknown package: " + name;
            }
        }

        if (err == null) {
            copy = NWUtils.dsCache.getPackage(newName, true);
            if (copy != null) {
                err = "Package already exists: " + newName;
            }
        }

        assert p != null;

        Page page;

        if (err == null) {
            if (!p.isCurrentUserPermittedToModify()) {
                err = "You do not have permission to modify this package.";
            }
        }

        if (err != null) {
            page = new MessagePage(err);
        } else {
            copy = p.copy();
            copy.name = newName;

            NWUtils.dsCache.savePackage(null, copy, true);

            // store the versions
            List<PackageVersion> pvs = NWUtils.dsCache.
                    getPackageVersions(p.name);
            for (PackageVersion pv : pvs) {
                pv.package_ = copy.name;
                pv.name = pv.package_ + "@" + pv.version;
                NWUtils.dsCache.savePackageVersion(null, pv, false, false);
            }

            NWUtils.dsCache.deletePackage(p.name);

            UserService us = UserServiceFactory.getUserService();
            User u = us.getCurrentUser();

            if (!NWUtils.isEmailEqual(u.getEmail(),
                    p.lastModifiedBy.getEmail())) {
                NWUtils.sendMailTo(
                        "The package \"" + name + "\" was renamed to \"" +
                                newName +
                                "\" by \"" + u.getEmail(),
                        p.lastModifiedBy.getEmail());
            }

            resp.sendRedirect("/p/" + copy.name);
            page = null;
        }
        return page;
    }
}
