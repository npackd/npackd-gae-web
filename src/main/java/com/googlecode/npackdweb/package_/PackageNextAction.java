package com.googlecode.npackdweb.package_;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Show the next package ordered by title.
 */
public class PackageNextAction extends Action {

    /**
     * -
     */
    public PackageNextAction() {
        super("^/package/next$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getParameter("name");

        Package next = null;

        Package p = NWUtils.dsCache.getPackage(name, false);
        if (p != null) {
            next = NWUtils.dsCache.findNextPackage(p);
        }

        if (next != null) {
            resp.sendRedirect("/p/" + next.name);
        } else {
            resp.sendRedirect("/p?q=" + NWUtils.encode(name));
        }

        return null;
    }
}
