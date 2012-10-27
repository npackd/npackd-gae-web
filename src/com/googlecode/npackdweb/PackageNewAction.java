package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;

/**
 * Create a new package.
 */
public class PackageNewAction extends Action {
	/**
	 * -
	 */
	public PackageNewAction() {
		super("^/p/new$");
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		return new PackageDetailPage(null, true);
	}
}
