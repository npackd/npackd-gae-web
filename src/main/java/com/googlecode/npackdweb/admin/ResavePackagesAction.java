package com.googlecode.npackdweb.admin;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import static com.googlecode.objectify.ObjectifyService.ofy;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Re-saves all packages.
 */
public class ResavePackagesAction extends Action {

    /**
     * -
     */
    public ResavePackagesAction() {
        super("^/resave-packages$", ActionSecurityType.ADMINISTRATOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Objectify ofy = ofy();
        List<Package> q = ofy.load().type(Package.class).list();
        ofy.save().entities(q);
        return new MessagePage("The packages were successfully re-saved");
    }
}
