package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Deletes a package
 */
public class PackageDeleteAction extends SecureAction {
	@Override
	public Page securePerform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long id = Long.parseLong(req.getParameter("id"));
		Objectify ofy = ObjectifyService.begin();
		ofy.delete(new Key<Package>(Package.class, id));

		resp.sendRedirect("/p");
		return null;
	}
}
