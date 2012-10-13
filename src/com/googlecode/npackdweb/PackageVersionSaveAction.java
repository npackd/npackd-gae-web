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
public class PackageVersionSaveAction extends Action {
	/**
	 * -
	 */
	public PackageVersionSaveAction() {
		super("^/pv/save$", ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String id = req.getParameter("id");
		Objectify ofy = ObjectifyService.begin();
		PackageVersion p;
		if (id == null || id.trim().length() == 0) {
			p = new PackageVersion();
			p.name = req.getParameter("name");
		} else {
			long id_ = Long.parseLong(id);
			p = ofy.get(new Key<PackageVersion>(PackageVersion.class, id_));
			if (p == null)
				throw new IOException("Package version does not exist");
		}
		ofy.put(p);
		resp.sendRedirect("/pv");
		return null;
	}
}
