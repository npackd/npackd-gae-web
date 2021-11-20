package com.googlecode.npackdweb.pv;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Shows the list of package versions.
 */
public class PackageVersionListAction extends Action {

    /**
     * -
     */
    public PackageVersionListAction() {
        super("^/pv$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String start_ = req.getParameter("start");
        if (start_ == null)
            start_ = "0";
        int start;
        try {
            start = Integer.parseInt(start_);
        } catch (NumberFormatException e) {
            start = 0;
        }

        String tag = req.getParameter("tag");
        return new PackageVersionListPage(tag, start);
    }
}
