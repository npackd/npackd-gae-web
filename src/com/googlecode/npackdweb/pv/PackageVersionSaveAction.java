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
 * Save or create a package.
 */
public class PackageVersionSaveAction extends Action {
    /**
     * -
     */
    public PackageVersionSaveAction() {
        super("^/package-version/save$", ActionSecurityType.EDITOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Page page;
        PackageVersionPage pvp = new PackageVersionPage(null, false);
        pvp.fillForm(req);
        String error = pvp.validate();
        if (error == null) {
            String package_ = pvp.getPackageName();
            final String version = pvp.getVersion();
            Objectify ofy = DefaultServlet.getObjectify();
            Package pa = ofy.get(new Key<Package>(Package.class, package_));
            if (!pa.isCurrentUserPermittedToModify())
                page = new MessagePage(
                        "You do not have permission to modify this package");
            else {
                PackageVersion p = ofy.find(new Key<PackageVersion>(
                        PackageVersion.class, package_ + "@" + version));
                if (p == null) {
                    pvp.normalizeVersion();
                    p = new PackageVersion();
                }
                PackageVersion old = p.copy();
                pvp.fillObject(p);
                if (!p.url.equals(old.url) || !p.sha1.equals(old.sha1)) {
                    if (!PackageVersion.DONT_CHECK_THIS_DOWNLOAD
                            .equals(p.downloadCheckError)) {
                        p.downloadCheckAt = null;
                        p.downloadCheckError = null;
                    }
                }

                if (NWUtils.isAdminLoggedIn())
                    p.reviewed = true;

                NWUtils.savePackageVersion(ofy, p);

                resp.sendRedirect("/p/" + p.package_);
                page = null;
            }
        } else {
            pvp.setErrorMessage(error);
            page = pvp;
        }
        return page;
    }
}
