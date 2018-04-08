package com.googlecode.npackdweb.license;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Deletes a license
 */
public class LicenseDeleteAction extends Action {

    /**
     * -
     */
    public LicenseDeleteAction() {
        super("^/license/delete$", ActionSecurityType.ADMINISTRATOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getParameter("id");
        License r = NWUtils.dsCache.getLicense(name, false);
        Page page;
        if (!r.isCurrentUserPermittedToModify()) {
            page =
                    new MessagePage(
                            "You do not have permission to modify this license");
        } else {
            NWUtils.dsCache.deleteLicense(name);
            resp.sendRedirect("/l");
            page = null;
        }
        return page;
    }
}
