package com.googlecode.npackdweb.api;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Marks a package as "starred".
 */
public class StarAction extends Action {

    /**
     * -
     */
    public StarAction() {
        super("^/api/star", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Objectify ofy = DefaultServlet.getObjectify();

        String package_ = req.getParameter("package");
        boolean star = "1".equals(req.getParameter("star"));

        com.googlecode.npackdweb.db.Package p = ofy.load().key(
                Key.create(
                        com.googlecode.npackdweb.db.Package.class, package_)).
                now();
        if (p == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        UserService us = UserServiceFactory.getUserService();
        User u = us.getCurrentUser();

        Editor e = NWUtils.findEditor(ofy, u);
        if (e == null) {
            e = new Editor(u);
        }

        NWUtils.starPackage(ofy, p, e, star);

        return null;
    }
}
