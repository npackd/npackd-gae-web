package com.googlecode.npackdweb.package_;

import com.googlecode.npackdweb.AuthService;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.User;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Delete a package version.
 */
public class PackageDeleteConfirmedAction extends Action {

    /**
     * -
     */
    public PackageDeleteConfirmedAction() {
        super("^/package/delete-confirmed$", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getParameter("name");
        Package r = NWUtils.dsCache.getPackage(name, false);
        Page page;
        if (!r.isCurrentUserPermittedToModify()) {
            page =
                    new MessagePage(
                            "You do not have permission to modify this package");
        } else {
            AuthService us = AuthService.getInstance();
            User u = us.getCurrentUser();

            String message = req.getParameter("message");
            NWUtils.dsCache.deletePackage(name);
            if (message != null) {
                if (!NWUtils.isEqual(r.createdBy, u)) {
                    NWUtils.sendMailTo("The package " + r.title + " (" +
                            r.name + ") was deleted by " + u.getNickname() +
                            ".\n" + "More information: " + message,
                            r.createdBy.email);
                }
            }
            resp.sendRedirect("/p");
            page = null;
        }
        return page;
    }
}
