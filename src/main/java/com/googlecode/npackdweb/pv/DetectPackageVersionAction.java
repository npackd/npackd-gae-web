package com.googlecode.npackdweb.pv;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            msg
                    = "No discovery regular expression for a package version is defined";
        }

        Version v = null;
        try {
            v = p.findNewestVersion();
        } catch (IOException e) {
            msg = e.getMessage();
        }

        if (msg == null) {
            List<PackageVersion> versions
                    = ofy.query(PackageVersion.class)
                    .filter("package_ =", package_).list();
            Collections.sort(versions, new Comparator<PackageVersion>() {
                @Override
                public int compare(PackageVersion a, PackageVersion b) {
                    Version va = Version.parse(a.version);
                    Version vb = Version.parse(b.version);
                    return va.compare(vb);
                }
            });

            PackageVersion pv;
            Version vnewest = null;
            if (versions.size() > 0) {
                pv = versions.get(versions.size() - 1);
                vnewest = Version.parse(pv.version);
                if (vnewest.compare(v) > 0) {
                    msg
                            = "The newest defined version " + vnewest.toString()
                            + " is bigger than the detected "
                            + v.toString();
                } else if (vnewest.compare(v) == 0) {
                    msg
                            = "The newest version is already in the repository ("
                            + vnewest + ")";
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
                }
                copy.name = copy.package_ + "@" + v.toString();
                copy.version = v.toString();
                copy.addTag("untested");

                NWUtils.savePackageVersion(ofy, copy, true, true, false);
                msg
                        = "Created version " + v.toString()
                        + " (the newest available was " + vnewest + ")";
                resp.sendRedirect("/p/" + package_ + "/" + copy.version);
                return null;
            }
        }
        return new MessagePage(msg);
    }
}
