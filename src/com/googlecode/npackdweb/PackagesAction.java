package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Packages
 */
public class PackagesAction extends Action {
	/**
	 * -
	 */
	public PackagesAction() {
		super("^/p$");
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		return new PackagesPage();
	}
}
