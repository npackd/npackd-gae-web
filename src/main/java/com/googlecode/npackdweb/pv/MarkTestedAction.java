package com.googlecode.npackdweb.pv;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Marks the specified package version as "tested".
 */
public class MarkTestedAction extends Action {

    /**
     * -
     */
    public MarkTestedAction() {
        super("^/package-version/mark-tested$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Objectify ofy = DefaultServlet.getObjectify();
        String pw = NWUtils.getSetting(ofy, "MarkTestedPassword", "");
        if (pw == null) {
            pw = "";
        }
        if (pw.trim().isEmpty()) {
            return new MessagePage("MarkTestedPassword setting is not defined");
        }

        String password = req.getParameter("password");
        String package_ = req.getParameter("package");
        String version = req.getParameter("version");

        if (!pw.equals(password)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        PackageVersion r
                = ofy.find(new Key<PackageVersion>(PackageVersion.class,
                                package_ + "@" + version));
        if (r == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        r.tags.remove("untested");
        NWUtils.savePackageVersion(ofy, r, false, false, true);

        return null;
    }
}
