package com.googlecode.npackdweb.pv;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import static com.googlecode.objectify.ObjectifyService.ofy;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Delete a package version.
 */
public class PackageVersionDeleteConfirmedAction extends Action {

    /**
     * -
     */
    public PackageVersionDeleteConfirmedAction() {
        super("^/package-version/delete-confirmed$",
                ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getParameter("name");
        Objectify ofy = ofy();
        PackageVersion p = ofy.load().key(Key.create(
                PackageVersion.class, name)).now();
        Package pa = ofy.load().key(Key.create(Package.class, p.package_)).now();
        Page page;
        if (!pa.isCurrentUserPermittedToModify()) {
            page = new MessagePage(
                    "You do not have permission to modify this package");
        } else {
            ofy.delete().entity(p);
            NWUtils.incDataVersion();
            resp.sendRedirect("/p/" + p.package_);
            page = null;
        }
        return page;
    }
}
