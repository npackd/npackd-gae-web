package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Creates a new repository.
 */
public class RepAddAction extends Action {
	/**
	 * -
	 */
	public RepAddAction() {
		super("^/rep/add$");
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Repository r = new Repository();
		r.name = req.getParameter("title");
		Objectify ofy = ObjectifyService.begin();
		ofy.put(r);

		resp.sendRedirect("/rep");
		return null;
	}
}
