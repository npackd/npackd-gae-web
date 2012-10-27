package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Delete a package version.
 */
public class PackageVersionDeleteAction extends Action {
	/**
	 * -
	 */
	public PackageVersionDeleteAction() {
		super("^/package-version/delete$", ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String name = req.getParameter("name");
		Objectify ofy = ObjectifyService.begin();
		PackageVersion p = ofy.get(new Key<PackageVersion>(
				PackageVersion.class, name));
		return new PackageVersionDeletePage(p);
	}
}
