package com.googlecode.npackdweb.wlib;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Redirect
 */
public class SendRedirectAction extends Action {

    private final String format;

    /**
     * @param urlRegEx regular expression for URLs. Example: "^/def$"
     * @param format see String.format. Use %2$s for the whole string matched by
     * the regular expression, %2$s for the first captured group, etc.
     */
    public SendRedirectAction(String urlRegEx, String format) {
        super(urlRegEx, ActionSecurityType.ANONYMOUS);
        this.format = format;
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Pattern p = Pattern.compile(getURLRegExp());
        Matcher m = p.matcher(req.getRequestURI());
        m.matches();
        Object[] groups = new Object[m.groupCount() + 1];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = m.group(i);
        }
        String queryString = req.getQueryString();
        String f = String.format(format, groups);
        if (queryString != null) {
            f = f + '?' + queryString;
        }
        resp.sendRedirect(f);
        return null;
    }

    @Override
    public boolean matches(HttpServletRequest req) {
        return req.getMethod().equals("GET") && super.matches(req);
    }
}
