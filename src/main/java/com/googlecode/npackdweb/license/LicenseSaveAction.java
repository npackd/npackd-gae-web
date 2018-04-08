package com.googlecode.npackdweb.license;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.DatastoreCache;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Save or create a license.
 */
public class LicenseSaveAction extends Action {

    /**
     * -
     */
    public LicenseSaveAction() {
        super("^/license/save$", ActionSecurityType.ADMINISTRATOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getParameter("name");
        LicensePage pdp = new LicensePage();
        pdp.fill(req);
        String msg = pdp.validate();
        if (msg == null) {
            License p = NWUtils.dsCache.getLicense(name, false);
            if (p == null) {
                p = new License();
                p.name = name;
            } else if (!p.isCurrentUserPermittedToModify()) {
                return new MessagePage(
                        "You do not have permission to modify this license");
            }
            pdp.fillObject(p);
            DatastoreCache.saveLicense(p, true);
            pdp = null;
            resp.sendRedirect("/l/" + p.name);
        } else {
            pdp.error = msg;
        }
        return pdp;
    }
}
