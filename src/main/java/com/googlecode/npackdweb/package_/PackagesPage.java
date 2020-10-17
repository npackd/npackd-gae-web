package com.googlecode.npackdweb.package_;

import com.googlecode.npackdweb.AuthService;
import com.googlecode.npackdweb.MyEnglishAnalyzer;
import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.SearchService;
import com.googlecode.npackdweb.User;
import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.markdown4j.Markdown4jProcessor;

/**
 * Packages.
 */
public class PackagesPage extends MyPage {

    private static final int PAGE_SIZE = 20;
    private List<Package> packages = new ArrayList<>();
    private String sort;
    private int start;
    private String query = "";
    private long found;
    private String category0;
    private String category1;
    private String repository;
    private org.apache.lucene.facet.FacetResult category0Values;
    private org.apache.lucene.facet.FacetResult category1Values;
    private org.apache.lucene.facet.FacetResult repositoryValues;

    /**
     * true = show the search text field
     */
    public boolean showSearch = true;

    /**
     * -
     *
     * @param ids IDs of the packages
     */
    public PackagesPage(List<String> ids) {
        packages.addAll(NWUtils.dsCache.getPackages(ids, true));
        packages.
                sort((Package o1, Package o2) -> o1.title.compareTo(o2.title));

        found = packages.size();
    }

    /**
     * -
     *
     * @param query search query. Example: "title:Python"
     * @param sort "" (relevancy), "created", "title" or "stars" sorting order
     * @param start initial offset
     * @param category0 filter for the top-level category or null or ""
     * @param category1 filter for the second level level or null or ""
     * @param repository filter for the repository or null or ""
     */
    public PackagesPage(String query, String sort, int start,
            String category0, String category1, String repository) {
        this.query = query;
        this.sort = sort;
        this.start = start;

        this.repository = repository;
        if (this.repository != null && this.repository.trim().isEmpty()) {
            this.repository = null;
        }

        this.category0 = category0;
        if (this.category0 != null && this.category0.trim().isEmpty()) {
            this.category0 = null;
        }

        this.category1 = category1;
        if (this.category1 != null && this.category1.trim().isEmpty()) {
            this.category1 = null;
        }

        SearchService index = SearchService.getInstance();

        Sort se;
        if ("created".equals(sort)) {
            se = new Sort(new SortField("createdAt", SortField.Type.LONG, true));
        } else if ("stars".equals(sort)) {
            se = new Sort(new SortedNumericSortField("starred",
                    SortField.Type.INT, true));
        } else if ("title".equals(sort)) {
            se = new Sort(new SortField("title", SortField.Type.STRING, false));
        } else {
            se = null;
        }

        QueryParser parser = new QueryParser("fieldname",
                new MyEnglishAnalyzer());
        parser.setDefaultOperator(QueryParser.Operator.AND);
        Query q;
        try {
            String q2 = NWUtils.analyzeText(query);
            if (q2.trim().isEmpty()) {
                q = new MatchAllDocsQuery();
            } else {
                q = parser.parse(q2);
            }
        } catch (ParseException ex) {
            this.error = ex.getMessage();
            return;
        }

        List<TermQuery> termQueries = new ArrayList<>();

        if (this.category0 != null) {
            termQueries.
                    add(new TermQuery(new Term("category0", this.category0)));
        }
        if (this.category1 != null) {
            termQueries.
                    add(new TermQuery(new Term("category1", this.category1)));
        }
        if (this.repository != null) {
            termQueries.add(new TermQuery(
                    new Term("repository", this.repository)));
        }

        if (termQueries.size() > 0) {
            BooleanQuery.Builder bqb = new BooleanQuery.Builder().add(q,
                    BooleanClause.Occur.MUST);
            for (TermQuery tq : termQueries) {
                bqb.add(tq, BooleanClause.Occur.FILTER);
            }
            q = bqb.build();
        }

        List<String> ids = new ArrayList<>();
        NWUtils.LOG.info("Starting search");
        try {
            TopDocs r = index.search(q, start, PAGE_SIZE + 1, se);
            found = r.totalHits.value;

            if (found > 0) {
                List<String> fields = new ArrayList<>();
                fields.add("category0");
                fields.add("category1");
                fields.add("repository");
                List<org.apache.lucene.facet.FacetResult> facets =
                        index.getFacets(q, fields);
                category0Values = facets.get(0);
                category1Values = facets.get(1);
                repositoryValues = facets.get(2);
            }

            for (int i = 0; i < r.scoreDocs.length; i++) {
                Document d = index.getDocument(r.scoreDocs[i].doc);
                ids.add(d.get("id"));

                if (ids.size() > PAGE_SIZE) {
                    break;
                }
            }
            NWUtils.LOG.info("Search completed");
        } catch (IOException ex) {
            throw new InternalError(ex);
        } catch (ParseException ex) {
            this.error = ex.getMessage();
        }

        packages.addAll(NWUtils.dsCache.getPackages(ids, true));
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        return createContent2(request) +
                createPager(start, packages.size() > PAGE_SIZE);
    }

