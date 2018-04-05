package com.googlecode.npackdweb;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Action for /star
 */
public class StarFragmentAction extends Action {

    /**
     * -
     */
    public StarFragmentAction() {
        super("^/star$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final String package_ = req.getParameter("package");
        if (package_ == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Empty repository name");
            return null;
        } else {
            return new StarFragmentPage(package_);
        }
    }
}
