package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
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

		final String key = getClass().getCanonicalName() + ".content." + start;
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers
				.getConsistentLogAndContinue(Level.INFO));
		String value = (String) syncCache.get(key); // read from cache
		if (value == null) {
			Objectify ofy = ObjectifyService.begin();
			packages = ofy.query(Package.class).limit(PAGE_SIZE + 1).offset(
					start).order("title").list();

			value = NWUtils.tmpl(this, "Packages.html")
					+ createPager(start, packages.size() > PAGE_SIZE);

			syncCache.put(key, value); // populate cache
		}
		return value;
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
		return "Packages";
	}

	/**
	 * @return packages that should be shown on the page
	 */
	public List<Package> getPackages() {
		return packages;
	}
}
