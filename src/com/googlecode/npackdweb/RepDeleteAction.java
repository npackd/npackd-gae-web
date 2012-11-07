package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Delete a repository.
 */
public class RepDeleteAction extends Action {
	/**
	 * -
	 */
	public RepDeleteAction() {
		super("^/rep/delete$");
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long id = Long.parseLong(req.getParameter("id"));
		Objectify ofy = NWUtils.getObjectify();
		ofy.delete(new Key<Repository>(Repository.class, id));
		DefaultServlet.dataVersion.incrementAndGet();

		resp.sendRedirect("/rep");
		return null;
	}
}
