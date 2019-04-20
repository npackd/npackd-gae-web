package com.googlecode.npackdweb.package_;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Packages
 */
public class PackagesAction extends Action {

    /**
     * -
     */
    public PackagesAction() {
        super("^/p$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String start_ = req.getParameter("start");
        if (start_ == null) {
            start_ = "0";
        }
        int start;
        try {
            start = Integer.parseInt(start_);
        } catch (NumberFormatException e) {
            start = 0;
        }

        if (start >= 1000) {
            return new MessagePage(
                    "Cannot show results after 1000. Try to filter by a category first.");
        } else {
            String q = req.getParameter("q");
            if (q == null) {
                q = "";
            }

            String sort = req.getParameter("sort");
            if (!"created".equals(sort) && !"title".equals(sort) && !"stars".
                    equals(sort)) {
                sort = "title";
            }

            return new PackagesPage(q, sort,
                    start, req.getParameter("category0"),
                    req.getParameter("category1"), req.
                    getParameter("repository"));
        }
    }
}
