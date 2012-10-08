package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Delete a repository.
 */
public class RepDeleteAction extends SecureAction {
	@Override
	public Page securePerform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long id = Long.parseLong(req.getParameter("id"));
		Objectify ofy = ObjectifyService.begin();
		ofy.delete(new Key<Repository>(Repository.class, id));

		resp.sendRedirect("/rep");
		return null;
	}
}
