package com.googlecode.npackdweb.admin;

import com.google.appengine.api.users.User;
import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Adds permissions to all packages for a user.
 */
public class AddPermissionsConfirmedAction extends Action {

    /**
     * -
     */
    public AddPermissionsConfirmedAction() {
        super("^/add-permissions-confirmed$", ActionSecurityType.ADMINISTRATOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Page res;

        AddPermissionsPage p = new AddPermissionsPage();
        p.fill(req);
        String err = p.validate();
        if (err == null) {
            User u = NWUtils.email2user(p.email);
            Objectify ofy = DefaultServlet.getObjectify();
            List<Package> q = ofy.query(Package.class).list();
            List<Package> toSave = new ArrayList<Package>();
            for (Package p_ : q) {
                if (!p_.isUserPermittedToModify(u)) {
                    p_.permissions.add(u);
                    toSave.add(p_);
                }
            }
            ofy.put(toSave);
            res =
                    new MessagePage("The permissions for " + p.email + " for " +
                            toSave.size() + " packages " +
                            "were successfully changed");
        } else {
            p.error = err;
            res = p;
        }

        return res;
    }
}
