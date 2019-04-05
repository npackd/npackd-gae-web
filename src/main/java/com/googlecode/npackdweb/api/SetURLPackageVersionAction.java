package com.googlecode.npackdweb.api;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Changes the URL for a package version.
 */
public class SetURLPackageVersionAction extends Action {

    /**
     * -
     */
    public SetURLPackageVersionAction() {
        super("^/api/set-url$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String pw = NWUtils.dsCache.getSetting("MarkTestedPassword", "");
        if (pw == null) {
            pw = "";
        }
        if (pw.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "MarkTestedPassword setting is not defined");
            return null;
        }

        String password = req.getParameter("password");
        String package_ = req.getParameter("package");
        String version = req.getParameter("version");

        String url = req.getParameter("url");
        if (!url.startsWith(
                "https://github.com/tim-lebedkov/packages/releases/download/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong new URL");
            return null;
        }

        PackageVersion r = NWUtils.dsCache.getPackageVersion(
                package_ + "@" + version);
        if (r == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        com.googlecode.npackdweb.db.Package pa = NWUtils.dsCache.getPackage(
                package_, false);
        if (!pa.isCurrentUserPermittedToModify() && !pw.equals(password)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        PackageVersion oldr = r.copy();
        r.url = url;
        NWUtils.dsCache.savePackageVersion(oldr, r, true, false);

        return null;
    }
}