    private String createContent2(HttpServletRequest request) {
        HTMLWriter w = new HTMLWriter();

        if (showSearch) {
            w.unencoded(createSearchForm());
        }

        if (this.getPackages().isEmpty()) {
            if (showSearch) {
                w.start("div", "style",
                        "padding-top: 10px; padding-bottom: 10px");
                NWUtils.jsButton(w, "Create package " + this.query,
                        "/package/new",
                        "Creates a new package");
                w.t(" ");
                w.e("a",
                        "href",
                        "https://github.com/tim-lebedkov/npackd/issues/new",
                        "Suggest " + this.query + " for inclusion*");
                w.end("div");
            }
        } else {
            // list of packages
            w.start("div", "class", "nw-packages");
            Markdown4jProcessor mp = new Markdown4jProcessor();

            AuthService us = AuthService.getInstance();
            final User u = us.getCurrentUser();
            Editor e = null;
            if (u != null) {
                e = NWUtils.dsCache.findEditor(u);
            }

            for (Package p : this.getPackages()) {
                License lic;
                if (!p.license.isEmpty()) {
                    lic = NWUtils.dsCache.getLicense(p.license, true);
                } else {
                    lic = null;
                }

                w.start("div", "class", "media");
                w.start("a", "class", "pull-left", "href", "/p/" + p.name);
                if (p.icon.isEmpty()) {
                    w.e("img", "src", "/static/App.png", "alt", p.title);
                } else {
                    w.e("img", "src", p.icon, "style",
                            "width: 32px; max-height: 32px", "alt", p.title);
                }
                w.end("a");

                w.start("div", "class", "media-body");
                w.start("h4", "class", "media-heading");
                w.t(" ");
                w.e("a", "href", "/p/" + p.name, p.title);

                createTags(w, p.noUpdatesCheck, p.hasTag("end-of-life"));

                w.t(" ");
                NWUtils.star(w, p.name, e != null && e.starredPackages.
                        contains(
                                p.name),
                        p.starred);

                w.end("h4");

                try {
                    w.unencoded(mp.process("Description: " + p.description));
                } catch (IOException ex) {
                    w.e("div",
                            "Description: " + p.description +
                            " Failed to parse the Markdown syntax: " +
                            ex.getMessage());
                }
                w.start("div");
                w.t("Category: " +
                        (p.category.isEmpty() ? "-" : p.category) +
                        "; License: " +
                        (lic == null ? "unknown" : lic.title) +
                        "; Created by: ");

                w.unencoded(NWUtils.obfuscateEmail(p.createdBy.getEmail(),
                        request.getServerName()));
                w.end("div");
                w.end("div");
                w.end("div");
            }
            w.end("div");
        }

        return w.toString();
    }

    /**
     * Creates HTML for special tags.
     *
     * @param w output
     * @param noUpdatesCheck see Package.noUpdatesCheck
     * @param eol true = "end-of-life" tag is present
     */
    public static void createTags(HTMLWriter w,
            java.util.Date noUpdatesCheck, boolean eol) {
        if (noUpdatesCheck != null) {
            if ((System.currentTimeMillis() - noUpdatesCheck
                    .getTime()) < 7L * 24 * 60 * 60 * 1000) {
                w.t(" ");
                w.e("span", "class", "label label-success",
                        "title",
                        "This package was checked in the last 7 days and there were no updates",
                        "up-to-date");
            }
        }

        if (eol) {
            w.t(" ");
            w.e("span", "class", "label label-warning",
                    "title",
                    "There will be no new versions of this pckage.",
                    "end-of-life");
        }
    }

