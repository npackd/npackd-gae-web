package com.googlecode.npackdweb.pv;

import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.Version;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A package version.
 */
public class PackageVersionDetailAction extends Action {

    /**
     * Returns the URL of the page presenting the specified version.
     *
     * @param pv package version
     * @return URL for the page
     */
    public static String getURL(PackageVersion pv) {
        return NWUtils.WEB_SITE + "/p/" + pv.package_ + "/" + pv.version;
    }

    /**
     * -
     */
    public PackageVersionDetailAction() {
        super("^/p/([^/]+)/([\\d.]+)$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Pattern p = Pattern.compile(getURLRegExp());
        Matcher m = p.matcher(req.getRequestURI());
        m.matches();
        String package_ = m.group(1);
        String version = m.group(2);

        Objectify ofy = DefaultServlet.getObjectify();
        PackageVersion r = ofy.load().key(Key.create(
                PackageVersion.class, package_ + "@" + version)).now();
        if (r == null) {
            Version v = Version.parse(version);
            if (!v.toString().equals(version)) {
                // trailing zeros or similar
                resp.sendRedirect("/p/" + package_ + "/" + v.toString());
                return null;
            } else {
                // non-existent version
                resp.sendRedirect("/p/" + package_);
                return null;
            }
        }

        return new PackageVersionPage(r, false);
    }
}
