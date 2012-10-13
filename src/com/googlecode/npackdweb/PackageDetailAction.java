package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * A package.
 */
public class PackageDetailAction extends Action {
	/**
	 * -
	 */
	public PackageDetailAction() {
		super("^/p/(.+)$", ActionSecurityType.LOGGED_IN);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String name = req.getRequestURI().substring(3);

		Objectify ofy = ObjectifyService.begin();
		Package r = ofy.get(new Key<Package>(Package.class, name));

		return new PackageDetailPage(r);
	}
}
