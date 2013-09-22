package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.markdown4j.Markdown4jProcessor;

import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import com.google.appengine.api.search.checkers.SearchApiLimits;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Packages.
 */
public class PackagesPage extends MyPage {
    private static final int PAGE_SIZE = 20;
    private List<Package> packages;
    private boolean recent;
    private int start;
    private String query;
    private long found;
    private String content;

    /**
     * -
     * 
     * @param query
     *            search query. Example: "title:Python"
     * @param recent
     *            true = sort by creation time, false = sort by title
     * @param start
     *            initial offset
     */
    public PackagesPage(String query, boolean recent, int start) {
        this.query = query;
        this.recent = recent;
        this.start = start;
        this.content = internalCreateContent();
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        return this.content;
    }

    private String internalCreateContent() {
        Index index = NWUtils.getIndex();
        QueryOptions.Builder ob = QueryOptions.newBuilder().setLimit(1000)
                .setOffset(start);

        SortExpression se;
        if (!recent)
            se = SortExpression.newBuilder().setExpression("title")
                    .setDirection(SortExpression.SortDirection.ASCENDING)
                    .setDefaultValue("").build();
        else
            se = SortExpression.newBuilder().setExpression("createdAt")
                    .setDirection(SortExpression.SortDirection.DESCENDING)
                    .setDefaultValueDate(SearchApiLimits.MINIMUM_DATE_VALUE)
                    .build();
        ob = ob.setSortOptions(SortOptions.newBuilder().addSortExpression(se)
                .setLimit(1000));
        com.google.appengine.api.search.Query.Builder qb = com.google.appengine.api.search.Query
                .newBuilder().setOptions(ob.build());

        Results<ScoredDocument> r = index.search(qb.build(query));
        found = r.getNumberFound();

        packages = new ArrayList<Package>();
        Objectify obj = DefaultServlet.getObjectify();
        for (ScoredDocument sd : r) {
            String id = sd.getId();
            Package p = obj.find(new Key<Package>(Package.class, id));
            if (p != null) {
                packages.add(p);
                if (packages.size() > PAGE_SIZE)
                    break;
            }
        }

        return createContent2()
                + createPager(start, packages.size() > PAGE_SIZE);
    }

    private String createContent2() {
        HTMLWriter w = new HTMLWriter();

        w.unencoded(createSearchForm(this.query, this.recent));

        w.start("div", "class", "nw-packages");
        Objectify ofy = DefaultServlet.getObjectify();
        Markdown4jProcessor mp = new Markdown4jProcessor();
        for (Package p : this.getPackages()) {
            License lic;
            if (!p.license.isEmpty())
                lic = ofy.find(License.class, p.license);
            else
                lic = null;

            w.start("div");
            w.start("h3");
            if (p.icon.isEmpty()) {
                w.e("img", "src", "/App.png");
            } else {
                w.e("img", "src", p.icon, "style",
                        "max-width: 32px; max-height: 32px");
            }
            w.t(" ");
            w.e("a", "href", "/p/" + p.name, p.title);
            if (p.noUpdatesCheck != null
                    && (System.currentTimeMillis() - p.noUpdatesCheck.getTime()) < 7L
                            * 24 * 60 * 60 * 1000) {
                w.t(" ");
                w.e("span",
                        "title",
                        "This package was checked in the last 7 days and there were no updates",
                        "\u2713");
            }
            w.end("h3");
            try {
                w.unencoded(mp.process("Description: " + p.description));
            } catch (IOException e) {
                w.e("div",
                        "Description: " + p.description
                                + " Failed to parse the Markdown syntax: "
                                + e.getMessage());
            }
            w.e("div", "License: " + (lic == null ? "unknown" : lic.title));
            w.end("div");
        }
        w.end("div");

        return w.toString();
    }

    /**
     * @param query
     *            search text
     * @param recent
     *            true = select "sort by creation date", false = "sort by title"
     * @return HTML for the search form
     */
    public static String createSearchForm(String query, boolean recent) {
        HTMLWriter w = new HTMLWriter();
        w.start("form", "method", "get", "action", "/p");
        w.t("Search: ");
        w.e("input", "type", "text", "name", "q", "value", query, "size", "50");
        w.t(" Sort: ");
        w.start("select", "name", "sort");
        w.e("option", "value", "title", "selected",
                !recent ? "selected" : null, "By title");
        w.e("option", "value", "created", "selected", recent ? "selected"
                : null, "By creation date");
        w.end("select");
        w.t(" ");
        w.e("input", "class", "input", "type", "submit", "value", "Search");
        w.t(" ");
        w.e("a",
                "href",
                "https://developers.google.com/appengine/docs/java/search/overview#Building_Queries",
                "target", "_blank", "Help");
        w.end("form");
        return w.toString();
    }

    private String createPager(int cur, boolean hasNextPage) {
        String r = "";
        if (cur >= PAGE_SIZE) {
            r += " <a href='/p?start=" + (cur - PAGE_SIZE)
                    + (recent ? "&sort=created" : "") + "&q="
                    + NWUtils.encode(this.query) + "'>";
        }
        r += "Previous page";
        if (cur >= PAGE_SIZE) {
            r += "</a>";
        }
        r += " ";
        if (hasNextPage) {
            r += "<a href='/p?start=" + (cur + PAGE_SIZE)
                    + (recent ? "&sort=created" : "") + "&q="
                    + NWUtils.encode(this.query) + "'>";
        }
        r += "Next page";
        if (hasNextPage) {
            r += "</a>";
        }
        return r;
    }

    @Override
    public String getTitle() {
        if (recent)
            return found + " packages sorted by creation time";
        else
            return found + " packages sorted by title";
    }

    /**
     * @return packages that should be shown on the page
     */
    public List<Package> getPackages() {
        return packages;
    }
}
