package com.googlecode.npackdweb;

import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
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
        String id = req.getRequestURI().substring(5);

        Repository r = NWUtils.dsCache.getRepository(id);

        return new RepDetailPage(r);
    }
}
