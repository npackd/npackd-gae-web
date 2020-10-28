package com.googlecode.npackdweb.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

/**
 * Creates XML for 20 recently changed package versions.
 */
public class RecentRepXMLAction extends Action {
	/**
	 * -
	 */
	public RecentRepXMLAction() {
		super("^/rep/recent-xml$", ActionSecurityType.ANONYMOUS);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String user = req.getParameter("user");
		String tag = req.getParameter("tag");
		return new RecentRepXMLPage(user, tag, req.getParameter("package"));
	}
}
