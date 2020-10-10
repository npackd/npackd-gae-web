package com.googlecode.npackdweb.api;

import com.googlecode.npackdweb.AuthService;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Marks the specified package version as "tested".
 */
public class NotifyAction extends Action {

    /**
     * -
     */
    public NotifyAction() {
        super("^/api/notify$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String pw = NWUtils.dsCache.getSetting("MarkTestedPassword", "");
        if (pw == null) {
            pw = "";
        }
        if (pw.trim().isEmpty()) {
            return new MessagePage("MarkTestedPassword setting is not defined");
        }

        String password = req.getParameter("password");
        String package_ = req.getParameter("package");
        String version = req.getParameter("version");
        boolean install = "1".equals(req.getParameter("install"));
        boolean success = "1".equals(req.getParameter("success"));

        com.googlecode.npackdweb.db.Package pa = NWUtils.dsCache.getPackage(
                package_, false);
        boolean ok = pa.isCurrentUserPermittedToModify();

        if (!ok) {
            ok = pw.equals(password);
        }

        if (!ok) {
            if (!AuthService.getInstance().isUserLoggedIn()) {
                ok = false;
            }
        }

        if (!ok) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        PackageVersion r = NWUtils.dsCache.getPackageVersion(
                package_ + "@" + version);
        if (r == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        PackageVersion oldr = r.copy();

        if (install) {
            if (success) {
                r.installSucceeded++;
            } else {
                r.installFailed++;
            }
        } else {
            if (success) {
                r.uninstallSucceeded++;
            } else {
                r.uninstallFailed++;
            }
        }

        NWUtils.dsCache.savePackageVersion(oldr, r, false, false);

        return null;
    }
}
