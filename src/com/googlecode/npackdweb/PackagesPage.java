package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

/**
 * Packages.
 */
public class PackagesPage extends MyPage {
	private static final int PAGE_SIZE = 20;
	private List<Package> packages;
	private boolean recent;
	private int start;

	/**
	 * -
	 * 
	 * @param recent
	 *            true = sort by creation time, false = sort by title
	 * @param start
	 *            initial offset
	 */
	public PackagesPage(boolean recent, int start) {
		this.recent = recent;
		this.start = start;
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		final String key = getClass().getCanonicalName() + ".content." + start
		        + "@" + DefaultServlet.dataVersion.get() + "," + recent;

		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers
		        .getConsistentLogAndContinue(Level.INFO));
		String value = (String) syncCache.get(key); // read from cache
		if (value == null) {
			Objectify ofy = NWUtils.getObjectify();
			Query<Package> q = ofy.query(Package.class).limit(PAGE_SIZE + 1)
			        .offset(start);
			if (recent)
				q.order("-createdAt");
			else
				q.order("title");
			packages = q.list();

			value = createContent2()
			        + createPager(start, packages.size() > PAGE_SIZE);

			syncCache.put(key, value); // populate cache
		}
		return value;
	}

	private String createContent2() {
		HTMLWriter w = new HTMLWriter();
		w.start("div", "class", "nw-packages");
		Objectify ofy = NWUtils.getObjectify();
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
			w.end("h3");
			w.e("div", "Description: " + p.description);
			w.e("div", "License: " + (lic == null ? "unknown" : lic.title));
			w.end("div");
		}
		w.end("div");

		return w.toString();
	}

	private String createPager(int cur, boolean hasNextPage) {
		String r = "";
		if (cur >= PAGE_SIZE) {
			r += " <a href='/p?start=" + (cur - PAGE_SIZE)
			        + (recent ? "&sort=created" : "") + "'>";
		}
		r += "Previous page";
		if (cur >= PAGE_SIZE) {
			r += "</a>";
		}
		r += " ";
		if (hasNextPage) {
			r += "<a href='/p?start=" + (cur + PAGE_SIZE)
			        + (recent ? "&sort=created" : "") + "'>";
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
			return NWUtils.countPackages()
			        + " packages sorted by creation time";
		else
			return NWUtils.countPackages() + " packages sorted by title";
	}

	/**
	 * @return packages that should be shown on the page
	 */
	public List<Package> getPackages() {
		return packages;
	}
}
