package com.googlecode.npackdweb.package_;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.FormMode;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * A package.
 */
public class PackageDetailAction extends Action {
    /**
     * -
     */
    public PackageDetailAction() {
        super("^/p/([^/]+)$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getRequestURI().substring(3);

        Objectify ofy = DefaultServlet.getObjectify();
        Package r = ofy.find(new Key<Package>(Package.class, name));
        PackageDetailPage pdp = null;
        if (r == null) {
            if (NWUtils.isEditorLoggedIn()) {
                r = new Package(name);
                pdp = new PackageDetailPage(
                        NWUtils.isEditorLoggedIn() ? FormMode.EDIT
                                : FormMode.VIEW);
                pdp.fill(r);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            pdp = new PackageDetailPage(
                    NWUtils.isEditorLoggedIn() ? FormMode.EDIT : FormMode.VIEW);
            pdp.fill(r);
        }
        return pdp;
    }
}
