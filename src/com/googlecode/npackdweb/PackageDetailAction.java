package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * A package.
 */
public class PackageDetailAction extends SecureAction {
	@Override
	public Page securePerform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long id = Long.parseLong(req.getRequestURI().substring(3));

		Objectify ofy = ObjectifyService.begin();
		Package r = ofy.get(new Key<Package>(Package.class, id));

		User currentUser = UserServiceFactory.getUserService().getCurrentUser();
		if (!r.createdBy.equals(currentUser))
			throw new IOException("Missing permission");

		return new PackageDetailPage(r);
	}
}
