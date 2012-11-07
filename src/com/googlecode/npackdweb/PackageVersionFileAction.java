package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * A package version.
 */
public class PackageVersionFileAction extends Action {
	/**
	 * -
	 */
	public PackageVersionFileAction() {
		super("^/p/([^/]+)/([\\d.]+)/file$", ActionSecurityType.ANONYMOUS);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Pattern p = Pattern.compile(getURLRegExp());
		Matcher m = p.matcher(req.getRequestURI());
		m.matches();
		String package_ = m.group(1);
		String version = m.group(2);

		Objectify ofy = NWUtils.getObjectify();
		PackageVersion r = ofy.get(new Key<PackageVersion>(
				PackageVersion.class, package_ + "@" + version));

		String path = req.getParameter("path");

		return new PackageVersionFilePage(r, path);
	}
}
