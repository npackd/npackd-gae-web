package com.googlecode.npackdweb.pv;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Version;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Start a copy of a package version.
 */
public class CopyPackageVersionConfirmedAction extends Action {

    /**
     * -
     */
    public CopyPackageVersionConfirmedAction() {
        super("^/package-version/copy-confirmed$", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getParameter("name").trim();
        String version = req.getParameter("version").trim();

        String err = null;
        try {
            Version version_ = Version.parse(version);
            version_.normalize();
            version = version_.toString();
        } catch (NumberFormatException e) {
            err = "Error parsing the version number: " + e.getMessage();
        }

        PackageVersion p = NWUtils.dsCache.getPackageVersion(
                name);
        Package r = NWUtils.dsCache.getPackage(p.package_, false);
        Page page;
        if (!r.isCurrentUserPermittedToModify()) {
            page =
                    new MessagePage(
                            "You do not have permission to modify this package");
        } else {
            PackageVersion copyFound = NWUtils.dsCache.getPackageVersion(
                    p.package_ + "@" + version);
            if (copyFound != null) {
                err =
                        "This version already exists: " + p.package_ + " " +
                        version;
            }

            if (err != null) {
                page =
                        new MessagePage(err);
            } else {
                PackageVersion copy = p.copy();
                copy.name = copy.package_ + "@" + version;
                copy.version = version;
                copy.createdAt = NWUtils.newDate();
                UserService us = UserServiceFactory.getUserService();
                copy.createdBy = us.getCurrentUser();
                copy.addTag("untested");

                NWUtils.dsCache.savePackageVersion(null, copy, true, true);
                resp.sendRedirect("/p/" + copy.package_ + "/" + copy.version);
                page = null;
            }
        }
        return page;
    }
}
