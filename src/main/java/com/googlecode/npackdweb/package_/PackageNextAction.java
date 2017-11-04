package com.googlecode.npackdweb.package_;

import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import java.io.IOException;
import java.util.List;
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

        Objectify ofy = DefaultServlet.getObjectify();
        Package next = null;

        Package p = ofy.find(new Key<Package>(Package.class, name));
        if (p != null) {
            List<Package> ps =
                    ofy.query(Package.class).limit(5).filter("title >=",
                            p.title).order("title").list();

            // find the next package
            for (int i = 0; i < ps.size() - 1; i++) {
                Package n = ps.get(i);

                if (n.name.equals(p.name)) {
                    next = ps.get(i + 1);
                    break;
                }
            }
        }

        if (next != null) {
            resp.sendRedirect("/p/" + next.name);
        } else {
            resp.sendRedirect("/p?q=" + NWUtils.encode(name));
        }

        return null;
    }
}
