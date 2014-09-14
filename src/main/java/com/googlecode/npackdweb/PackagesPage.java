package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.googlecode.objectify.Objectify;

/**
 * Packages.
 */
public class PackagesPage extends MyPage {
	private static final int PAGE_SIZE = 20;
	private List<Package> packages;
	private boolean recent;
	private int start;
	private String query = "";
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
		QueryOptions.Builder ob =
				QueryOptions.newBuilder().setLimit(1000).setOffset(start);

		SortExpression se;
		if (!recent)
			se =
					SortExpression
							.newBuilder()
							.setExpression("title")
							.setDirection(
									SortExpression.SortDirection.ASCENDING)
							.setDefaultValue("").build();
		else
			se =
					SortExpression
							.newBuilder()
							.setExpression("createdAt")
							.setDirection(
									SortExpression.SortDirection.DESCENDING)
							.setDefaultValueDate(
									SearchApiLimits.MINIMUM_DATE_VALUE).build();
		ob =
				ob.setSortOptions(SortOptions.newBuilder()
						.addSortExpression(se).setLimit(1000));
		com.google.appengine.api.search.Query.Builder qb =
				com.google.appengine.api.search.Query.newBuilder().setOptions(
						ob.build());

		Results<ScoredDocument> r = index.search(qb.build(query));
		found = r.getNumberFound();

		List<String> ids = new ArrayList<String>();
		for (ScoredDocument sd : r) {
			ids.add(sd.getId());

			if (ids.size() > PAGE_SIZE)
				break;
		}

		Objectify obj = DefaultServlet.getObjectify();
		Map<String, Package> map = obj.get(Package.class, ids);

		packages = new ArrayList<Package>();

		for (Map.Entry<String, Package> e : map.entrySet()) {
			if (e.getValue() != null)
				packages.add(e.getValue());
		}

		return createContent2() +
				createPager(start, packages.size() > PAGE_SIZE);
	}

	private String createContent2() {
		HTMLWriter w = new HTMLWriter();

		w.unencoded(createSearchForm(this.query, this.recent));

		if (this.getPackages().size() == 0) {
			w.start("div", "style", "padding-top: 10px; padding-bottom: 10px");
			NWUtils.jsButton(w, "Create package " + this.query, "/package/new",
					"Creates a new package");
			w.t(" ");
			w.e("a",
					"href",
					"https://code.google.com/p/windows-package-manager/wiki/RejectedSoftware",
					"List of rejected packages*");
			w.t(" ");
			w.e("a",
					"href",
					"http://code.google.com/p/windows-package-manager/issues/entry?template=Defect%20report%20from%20user",
					"Suggest " + this.query + " for inclusion*");
			w.end("div");
		} else {
			w.start("div", "class", "nw-packages");
			Objectify ofy = DefaultServlet.getObjectify();
			Markdown4jProcessor mp = new Markdown4jProcessor();
			for (Package p : this.getPackages()) {
				License lic;
				if (!p.license.isEmpty())
					lic = ofy.find(License.class, p.license);
				else
					lic = null;

				w.start("div", "class", "media");
				w.start("a", "class", "pull-left", "href", "/p/" + p.name);
				if (p.icon.isEmpty()) {
					w.e("img", "src", "/App.png", "alt", p.title);
				} else {
					w.e("img", "src", p.icon, "style",
							"width: 32px; max-height: 32px", "alt", p.title);
				}
				w.end("a");

				w.start("div", "class", "media-body");
				w.start("h4", "class", "media-heading");
				w.t(" ");
				w.e("a", "href", "/p/" + p.name, p.title);

				if (p.noUpdatesCheck != null &&
						(System.currentTimeMillis() - p.noUpdatesCheck
								.getTime()) < 7L * 24 * 60 * 60 * 1000) {
					w.t(" ");
					w.e("span",
							"title",
							"This package was checked in the last 7 days and there were no updates",
							"\u2713");
				}

				/*
				 * // Google+ w.unencoded(
				 * " <div class='g-plusone' data-size='small' data-href='https://npackd.appspot.com/p/"
				 * + p.name + "'></div>");
				 */

				w.end("h4");

				try {
					w.unencoded(mp.process("Description: " + p.description));
				} catch (IOException e) {
					w.e("div",
							"Description: " + p.description +
									" Failed to parse the Markdown syntax: " +
									e.getMessage());
				}
				w.e("div",
						"Categories: " +
								(p.tags.size() == 0 ? "-" : NWUtils.join(", ",
										p.tags)) + "; License: " +
								(lic == null ? "unknown" : lic.title));
				w.end("div");
				w.end("div");
			}
			w.end("div");
		}

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
		w.start("form", "class", "form-inline", "method", "get", "action", "/p");
		w.t("Search: ");
		w.e("input", "class", "form-control", "type", "text", "name", "q",
				"value", query, "size", "50");
		w.t(" Sort: ");
		w.start("select", "class", "form-control", "name", "sort");
		w.e("option", "value", "title", "selected",
				!recent ? "selected" : null, "By title");
		w.e("option", "value", "created", "selected", recent ? "selected"
				: null, "By creation date");
		w.end("select");
		w.t(" ");
		w.e("input", "class", "btn btn-default", "type", "submit", "value",
				"Search");
		w.t(" ");
		w.e("a",
				"href",
				"https://developers.google.com/appengine/docs/java/search/overview#Building_Queries",
				"target", "_blank", "Help");
		w.end("form");
		return w.toString();
	}

	private String createPager(int cur, boolean hasNextPage) {
		HTMLWriter w = new HTMLWriter();
		w.start("ul", "class", "pager");
		if (cur >= PAGE_SIZE) {
			w.start("li");
			w.e("a",
					"href",
					"/p?start=" + (cur - PAGE_SIZE) +
							(recent ? "&sort=created" : "") + "&q=" +
							NWUtils.encode(this.query), "\u2190 Previous page");
			w.end("li");
		} else {
			w.start("li", "class", "disabled");
			w.e("a", "href", "#", "\u2190 Previous page");
			w.end("li");
		}

		if (hasNextPage) {
			w.start("li");
			w.e("a",
					"href",
					"/p?start=" + (cur + PAGE_SIZE) +
							(recent ? "&sort=created" : "") + "&q=" +
							NWUtils.encode(this.query), "Next page \u2192");
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

	@Override
	public boolean needsSearchFormInTheMenu() {
		return false;
	}

	@Override
	public String createBodyBottom(HttpServletRequest request)
			throws IOException {
		return super.createBodyBottom(request) +
				NWUtils.tmpl("GooglePlus.html");
	}
}
