package com.googlecode.npackdweb.package_;

import com.googlecode.npackdweb.AuthService;
import com.googlecode.npackdweb.FormMode;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.User;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A package.
 */
public class PackageDetailAction extends Action {

    /**
     * -
     */
    public PackageDetailAction() {
        super("^/p/([^/]+)$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getRequestURI().substring(3);

        Package r = NWUtils.dsCache.getPackage(name, false);
        PackageDetailPage pdp = null;
        User u = AuthService.getInstance().getCurrentUser();
        if (r == null) {
            if (u != null) {
                r = new Package(name);
                pdp = new PackageDetailPage(FormMode.EDIT);
                pdp.fill(r);
            } else {
                resp.sendRedirect("/p?q=" + NWUtils.encode(name));
            }
        } else {
            pdp = new PackageDetailPage(u != null ? FormMode.EDIT :
                    FormMode.VIEW);
            pdp.fill(r);
        }
        return pdp;
    }
}
