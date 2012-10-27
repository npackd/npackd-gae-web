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
public class PackageVersionDeleteConfirmedAction extends Action {
	/**
	 * -
	 */
	public PackageVersionDeleteConfirmedAction() {
		super("^/package-version/delete-confirmed$",
				ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String name = req.getParameter("name");
		Objectify ofy = ObjectifyService.begin();
		PackageVersion p = ofy.get(new Key<PackageVersion>(
				PackageVersion.class, name));
		ofy.delete(p);
		resp.sendRedirect("/p/" + p.package_);
		return null;
	}
}
