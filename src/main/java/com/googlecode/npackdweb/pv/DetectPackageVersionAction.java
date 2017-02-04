package com.googlecode.npackdweb.pv;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.Version;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        Objectify ofy = DefaultServlet.getObjectify();
        Package p = ofy.get(new Key<Package>(Package.class, package_));
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

        Version v = null;
        try {
            v = p.findNewestVersion();
        } catch (IOException e) {
            msg = e.getMessage();
        }

        if (msg == null) {
            List<PackageVersion> versions = p.getSortedVersions(ofy);

            PackageVersion pv;
            Version vnewest = null;
            if (versions.size() > 0) {
                pv = versions.get(versions.size() - 1);
                vnewest = Version.parse(pv.version);
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
            } else {
                pv = null;
            }

            if (msg == null) {
                PackageVersion copy;
                if (pv == null) {
                    copy = new PackageVersion(package_, v.toString());
                } else {
                    copy = pv.copy();
                    UserService us = UserServiceFactory.getUserService();
                    copy.createdBy = us.getCurrentUser();
                    copy.createdAt = new Date();
                }
                copy.name = copy.package_ + "@" + v.toString();
                copy.version = v.toString();
                if (p.discoveryURLPattern.trim().length() > 0) {
                    Map<String, String> map = new HashMap<>();
                    map.put("${version}", v.toString());
                    /*
                     map.put("${{version2Parts}}", v.toString());
                     map.put("${{version3Parts}}", v.toString());
                     map.put("${{version2PartsWithoutDots}}", v.toString());
                     map.put("${{actualVersion}}", v.toString());
                     map.put("${{actualVersionWithoutDots}}", v.toString());
                     map.put("${{actualVersionWithUnderscores}}", v.toString());
                     map.put("${{match}}", v.toString());
                     */
                    copy.url = NWUtils.tmplString(p.discoveryURLPattern, map);
                }
                copy.addTag("untested");

                NWUtils.savePackageVersion(ofy, null, copy, true, true);
                msg =
                        "Created version " + v.toString() +
                        " (the newest available was " + vnewest + ")";
                resp.sendRedirect("/p/" + package_ + "/" + copy.version);
                return null;
            }
        }
        return new MessagePage(msg);
    }
}
