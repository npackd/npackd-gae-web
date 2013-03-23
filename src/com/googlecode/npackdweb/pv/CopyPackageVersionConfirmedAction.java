package com.googlecode.npackdweb.pv;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.PackageVersion;
import com.googlecode.npackdweb.Version;
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
		String name = req.getParameter("name").trim();
		String version = req.getParameter("version").trim();

		String err = null;
		try {
			Version version_ = Version.parse(version);
			version_.normalize();
			version = version_.toString();
		} catch (NumberFormatException e) {
			err = "Error parsing the version number: " + e.getMessage();
		}

		Objectify ofy = NWUtils.getObjectify();
		PackageVersion p = ofy.get(new Key<PackageVersion>(
		        PackageVersion.class, name));

		PackageVersion copyFound = ofy.find(new Key<PackageVersion>(
		        PackageVersion.class, p.package_ + "@" + version));
		if (copyFound != null)
			err = "This version already exists: " + p.package_ + " " + version;

		if (err != null) {
			return new CopyPackageVersionPage(p, err, req
			        .getParameter("version"));
		} else {
			PackageVersion copy = p.copy();
			copy.name = copy.package_ + "@" + version;
			copy.version = version;

			NWUtils.savePackageVersion(ofy, copy);
			resp.sendRedirect("/p/" + copy.package_ + "/" + copy.version);
			return null;
		}
	}
}
