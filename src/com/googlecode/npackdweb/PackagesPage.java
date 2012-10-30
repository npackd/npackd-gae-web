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
import com.googlecode.objectify.ObjectifyService;

/**
 * Packages.
 */
public class PackagesPage extends MyPage {
	private static final int PAGE_SIZE = 20;
	private List<Package> packages;

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		String start_ = request.getParameter("start");
		if (start_ == null)
			start_ = "0";
		int start;
		try {
			start = Integer.parseInt(start_);
		} catch (NumberFormatException e) {
			start = 0;
		}

		final String key = getClass().getCanonicalName() + ".content." + start
				+ "@" + DefaultServlet.dataVersion.get();
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers
				.getConsistentLogAndContinue(Level.INFO));
		String value = (String) syncCache.get(key); // read from cache
		if (value == null) {
			Objectify ofy = ObjectifyService.begin();
			packages = ofy.query(Package.class).limit(PAGE_SIZE + 1).offset(
					start).order("title").list();

			value = createContent2()
					+ createPager(start, packages.size() > PAGE_SIZE);

			syncCache.put(key, value); // populate cache
		}
		return value;
	}

	private String createContent2() {
		HTMLWriter w = new HTMLWriter();
		w.start("div", "class", "nw-packages");
		Objectify ofy = ObjectifyService.begin();
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
			r += " <a href='/p?start=" + (cur - PAGE_SIZE) + "'>";
		}
		r += "Previous page";
		if (cur >= PAGE_SIZE) {
			r += "</a>";
		}
		r += " ";
		if (hasNextPage) {
			r += "<a href='/p?start=" + (cur + PAGE_SIZE) + "'>";
		}
		r += "Next page";
		if (hasNextPage) {
			r += "</a>";
		}
		return r;
	}

	@Override
	public String getTitle() {
		return NWUtils.countPackages() + " packages";
	}

	/**
	 * @return packages that should be shown on the page
	 */
	public List<Package> getPackages() {
		return packages;
	}
}
