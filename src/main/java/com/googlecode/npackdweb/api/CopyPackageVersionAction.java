package com.googlecode.npackdweb.api;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.db.Version;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create a copy of a package version.
 */
public class CopyPackageVersionAction extends Action {
    /**
     * -
     */
    public CopyPackageVersionAction() {
        super("^/api/copy$", ActionSecurityType.ADMINISTRATOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // package name
        String package_ = req.getParameter("package");
        if (Package.checkName(package_) != null) {
            throw new IOException("Invalid package name");
        }
        Package p = NWUtils.dsCache.getPackage(package_, false);
        if (p == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        // version
        Version from = Version.parse(req.getParameter("from"));
        from.normalize();
        PackageVersion pv = NWUtils.dsCache.getPackageVersion(package_ + "@" + from.toString());
        if (pv == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        // target version number
        Version to = Version.parse(req.getParameter("to"));
        to.normalize();

        // does a version already exist?
        PackageVersion copy = NWUtils.dsCache.getPackageVersion(
                package_ + "@" + to);
        if (copy != null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        // create a copy
        copy = pv.copy();
        copy.name = copy.package_ + "@" + to.toString();
        copy.version = to.toString();
        copy.createdAt = NWUtils.newDate();
        copy.createdBy = NWUtils.email2user(NWUtils.THE_EMAIL);
        copy.addTag("untested");

        NWUtils.dsCache.savePackageVersion(null, copy, true, false);

        return null;
    }
}
