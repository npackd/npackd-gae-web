package com.googlecode.npackdweb.admin;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;

/**
 * Re-saves all packages.
 */
public class ResavePackagesAction extends Action {
	/**
	 * -
	 */
	public ResavePackagesAction() {
		super("^/resave-packages$", ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Objectify ofy = DefaultServlet.getObjectify();
		List<Package> q = ofy.query(Package.class).list();
		ofy.put(q);
		return new MessagePage("The packages were successfully re-saved");
	}
}
