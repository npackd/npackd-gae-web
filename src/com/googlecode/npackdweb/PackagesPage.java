package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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

		Objectify ofy = ObjectifyService.begin();
		packages = new ArrayList<Package>();
		for (Package p : ofy.query(Package.class).limit(PAGE_SIZE + 1).offset(
				start).order("title").fetch())
			packages.add(p);

		return NWUtils.tmpl(this, "Packages.html")
				+ createPager(start, packages.size() > PAGE_SIZE);
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
