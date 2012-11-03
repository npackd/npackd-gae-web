package com.googlecode.npackdweb.package_;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.Package;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Deletes a package
 */
public class PackageDeleteAction extends Action {
	/**
	 * -
	 */
	public PackageDeleteAction() {
		super("^/package/delete$", ActionSecurityType.EDITOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String id = req.getParameter("id");
		Objectify ofy = ObjectifyService.begin();
		Package p = ofy.get(new Key<Package>(Package.class, id));

		return new PackageDeletePage(p);
	}
}
