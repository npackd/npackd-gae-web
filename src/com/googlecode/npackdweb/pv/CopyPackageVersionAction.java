package com.googlecode.npackdweb.pv;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Start a copy of a package version.
 */
public class CopyPackageVersionAction extends Action {
    /**
     * -
     */
    public CopyPackageVersionAction() {
        super("^/package-version/copy$", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String package_ = req.getParameter("package");
        String version = req.getParameter("version");
        Objectify ofy = DefaultServlet.getObjectify();
        Package r = ofy.find(new Key<Package>(Package.class, package_));
        Page page;
        if (!r.isCurrentUserPermittedToModify())
            page = new MessagePage(
                    "You do not have permission to modify this package");
        else {
            PackageVersion p = ofy.get(new Key<PackageVersion>(
                    PackageVersion.class, package_ + "@" + version));
            page = new CopyPackageVersionPage(p);
        }
        return page;
    }
}
