package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Creates a new repository.
 */
public class RepAddAction extends SecureAction {
	@Override
	public Page securePerform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Repository r = new Repository();
		r.name = req.getParameter("title");
		r.user = UserServiceFactory.getUserService().getCurrentUser();
		Objectify ofy = ObjectifyService.begin();
		ofy.put(r);

		resp.sendRedirect("/rep");
		return null;
	}
}
