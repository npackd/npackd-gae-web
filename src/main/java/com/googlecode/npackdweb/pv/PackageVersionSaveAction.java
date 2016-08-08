package com.googlecode.npackdweb.pv;

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
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Save or create a package.
 */
public class PackageVersionSaveAction extends Action {

    /**
     * -
     */
    public PackageVersionSaveAction() {
        super("^/package-version/save$", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Page page;
        PackageVersionPage pvp = new PackageVersionPage(null, false);
        pvp.fill(req);
        String error = pvp.validate();
        if (error == null) {
            String package_ = pvp.getPackageName();
            final String version = pvp.getVersion();
            Objectify ofy = DefaultServlet.getObjectify();
            Package pa = ofy.get(new Key<Package>(Package.class, package_));
            if (!pa.isCurrentUserPermittedToModify()) {
                page =
                        new MessagePage(
                                "You do not have permission to modify this package");
            } else {
                PackageVersion p =
                        ofy.find(new Key<PackageVersion>(PackageVersion.class,
                                package_ + "@" + version));
                PackageVersion old;
                if (p == null) {
                    old = null;
                    pvp.normalizeVersion();
                    p = new PackageVersion(package_, version);
                } else {
                    old = p.copy();
                }

                pvp.fillObject(p);

                NWUtils.savePackageVersion(ofy, old, p, true, true);

                resp.sendRedirect("/p/" + p.package_ + "/" + p.version);
                page = null;
            }
        } else {
            pvp.setErrorMessage(error);
            page = pvp;
        }
        return page;
    }
}
