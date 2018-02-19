package com.googlecode.npackdweb.api;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.User;
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
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
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
        boolean install = "1".equals(req.getParameter("install"));
        boolean success = "1".equals(req.getParameter("success"));

        com.googlecode.npackdweb.db.Package pa = ofy.get(
                new Key<>(
                        com.googlecode.npackdweb.db.Package.class, package_));
        boolean ok = pa.isCurrentUserPermittedToModify();

        if (!ok) {
            ok = pw.equals(password);
        }

        if (!ok) {
            try {
                getOAuthUser();

                // any authenticated user is allowed to do this
                ok = true;

                NWUtils.LOG.log(Level.SEVERE, "Got user via OAuth");
            } catch (OAuthRequestException ex) {
                NWUtils.LOG.log(Level.SEVERE, null, ex);
            }
        }

        if (!ok) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        PackageVersion r =
                ofy.find(new Key<>(PackageVersion.class,
                                package_ + "@" + version));
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

        NWUtils.savePackageVersion(ofy, oldr, r, false, false);

        return null;
    }

    /**
     * @return a non-null User object if somebody is authenticated
     */
    private User getOAuthUser() throws OAuthRequestException {
        OAuthService oauth = OAuthServiceFactory.getOAuthService();
        String scope = "https://www.googleapis.com/auth/userinfo.email";
        Set<String> allowedClients = new HashSet<>();

        // see https://console.developers.google.com/apis/credentials?project=npackd
        // for more details about client IDs.
        // Npackd client 1.21
        allowedClients.add(
                "222041139141-vqv00o07p54ql0saefqkq59nupcgamih.apps.googleusercontent.com");

        User user = oauth.getCurrentUser(scope);
        if (user == null) {
            throw new OAuthRequestException("getCurrentUser() returned null");
        }

        String tokenAudience = oauth.getClientId(scope);
        if (!allowedClients.contains(tokenAudience)) {
            throw new OAuthRequestException("audience of token '" +
                    tokenAudience +
                    "' is not in allowed list " + allowedClients);
        }
        return user;
    }
}
