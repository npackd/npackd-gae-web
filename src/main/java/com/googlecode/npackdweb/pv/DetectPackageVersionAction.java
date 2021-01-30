package com.googlecode.npackdweb.pv;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Version;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Detects new version of a package.
 */
public class DetectPackageVersionAction extends Action {

    /**
     * -
     */
    public DetectPackageVersionAction() {
        super("^/p/([^/]+)/detect$", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Pattern pattern = Pattern.compile(getURLRegExp());
        Matcher m = pattern.matcher(req.getRequestURI());
        m.matches();
        String package_ = m.group(1);
        if (com.googlecode.npackdweb.db.Package.checkName(package_) != null) {
            throw new IOException("Invalid package name");
        }

        Package p = NWUtils.dsCache.getPackage(package_, false);
        if (!p.isCurrentUserPermittedToModify()) {
            return new MessagePage(
                    "You do not have permission to modify this package");
        }

        String msg = null;

        if (p.discoveryPage.trim().isEmpty()) {
            msg = "No discovery page (URL) is defined";
        } else if (p.discoveryRE.trim().isEmpty()) {
            msg =
                    "No discovery regular expression for a package version is defined";
        }

        if (msg != null) {
            return new MessagePage(msg);
        }

        Version v = null;
        Matcher found = null;
        try {
            found = p.findNewestVersion();
            v = NWUtils.parseVersion(found.group(1));
        } catch (IOException e) {
            msg = e.getMessage();
        }

        if (msg != null) {
            return new MessagePage(msg);
        }

        List<PackageVersion> versions = NWUtils.dsCache.
                getSortedVersions(p.name);

        if (versions.size() > 0) {
            PackageVersion pv = versions.get(versions.size() - 1);
            Version vnewest = Version.parse(pv.version);
            if (vnewest.compare(v) > 0) {
                msg =
                        "The newest defined version " + vnewest.toString() +
                        " is bigger than the detected " +
                        v.toString();
            } else if (vnewest.compare(v) == 0) {
                msg =
                        "The newest version is already in the repository (" +
                        vnewest + ")";
            }
        }

        if (msg != null) {
            return new MessagePage(msg);
        }

        PackageVersion copy = p.
                createDetectedVersion(found, v, 100L * 1024 * 1024);

        resp.sendRedirect("/p/" + package_ + "/" + copy.version);
        return null;
    }
}
