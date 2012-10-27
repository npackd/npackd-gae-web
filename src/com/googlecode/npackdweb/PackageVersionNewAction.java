package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;

/**
 * Create a new package version.
 */
public class PackageVersionNewAction extends Action {
	/**
	 * -
	 */
	public PackageVersionNewAction() {
		super("^/pv/new$");
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		return new PackageVersionPage(null);
	}
}
