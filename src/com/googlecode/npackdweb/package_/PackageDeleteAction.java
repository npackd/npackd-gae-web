package com.googlecode.npackdweb.package_;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Deletes a package
 */
public class PackageDeleteAction extends Action {
    /**
     * -
     */
    public PackageDeleteAction() {
        super("^/package/delete$", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String id = req.getParameter("id");
        Objectify ofy = DefaultServlet.getObjectify();
        Package p = ofy.get(new Key<Package>(Package.class, id));
        Page page;
        if (!p.isCurrentUserPermittedToModify())
            page = new MessagePage(
                    "You do not have permission to modify this package");
        else
            page = new PackageDeletePage(p);
        return page;
    }
}
