package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Repository details
 */
public class RepDetailAction extends Action {
	/**
	 * -
	 */
	public RepDetailAction() {
		super("^/rep/(\\d+)$", ActionSecurityType.LOGGED_IN);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long id = Long.parseLong(req.getRequestURI().substring(5));

		Objectify ofy = ObjectifyService.begin();
		Repository r = ofy.get(new Key<Repository>(Repository.class, id));

		return new RepDetailPage(r);
	}
}