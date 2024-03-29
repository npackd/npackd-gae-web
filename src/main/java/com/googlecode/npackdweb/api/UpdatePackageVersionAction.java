package com.googlecode.npackdweb.api;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.db.Version;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Changes a package version.
 */
public class UpdatePackageVersionAction extends Action {
    /**
     * -
     */
    public UpdatePackageVersionAction() {
        super("^/api/set-url$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String pw = NWUtils.dsCache.getSetting("AdminPassword", "");
        if (pw == null) {
            pw = "";
        }
        if (pw.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "AdminPassword setting is not defined");
            return null;
        }

        String password = req.getParameter("password");
        String package_ = req.getParameter("package");
        String version = req.getParameter("version");

        Version v = Version.parse(version);
        v.normalize();

        PackageVersion r = NWUtils.dsCache.getPackageVersion(
                package_ + "@" + v);
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

        if (req.getMethod().equals("DELETE")) {
            NWUtils.dsCache.deletePackageVersion(r);
            return null;
        }

        PackageVersion oldr = r.copy();

        String url = req.getParameter("url");
        if (url != null) {
            String err = NWUtils.validateURL(url, false);
            if (err != null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }
            r.url = url;
        }
        String hashSum = req.getParameter("hash-sum");
        if (hashSum != null) {
            hashSum = hashSum.trim();
            if (!hashSum.isEmpty()) {
                String err = NWUtils.validateSHA1(hashSum);
                if (err != null) {
                    err = NWUtils.validateSHA256(hashSum);
                    if (err != null) {
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        return null;
                    }
                }
            }
            r.sha1 = hashSum;
        }
        String[] tags = req.getParameterValues("tag");
        if (tags != null) {
            for (String tag : tags) {
                r.addTag(tag);
            }
        }
        NWUtils.dsCache.savePackageVersion(oldr, r, true, false);

        return null;
    }
}
