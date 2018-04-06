package com.googlecode.npackdweb;

import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import static com.googlecode.objectify.ObjectifyService.ofy;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Delete a repository.
 */
public class RepDeleteAction extends Action {

    /**
     * -
     */
    public RepDeleteAction() {
        super("^/rep/delete$");
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        long id = Long.parseLong(req.getParameter("id"));
        Objectify ofy = ofy();
        ofy.delete().key(Key.create(Repository.class, id));
        NWUtils.incDataVersion();

        resp.sendRedirect("/rep");
        return null;
    }
}
