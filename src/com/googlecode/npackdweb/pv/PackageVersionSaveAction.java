package com.googlecode.npackdweb.pv;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.PackageVersion;
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
        PackageVersionPage pvp = new PackageVersionPage(null, false);
        pvp.fillForm(req);
        String error = pvp.validate();
        if (error == null) {
            String package2 = pvp.getPackageName();
            String version = pvp.getVersion();
            Objectify ofy = NWUtils.getObjectify();
            PackageVersion p = ofy.find(new Key<PackageVersion>(
                    PackageVersion.class, package2 + "@" + version));
            if (p == null) {
                p = new PackageVersion();
                p.name = package2 + "@" + version;
                p.package_ = package2;
                p.version = version;
            }
            pvp.fillObject(p);
            NWUtils.savePackageVersion(ofy, p);

            pvp = null;
            resp.sendRedirect("/p/" + p.package_);
        } else {
            pvp.setErrorMessage(error);
        }
        return pvp;
    }
}
