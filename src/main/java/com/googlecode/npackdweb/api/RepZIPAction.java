package com.googlecode.npackdweb.api;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Creates ZIP for a whole repository definition.
 */
public class RepZIPAction extends Action {

    /**
     * -
     */
    public RepZIPAction() {
        super("^/rep/zip$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String tag = req.getParameter("tag");
        boolean create = "true".equals(req.getParameter("create"));

        if (tag != null && !tag.isEmpty()) {
            Repository r = NWUtils.dsCache.findRepository(tag, true);
            if (r == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Repository " +
                        tag + " not found");
                return null;
            } else {
                return new RepZIPPage(tag, create);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Empty repository name");
            return null;
        }
    }
}
