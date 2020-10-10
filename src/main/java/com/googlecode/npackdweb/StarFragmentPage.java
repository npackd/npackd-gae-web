package com.googlecode.npackdweb;

import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Creates a "span" with a star.
 */
public class StarFragmentPage extends Page {

    private final String package_;

    /**
     * -
     *
     * @param package_ package name
     */
    public StarFragmentPage(String package_) {
        this.package_ = package_;
    }

    @Override
    public void create(HttpServletRequest request, HttpServletResponse resp)
            throws IOException {
        com.googlecode.npackdweb.db.Package p = NWUtils.dsCache.getPackage(
                package_, false);
        HTMLWriter w = new HTMLWriter();
        AuthService us = AuthService.getInstance();
        final User u = us.getCurrentUser();
        Editor e = null;
        if (u != null) {
            e = NWUtils.dsCache.findEditor(u);
        }

        NWUtils.star(w, p.name, e != null && e.starredPackages.contains(
                p.name),
                p.starred);

        resp.setContentType("text/html; charset=UTF-8");

        Writer out = resp.getWriter();
        out.write(w.toString());
        out.close();
    }
}
