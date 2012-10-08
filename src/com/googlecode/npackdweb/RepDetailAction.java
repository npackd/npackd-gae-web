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
 * Repository details
 */
public class RepDetailAction extends SecureAction {
	@Override
	public Page securePerform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long id = Long.parseLong(req.getRequestURI().substring(5));

		Objectify ofy = ObjectifyService.begin();
		Repository r = ofy.get(new Key<Repository>(Repository.class, id));

		User currentUser = UserServiceFactory.getUserService().getCurrentUser();
		if (!r.user.equals(currentUser))
			throw new IOException("Missing permission");

		return new RepDetailPage(r);
	}
}