    /**
     * @return HTML for the search form
     */
    public String createSearchForm() {
        HTMLWriter w = new HTMLWriter();
        w.start("form", "class", "form-inline", "method", "get", "action",
                "/p", "id", "searchForm");
        w.t("Search: ");

        // from the C++ GUI:
        //
        // The words prefixed with a minus act as negative filters.
        w.e("input", "class", "form-control", "type", "text", "name", "q",
                "value", query, "size", "50", "title",
                "Enter here your search text. " +
                "You can enter multiple words if a package should contain all of them. " +
                "The search is case insensitive. " +
                "Special characters are filtered out. " +
                "Singular and plural word forms can be used. " +
                "Change in letter case or digits create a new search word. " +
                "Version numbers and common english words are ignored.");

        // sort order
        w.t(" Sort by: ");
        w.start("select", "class", "form-control", "name", "sort", "id", "sort");
        w.e("option", "value", "", "selected",
                "".equals(sort) ? "selected" : null, "Relevance");
        w.e("option", "value", "title", "selected",
                "title".equals(sort) ? "selected" : null, "Title");
        w.e("option", "value", "created", "selected", "created".equals(sort) ?
                "selected" :
                null, "Creation date");
        w.e("option", "value", "stars", "selected", "stars".equals(sort) ?
                "selected" :
                null, "Stars");
        w.end("select");

        w.t(" Category: ");
        if (category0Values != null && category0Values.labelValues.length > 1) {
            // we assume there were no category filter here. It should not
            // be possible.
            w.start("select", "class", "form-control", "name", "category0",
                    "id", "category0");
            w.e("option", "value", "", "Any");
            for (int i = 0; i < category0Values.labelValues.length; i++) {
                LabelAndValue c0 = category0Values.labelValues[i];
                w.e("option", "value", c0.label,
                        c0.label + " (" + c0.value.intValue() + ")");
            }
            w.end("select");
        } else {
            if (this.category0 != null) {
                w.e("input", "type", "hidden", "name", "category0", "value",
                        this.category0, "id", "category0");
                w.start("a", "href", "javascript:removeCategory0Filter()",
                        "title", "Remove this filter");
                w.t(this.category0);
                w.end("a");
            } else {
                if (category0Values != null &&
                        category0Values.labelValues.length > 0) {
                    w.t(category0Values.labelValues[0].label);
                } else {
                    w.t("-");
                }
            }
        }

        w.t(" Repository: ");
        if (repositoryValues != null && repositoryValues.labelValues.length > 1) {
            w.start("select", "class", "form-control", "name", "repository",
                    "id", "repository");
            w.e("option", "value", "", "Any");
            for (LabelAndValue c0 : repositoryValues.labelValues) {
                w.e("option", "value", c0.label,
                        c0.label + " (" + c0.value.intValue() + ")");
            }
            w.end("select");
        } else {
            if (this.repository != null) {
                w.e("input", "type", "hidden", "name", "repository", "value",
                        this.repository, "id", "repository");
                w.start("a", "href", "javascript:removeRepositoryFilter()",
                        "title", "Remove this filter");
                w.t(this.repository);
                w.end("a");
            } else {
                if (repositoryValues != null &&
                        repositoryValues.labelValues.length > 0) {
                    w.t(repositoryValues.labelValues[0].label);
                } else {
                    w.t("-");
                }
            }
        }

        w.t(" ");
        w.e("input", "class", "btn btn-default", "type", "submit", "value",
                "Search");
        w.t(" ");
        w.end("form");
        return w.toString();
    }

    private String createPager(int cur, boolean hasNextPage) {
        HTMLWriter w = new HTMLWriter();
        w.start("ul", "class", "pager");
        String p =
                ("".equals(sort) ? "" : "&sort=" + sort) + "&q=" +
                NWUtils.encode(this.query);
        if (category0 != null) {
            p += "&category0=" + NWUtils.encode(this.category0);
        }
        if (cur >= PAGE_SIZE) {
            w.start("li");
            w.e("a", "href", "/p?start=" + (cur - PAGE_SIZE) + p,
                    "\u2190 Previous page");
            w.end("li");
        } else {
            w.start("li", "class", "disabled");
            w.e("a", "href", "#", "\u2190 Previous page");
            w.end("li");
        }

        if (hasNextPage) {
            w.start("li");
            w.e("a", "href", "/p?start=" + (cur + PAGE_SIZE) + p,
                    "Next page \u2192");
            w.end("li");
        } else {
            w.start("li", "class", hasNextPage ? null : "disabled");
            w.e("a", "href", "#", "Next page \u2192");
            w.end("li");
        }

        w.end("ul");
        return w.toString();
    }

    @Override
    public String getTitle() {
        if ("created".equals(sort)) {
            return found + " packages sorted by creation time";
        } else if ("stars".equals(sort)) {
            return found + " packages sorted by the number of stars";
        } else if ("title".equals(sort)) {
            return found + " packages sorted by title";
        } else {
            return found + " packages sorted by relevance";
        }
    }

    /**
     * @return packages that should be shown on the page
     */
    public List<Package> getPackages() {
        return packages;
    }

    @Override
    public boolean needsSearchFormInTheMenu() {
        return false;
    }

    @Override
    public String createBodyBottom(HttpServletRequest request)
            throws IOException {
        HTMLWriter w = new HTMLWriter();
        w.start("script");
        InputStream stream = getClass().getResourceAsStream(
                "/templates/Packages.js");
        w.unencoded(NWUtils.readUTF8Resource(stream));
        w.end("script");

        return w.toString();
    }
}
