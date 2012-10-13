package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Create a new package version.
 */
public class PackageVersionNewAction extends Action {
	/**
	 * -
	 */
	public PackageVersionNewAction() {
		super("^/pv/new$", ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		return new PackageVersionPage(null);
	}
}
