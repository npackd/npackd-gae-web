package com.googlecode.npackdweb;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.npackdweb.wlib.Page;

/**
 * A page with a frame.
 */
public abstract class MyPage extends Page {
    @Override
    public final void create(HttpServletRequest request,
            HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        Writer out = resp.getWriter();

        out.write(NWUtils.tmpl("Frame.html", "title", getTitle(), "content",
                createContent(request), "login",
                MyPage.getLoginHeader(request), "head", getHeadPart(), "menu",
                createMenu()));
        out.close();
    }

    /**
     * Creates HTML without the header and the footer.
     * 
     * @return HTML
     */
    public abstract String createContent(HttpServletRequest request)
            throws IOException;

    /**
     * @return page title
     */
    public abstract String getTitle();

    /**
     * @return HTML code that should be inserted in <head>
     */
    public String getHeadPart() {
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
     * Fills the fields from HTTP parameters (e.g. a <form>).
     * 
     * @param req
     *            HTTP request
     */
    public void fill(HttpServletRequest req) {
    }

    private String createMenu() {
        // http://www.red-team-design.com/css3-dropdown-menu
        HTMLWriter w = new HTMLWriter();
        w.start("ul", "id", "menu");
        w.start("li");
        w.e("a", "href", "/", "Home");
        w.end("li");

        w.start("li");
        w.e("a", "href", "/p", "Packages");
        w.start("ul");
        mi(w, "/p", "All packages");
        mi(w, "/p?q=&sort=created", "All packages sorted by creation date");
        mi(w,
                "http://code.google.com/p/windows-package-manager/issues/entry?template=Defect%20report%20from%20user",
                "Suggest a package*");
        w.end("ul");
        w.end("li");

        w.start("li");
        w.e("a", "href", "/rep", "Repositories");
        w.end("li");

        w.start("li");
        w.e("a", "href", "#", "Edit");
        w.start("ul");
        mi(w, "/rep/from-file", "Upload from a file");
        mi(w, "/rep/edit-as-xml", "Upload as text");
        mi(w, "/package/new", "Create new package");
        mi(w, "/download-failed", "List of failed downloads");
        mi(w, "/not-reviewed", "List of not reviewed versions");

        if (NWUtils.isAdminLoggedIn()) {
            mi(w, "/add-editor", "Add editor");
            mi(w, "/recreate-index", "Recreate index");
            mi(w, "/resave-packages", "Re-save packages");
            mi(w, "/info", "Show registered actions");
        }
        w.end("ul");
        w.end("li");

        w.start("li");
        w.e("a", "href", "http://code.google.com/p/windows-package-manager",
                "About");
        w.start("ul");
        mi(w, "http://code.google.com/p/windows-package-manager",
                "About Npackd");
        mi(w,
                "http://code.google.com/p/windows-package-manager/downloads/list",
                "Download client*");
        w.end("ul");
        w.end("li");

        w.end("ul");
        return w.toString();
    }

    private void mi(HTMLWriter w, String href, String title) {
        w.start("li");
        w.e("a", "href", href, title);
        w.end("li");
    }

    /**
     * Login/Logout-footer
     * 
     * @param request
     *            HTTP request
     * @return HTML
     * @throws IOException
     */
    private static String getLoginHeader(HttpServletRequest request)
            throws IOException {
        UserService userService = UserServiceFactory.getUserService();

        String thisURL = request.getRequestURI();
        if (request.getQueryString() != null)
            thisURL += "?" + request.getQueryString();
        HTMLWriter res = new HTMLWriter();
        if (request.getUserPrincipal() != null) {
            res.start("p");
            res.t("Hello, " + request.getUserPrincipal().getName()
                    + "!  You can ");
            res.e("a", "href", userService.createLogoutURL(thisURL), "sign out");
            res.t(".");
            res.end("p");
        } else {
            res.start("p");
            res.e("a", "href", userService.createLoginURL(thisURL), "Log on");
            res.end("p");
        }
        return res.toString();
    }
}
