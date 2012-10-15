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
	private License license;
	private boolean editable;
	private List<License> licenses;

	/**
	 * @param r
	 *            a package or null
	 * @param editable
	 *            true if the data should be editable
	 */
	public PackageDetailPage(Package r, boolean editable) {
		this.r = r;
		this.editable = editable;

		Objectify ofy = ObjectifyService.begin();
		versions = new ArrayList<PackageVersion>();
		if (r != null) {
			for (PackageVersion p : ofy.query(PackageVersion.class).filter(
					"package_ =", r.name).fetch())
				versions.add(p);

			if (!r.license.isEmpty())
				this.license = ofy.get(License.class, r.license);
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

	/**
	 * @return license of the package or null
	 */
	public License getLicense() {
		return license;
	}

	/**
	 * @return true if the data should be editable
	 */
	public boolean getEditable() {
		return editable;
	}

	/**
	 * @return list of all licenses
	 */
	public List<License> getLicenses() {
		if (this.licenses == null) {
			Objectify ofy = ObjectifyService.begin();
			this.licenses = new ArrayList<License>();
			for (License p : ofy.query(License.class).fetch())
				this.licenses.add(p);
		}
		return licenses;
	}
}
