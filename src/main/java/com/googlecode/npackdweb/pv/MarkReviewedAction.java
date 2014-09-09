package com.googlecode.npackdweb.pv;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

/**
 * Marks a package version as reviewed.
 */
public class MarkReviewedAction extends Action {
    /**
     * -
     */
    public MarkReviewedAction() {
        super("^/package-version/mark-reviewed$",
                ActionSecurityType.ADMINISTRATOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String package_ = req.getParameter("package");
        String version = req.getParameter("version");
        Objectify ofy = DefaultServlet.getObjectify();
        Package pa = ofy.get(new Key<Package>(Package.class, package_));
        Page page;
        if (!pa.isCurrentUserPermittedToModify())
            page = new MessagePage(
                    "You do not have permission to modify this package");
        else {
            PackageVersion p = ofy.get(new Key<PackageVersion>(
                    PackageVersion.class, package_ + "@" + version));
            p.reviewed = true;
            NWUtils.savePackageVersion(ofy, p, true);
            page = new PackageVersionPage(p, false);
        }
        return page;
    }
}
