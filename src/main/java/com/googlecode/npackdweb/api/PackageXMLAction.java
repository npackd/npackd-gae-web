package com.googlecode.npackdweb.api;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Creates XML for a package.
 */
public class PackageXMLAction extends Action {

    /**
     * -
     */
    public PackageXMLAction() {
        super("^/api/p/([^/]+)$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getRequestURI().substring(7);

        Package r = NWUtils.dsCache.getPackage(name, true);
        if (r == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Package " +
                    name + " not found");
            return null;
        } else {
            return new PackageXMLPage(name);
        }
    }
}
