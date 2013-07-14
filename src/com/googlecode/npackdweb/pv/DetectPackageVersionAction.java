package com.googlecode.npackdweb.pv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.Package;
import com.googlecode.npackdweb.PackageVersion;
import com.googlecode.npackdweb.Version;
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
        super("^/p/([^/]+)/detect$", ActionSecurityType.EDITOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Pattern pattern = Pattern.compile(getURLRegExp());
        Matcher m = pattern.matcher(req.getRequestURI());
        m.matches();
        String package_ = m.group(1);
        if (com.googlecode.npackdweb.Package.checkName(package_) != null) {
            throw new IOException("Invalid package name");
        }

        Objectify ofy = NWUtils.getObjectify();
        Package p = ofy.get(new Key<Package>(Package.class, package_));

        String msg = null;

        if (p.discoveryPage.trim().isEmpty()) {
            msg = "No discovery page (URL) is defined";
        } else if (p.discoveryRE.trim().isEmpty()) {
            msg = "No discovery regular expression for a package version is defined";
        }

        String version = null;
        if (msg == null) {
            URLFetchService s = URLFetchServiceFactory.getURLFetchService();
            HTTPResponse r = s.fetch(new URL(p.discoveryPage));
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(r.getContent()), "UTF-8"));
            String line;
            Pattern vp = Pattern.compile(p.discoveryRE);
            while ((line = br.readLine()) != null) {
                Matcher vm = vp.matcher(line);
                if (vm.find()) {
                    version = vm.group(1);
                    break;
                }
            }

            if (version == null) {
                msg = "Error detecting new version: the version number pattern was not found.";
            }
        }

        Version v = null;
        if (msg == null) {
            try {
                v = Version.parse(version);
                v.normalize();
            } catch (NumberFormatException e) {
                msg = "Error parsing the version number " + version + ": "
                        + e.getMessage();
            }
        }

        if (msg == null) {
            List<PackageVersion> versions = ofy.query(PackageVersion.class)
                    .filter("package_ =", package_).list();
            Collections.sort(versions, new Comparator<PackageVersion>() {
                public int compare(PackageVersion a, PackageVersion b) {
                    Version va = Version.parse(a.version);
                    Version vb = Version.parse(b.version);
                    return va.compare(vb);
                }
            });

            if (versions.size() == 0) {
                msg = "The package contains no versions. Found version "
                        + v.toString();
            } else {
                PackageVersion pv = versions.get(versions.size() - 1);
                Version vnewest = Version.parse(pv.version);
                if (vnewest.compare(v) > 0) {
                    msg = "The newest defined version " + vnewest.toString()
                            + " is bigger than the detected " + v.toString();
                } else if (vnewest.compare(v) == 0) {
                    msg = "The newest version is already in the repository ("
                            + vnewest + ")";
                } else {
                    PackageVersion copy = pv.copy();
                    copy.name = copy.package_ + "@" + v.toString();
                    copy.version = v.toString();

                    NWUtils.savePackageVersion(ofy, copy);
                    msg = "Created version " + v.toString()
                            + " (the newest available was " + vnewest + ")";
                    resp.sendRedirect("/p/" + pv.package_ + "/" + copy.version);
                    return null;
                }
            }
        }
        return new MessagePage(msg);
    }
}
