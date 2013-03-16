package com.googlecode.npackdweb.pv;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Delete a package version.
 */
public class PackageVersionDeleteConfirmedAction extends Action {
	/**
	 * -
	 */
	public PackageVersionDeleteConfirmedAction() {
		super("^/package-version/delete-confirmed$", ActionSecurityType.EDITOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
	        throws IOException {
		String name = req.getParameter("name");
		Objectify ofy = NWUtils.getObjectify();
		PackageVersion p = ofy.get(new Key<PackageVersion>(
		        PackageVersion.class, name));
		ofy.delete(p);
		NWUtils.incDataVersion();
		resp.sendRedirect("/p/" + p.package_);
		return null;
	}
}
