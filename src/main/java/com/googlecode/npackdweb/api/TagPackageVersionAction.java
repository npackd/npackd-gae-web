package com.googlecode.npackdweb.api;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import static com.googlecode.objectify.ObjectifyService.ofy;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Adds or removes a tag for a package version.
 */
public class TagPackageVersionAction extends Action {

    /**
     * -
     */
    public TagPackageVersionAction() {
        super("^/api/tag$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Objectify ofy = ofy();
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
        String name = req.getParameter("name");
        String value = req.getParameter("value");

        PackageVersion r =
                ofy.load().key(Key.create(PackageVersion.class,
                                package_ + "@" + version)).now();
        if (r == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        com.googlecode.npackdweb.db.Package pa = ofy.load().key(
                Key.create(
                        com.googlecode.npackdweb.db.Package.class, package_)).
                now();
        if (!pa.isCurrentUserPermittedToModify() && !pw.equals(password)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        PackageVersion oldr = r.copy();

        if ("1".equals(value)) {
            r.addTag(name);
        } else {
            r.tags.remove(name);
        }
        NWUtils.savePackageVersion(ofy, oldr, r, false, false);

        return null;
    }
}
