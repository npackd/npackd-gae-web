package com.googlecode.npackdweb.package_;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.Package;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Deletes a package
 */
public class PackageDeleteAction extends Action {
	/**
	 * -
	 */
	public PackageDeleteAction() {
		super("^/p/delete$");
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long id = Long.parseLong(req.getParameter("id"));
		Objectify ofy = ObjectifyService.begin();
		ofy.delete(new Key<Package>(Package.class, id));

		resp.sendRedirect("/p");
		return null;
	}
}
