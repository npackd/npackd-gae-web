package com.googlecode.npackdweb.api;

import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

        PackageVersion r =
                ofy.find(new Key<PackageVersion>(PackageVersion.class,
                                package_ + "@" + version));
        if (r == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        com.googlecode.npackdweb.db.Package pa = ofy.get(
                new Key<com.googlecode.npackdweb.db.Package>(
                        com.googlecode.npackdweb.db.Package.class, package_));
        if (!pa.isCurrentUserPermittedToModify() && !pw.equals(password)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        PackageVersion oldr = r.copy();

        r.tags.remove("untested");
        NWUtils.savePackageVersion(ofy, oldr, r, false, false);

        return null;
    }
}