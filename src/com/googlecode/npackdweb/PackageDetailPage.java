package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * A package.
 */
public class PackageDetailPage extends FramePage {
	private Package r;
	private List<PackageVersion> versions;

	/**
	 * @param r
	 *            a package or null
	 */
	public PackageDetailPage(Package r) {
		this.r = r;

		Objectify ofy = ObjectifyService.begin();
		versions = new ArrayList<PackageVersion>();
		if (r != null) {
			for (PackageVersion p : ofy.query(PackageVersion.class).filter(
					"package_ =", r.name).fetch())
				versions.add(p);
		}
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		return NWUtils.tmpl(this, "PackageDetail.html");
	}

	@Override
	public String getTitle() {
		return "Package";
	}

	/**
	 * @return package shown on this page or null
	 */
	public Package getPackage() {
		return r;
	}

	/**
	 * @return versions of this package
	 */
	public List<PackageVersion> getVersions() {
		return versions;
	}
}
