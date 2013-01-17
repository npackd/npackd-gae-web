package com.googlecode.npackdweb.package_;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;

/**
 * Delete a package version.
 */
public class PackageDeleteConfirmedAction extends Action {
	/**
	 * -
	 */
	public PackageDeleteConfirmedAction() {
		super("^/package/delete-confirmed$", ActionSecurityType.EDITOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
	        throws IOException {
		String name = req.getParameter("name");
		Objectify ofy = NWUtils.getObjectify();
		NWUtils.deletePackage(ofy, name);
		resp.sendRedirect("/p");
		return null;
	}
}
