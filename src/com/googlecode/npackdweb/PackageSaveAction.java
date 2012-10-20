package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Save or create a package.
 */
public class PackageSaveAction extends Action {
	/**
	 * -
	 */
	public PackageSaveAction() {
		super("^/package/save$", ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String name = req.getParameter("name");
		Objectify ofy = ObjectifyService.begin();
		Package p;
		if (name == null || name.trim().length() == 0) {
			p = new Package(name);
		} else {
			p = ofy.find(new Key<Package>(Package.class, name));
			if (p == null)
				p = new Package(name);
		}
		p.description = req.getParameter("description");
		p.icon = req.getParameter("icon");
		p.title = req.getParameter("title");
		p.url = req.getParameter("url");
		p.license = req.getParameter("license");
		p.comment = req.getParameter("comment");
		ofy.put(p);
		resp.sendRedirect("/p");
		return null;
	}
}
