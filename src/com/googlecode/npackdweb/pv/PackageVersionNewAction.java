package com.googlecode.npackdweb.pv;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

/**
 * Create a new package version.
 */
public class PackageVersionNewAction extends Action {
    /**
     * -
     */
    public PackageVersionNewAction() {
        super("^/p/([^/]+)/new$", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Pattern p = Pattern.compile(getURLRegExp());
        Matcher m = p.matcher(req.getRequestURI());
        m.matches();
        String package_ = m.group(1);
        return new PackageVersionPage(new PackageVersion(package_, "1"), true);
    }
}
