package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Start a copy of a package version.
 */
public class CopyPackageVersionConfirmedAction extends Action {
	/**
	 * -
	 */
	public CopyPackageVersionConfirmedAction() {
		super("^/package-version/copy-confirmed$", ActionSecurityType.EDITOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String name = req.getParameter("name");
		String version = req.getParameter("version");
		Objectify ofy = NWUtils.OBJECTIFY.get();
		PackageVersion p = ofy.get(new Key<PackageVersion>(
				PackageVersion.class, name));
		PackageVersion copy = p.copy();
		copy.name = copy.package_ + "@" + version;
		copy.version = version;
		copy.sha1 = "";
		copy.detectFileSHA1s.clear();
		copy.detectFilePaths.clear();
		copy.detectMSI = "";
		ofy.put(copy);
		resp.sendRedirect("/p/" + copy.package_ + "/" + copy.version);
		return null;
	}
}
