package com.googlecode.npackdweb;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A page with a frame.
 */
public abstract class MyPage extends Page {

    /**
     * HTTP parameters
     */
    protected Map<String, String> params = new HashMap<>();

    /**
     * error message shown at the top of the page or null
     */
    public String error;

    /**
     * informational message or null
     */
    public String info;

    @Override
    public final void create(HttpServletRequest request,
            HttpServletResponse resp) throws IOException {

        resp.setContentType("text/html; charset=UTF-8");
        Writer out = resp.getWriter();

        out.write(NWUtils.tmpl("Frame.html", "title", getTitle(), "titleHTML",
                getTitleHTML(), "content", createContent(request),
                "head", createHead(),
                "scripts",
                getScriptsPart(), "menu", createMenu(request), "error", error,
                "info", info, "generator", this.getClass().getName(),
                "bodyBottom", createBodyBottom(request)));
        out.close();
    }

    /**
     * @return HTML inserted in the &lt;head&gt; tag.
     */
    public String createHead() throws IOException {
        return "";
    }

    /**
     * Creates HTML to be inserted before &lt;/body&gt;
     *
     * @param request request
     * @return HTML
     * @throws java.io.IOException any error
     */
    public String createBodyBottom(HttpServletRequest request)
            throws IOException {
        return "";
    }

    /**
     * Creates HTML without the header and the footer.
     *
     * @param request request
     * @return HTML
     * @throws java.io.IOException any error
     */
    public abstract String createContent(HttpServletRequest request)
            throws IOException;

    /**
     * @return page title
     */
    public abstract String getTitle();

    /**
     * @return page title as HTML. The default implementation just converts the
     * return value of {@link #getTitle()} to HTML
     */
    public String getTitleHTML() {
        HTMLWriter w = new HTMLWriter();
        w.t(getTitle());
        return w.toString();
    }

    /**
     * @return HTML code that should be inserted in &lt;head&gt;
     */
    public String getScriptsPart() {
        return "";
    }

    /**
     * Validates the data.
     *
     * @return error message or null
     */
    public String validate() {
        return null;
    }

    /**
     * Fills the fields from HTTP parameters (e.g. a &lt;form&gt;).
     *
     * @param req HTTP request
     */
    public void fill(HttpServletRequest req) {
        this.params.clear();
        for (Object e : req.getParameterMap().entrySet()) {
            Map.Entry me = (Map.Entry) e;
            String[] values = (String[]) me.getValue();
            if (values.length == 1) {
                this.params.put((String) me.getKey(), values[0]);
            }
        }
    }

    /**
     * @return true = create the search form in the menu
     */
    public boolean needsSearchFormInTheMenu() {
        return true;
    }

    private String createMenu(HttpServletRequest request) {
        try {
            return NWUtils.tmpl("Menu.html", "admin",
                    NWUtils.isAdminLoggedIn() ? "true" : null, "login",
                    MyPage.getLoginHeader(request), "searchForm",
                    needsSearchFormInTheMenu() ? "true" : null);
        } catch (IOException e) {
            throw new InternalError(e.getMessage());
        }
    }

    /**
     * Login/Logout-footer
     *
     * @param request HTTP request
     * @return HTML
     * @throws IOException
     */
    private static String getLoginHeader(HttpServletRequest request)
            throws IOException {
        UserService userService = UserServiceFactory.getUserService();

        String thisURL = request.getRequestURI();
        if (request.getQueryString() != null) {
            thisURL += "?" + request.getQueryString();
        }
        HTMLWriter res = new HTMLWriter();
        if (request.getUserPrincipal() != null) {
            res.t("Hello, " + userService.getCurrentUser().getNickname() +
                    "!  You can ");
            res.e("a", "href", userService.createLogoutURL(thisURL), "sign out");
            res.t(".");
        } else {
            NWUtils.LOG.info("Calling createLoginURL");
            res.e("a", "href", userService.createLoginURL(thisURL), "Log on");
        }
        return res.toString();
    }
}
