package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

/**
 * Packages
 */
public class PackagesAction extends Action {
	/**
	 * -
	 */
	public PackagesAction() {
		super("^/p$", ActionSecurityType.ANONYMOUS);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String start_ = req.getParameter("start");
		if (start_ == null)
			start_ = "0";
		int start;
		try {
			start = Integer.parseInt(start_);
		} catch (NumberFormatException e) {
			start = 0;
		}
		String q = req.getParameter("q");
		if (q == null)
			q = "";

		return new PackagesPage(q, "created".equals(req.getParameter("sort")),
				start, req.getParameter("category0"),
				req.getParameter("category1"));
	}
}
