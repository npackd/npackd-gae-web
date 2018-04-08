package com.googlecode.npackdweb;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;
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
        NWUtils.dsCache.deleteRepository(id);

        resp.sendRedirect("/rep");
        return null;
    }
}
