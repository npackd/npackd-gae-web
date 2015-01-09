package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

/**
 * Shows the list of package versions.
 */
public class PackageVersionListAction extends Action {
	/**
	 * -
	 */
	public PackageVersionListAction() {
		super("^/pv$", ActionSecurityType.ANONYMOUS);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String tag = req.getParameter("tag");
		return new PackageVersionListPage(tag);
	}
}
