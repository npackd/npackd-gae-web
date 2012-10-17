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
		super("^/package-version/save$", ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String name = req.getParameter("name");
		Objectify ofy = ObjectifyService.begin();
		PackageVersion p;
		if (name == null || name.trim().length() == 0) {
			p = new PackageVersion();
			p.name = name;
		} else {
			p = ofy.get(new Key<PackageVersion>(PackageVersion.class, name));
			if (p == null)
				throw new IOException("Package version does not exist");
		}
		p.url = req.getParameter("url");
		p.sha1 = req.getParameter("sha1");
		p.detectMSI = req.getParameter("detectMSI");
		p.oneFile = "one-file".equals(req.getParameter("type"));
		ofy.put(p);
		resp.sendRedirect("/p/" + p.package_);
		return null;
	}
}
