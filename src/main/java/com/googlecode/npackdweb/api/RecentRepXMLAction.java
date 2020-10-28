package com.googlecode.npackdweb.api;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Creates XML for 20 recently changed package versions.
 */
public class RecentRepXMLAction extends Action {
    /**
     * -
     */
    public RecentRepXMLAction() {
        super("^/rep/recent-xml$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        return new RecentRepXMLPage(req.getParameter("user"),
                req.getParameter("tag"), req.getParameter("package"));
    }
}
