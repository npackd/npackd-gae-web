package com.googlecode.npackdweb.package_;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Delete a package version.
 */
public class PackageDeleteConfirmedAction extends Action {
    /**
     * -
     */
    public PackageDeleteConfirmedAction() {
        super("^/package/delete-confirmed$", ActionSecurityType.EDITOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getParameter("name");
        Objectify ofy = NWUtils.getObjectify();
        Package r = ofy.find(new Key<Package>(Package.class, name));
        Page page;
        if (!r.isCurrentUserPermittedToModify())
            page = new MessagePage(
                    "You do not have permission to modify this package");
        else {
            NWUtils.deletePackage(ofy, name);
            resp.sendRedirect("/p");
            page = null;
        }
        return page;
    }
}
