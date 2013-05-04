package com.googlecode.npackdweb;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
                NWUtils.getLoginFooter(request), "head", getHeadPart(), "menu",
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
        w.e("a",
                "href",
                "http://code.google.com/p/windows-package-manager/downloads/list",
                "Download client*");
        w.end("li");
        w.start("li");
        w.e("a", "href", "#", "Edit");
        w.start("ul");
        mi(w, "/rep/from-file", "Upload from a file");
        mi(w, "/rep/edit-as-xml", "Upload as text");
        mi(w, "/package/new", "Create new package");
        /*<!-- 
        <li><a href="/add-editor">Add editor</a></li>
        <li><a href="/recreate-index">Recreate index</a></li>
        <li><a href="/resave-packages">Re-save packages</a></li>
         -->*/
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
}
