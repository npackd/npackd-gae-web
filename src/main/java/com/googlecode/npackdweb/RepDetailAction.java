package com.googlecode.npackdweb;

import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Repository details
 */
public class RepDetailAction extends Action {

    /**
     * -
     */
    public RepDetailAction() {
        super("^/rep/(\\d+)$", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        long id = Long.parseLong(req.getRequestURI().substring(5));

        Objectify ofy = DefaultServlet.getObjectify();
        Repository r = ofy.load().key(Key.create(Repository.class, id)).now();

        return new RepDetailPage(r);
    }
}
