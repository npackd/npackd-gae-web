package com.googlecode.npackdweb.wlib;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An action.
 */
public abstract class Action {

    private ActionSecurityType securityType = ActionSecurityType.ADMINISTRATOR;
    private String urlRegEx;
    private Pattern pattern;

    /**
     * @param urlRegEx regular expression for URLs. Example: "^/def$"
     */
    public Action(String urlRegEx) {
        this.urlRegEx = urlRegEx;
        this.pattern = Pattern.compile(urlRegEx);
    }

    /**
     * @param urlRegEx regular expression for URLs. Example: "^/def$"
     * @param st permission to call this action
     */
    public Action(String urlRegEx, ActionSecurityType st) {
        this(urlRegEx);
        this.securityType = st;
    }

    /**
     * @return regular expression for URLs. Example: "^/def$"
     */
    public String getURLRegExp() {
        return urlRegEx;
    }

    /**
     * Performs the action
     *
     * @param req request
     * @param resp response
     * @return page to write or null if a sendRedirect() was used
     * @throws IOException internal error
     */
    public abstract Page perform(HttpServletRequest req,
            HttpServletResponse resp) throws IOException;

    /**
     * @return permission to call this action
     */
    public ActionSecurityType getSecurityType() {
        return securityType;
    }

    /**
     * @param t permission to call this action
     */
    public void setSecurityType(ActionSecurityType t) {
        this.securityType = t;
    }

    /**
     * Checks whether this action can process the supplied request.
     *
     * @param req an HTTP request
     * @return true = this action can process this request
     */
    public boolean matches(HttpServletRequest req) {
        String pi = req.getRequestURI();
        if (pi == null) {
            pi = "/";
        }

        Matcher m = pattern.matcher(pi);
        return m.matches();
    }

    /**
     * @return URL pattern that can be processed by this action
     */
    public String pattern() {
        return pattern.pattern();
    }
}
