package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * A package version.
 */
public class PackageVersionDetailAction extends Action {
	/**
	 * -
	 */
	public PackageVersionDetailAction() {
		super("^/pv/(\\d+)$", ActionSecurityType.LOGGED_IN);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long id = Long.parseLong(req.getRequestURI().substring(4));

		Objectify ofy = ObjectifyService.begin();
		PackageVersion r = ofy.get(new Key<PackageVersion>(
				PackageVersion.class, id));

		return new PackageVersionPage(r);
	}
}
