package com.googlecode.npackdweb.license;

import com.google.appengine.api.datastore.*;
import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.wlib.HTMLWriter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Licenses.
 */
public class LicensesPage extends MyPage {
    private static final int PAGE_SIZE = 20;
    private List<License> licenses;
    private final int start;
    private long found;
    private final String content;

    /**
     * -
     *
     * @param start initial offset
     */
    public LicensesPage(int start) {
        this.start = start;
        this.content = internalCreateContent();
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        return this.content;
    }

    private String internalCreateContent() {

        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();

        // find licenses
        com.google.appengine.api.datastore.Query query =
                new com.google.appengine.api.datastore.Query("License");
        query.addSort("title");
        PreparedQuery pq = datastore.prepare(query);
        FetchOptions fo = FetchOptions.Builder.withOffset(start);
        fo.limit(PAGE_SIZE + 1);
        final List<Entity> list = pq.asList(fo);
        licenses = new ArrayList<>();
        for (Entity e : list) {
            licenses.add(new License(e));
        }

        query = new com.google.appengine.api.datastore.Query("License");
        pq = datastore.prepare(query);
        found = pq.countEntities(FetchOptions.Builder.withDefaults());

        return createContent2() +
                createPager(start, licenses.size() > PAGE_SIZE);
    }

    private String createContent2() {
        HTMLWriter w = new HTMLWriter();

        w.start("div", "class", "nw-packages");
        for (License p : this.getLicenses()) {
            w.start("div", "class", "media");
            w.start("a", "class", "pull-left", "href", "/l/" + p.name);
            w.e("img", "src", "/App.png", "alt", p.title);
            w.end("a");

            w.start("div", "class", "media-body");
            w.start("h4", "class", "media-heading");
            w.t(" ");
            w.e("a", "href", "/l/" + p.name, p.title);
            w.end("h4");
            w.end("div");
            w.end("div");
        }
        w.end("div");

        return w.toString();
    }

    private String createPager(int cur, boolean hasNextPage) {
        HTMLWriter w = new HTMLWriter();
        w.start("ul", "class", "pager");
        if (cur >= PAGE_SIZE) {
            w.start("li");
            w.e("a", "href", "/l?start=" + (cur - PAGE_SIZE),
                    "← Previous page");
            w.end("li");
        } else {
            w.start("li", "class", "disabled");
            w.e("a", "href", "#", "← Previous page");
            w.end("li");
        }

        if (hasNextPage) {
            w.start("li");
            w.e("a", "href", "/l?start=" + (cur + PAGE_SIZE),
                    "Next page →");
            w.end("li");
        } else {
            w.start("li", "class", "disabled");
            w.e("a", "href", "#", "Next page →");
            w.end("li");
        }

        w.end("ul");
        return w.toString();
    }

    @Override
    public String getTitle() {
        return found + " licenses sorted by title";
    }

    /**
     * @return licenses that should be shown on the page
     */
    public List<License> getLicenses() {
        return licenses;
    }

    @Override
    public boolean needsSearchFormInTheMenu() {
        return false;
    }
}
