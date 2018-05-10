package com.googlecode.npackdweb.package_;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Request permissions for a package.
 */
public class RequestPermissionsAction extends Action {

    /**
     * -
     */
    public RequestPermissionsAction() {
        super("^/request-permissions$", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String package_ = req.getParameter("package");
        if (com.googlecode.npackdweb.db.Package.checkName(package_) != null) {
            throw new IOException("Invalid package name");
        }

        Package p = NWUtils.dsCache.getPackage(package_, false);
        if (p.isCurrentUserPermittedToModify()) {
            return new MessagePage(
                    "You already have permission to modify this package");
        } else {
            UserService us = UserServiceFactory.getUserService();

            NWUtils.sendMailToAdmin("User " + us.getCurrentUser().getEmail() +
                    " requests access to modify the package " + package_);
            return new MessagePage(
                    "Your request was sent to the admins. We will contact you via " +
                    us.getCurrentUser().getEmail());
        }
    }
}
