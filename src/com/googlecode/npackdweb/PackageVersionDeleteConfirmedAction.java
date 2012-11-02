package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;
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
		super("^/package-version/delete-confirmed$");
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String name = req.getParameter("name");
		Objectify ofy = ObjectifyService.begin();
		PackageVersion p = ofy.get(new Key<PackageVersion>(
				PackageVersion.class, name));
		ofy.delete(p);
		DefaultServlet.dataVersion.incrementAndGet();
		resp.sendRedirect("/p/" + p.package_);
		return null;
	}
}