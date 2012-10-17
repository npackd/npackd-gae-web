package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Packages.
 */
public class PackageVersionPage extends FramePage {
	private PackageVersion pv;
	private Package package_;
	private License license;

	/**
	 * @param pv
	 *            a package version
	 */
	public PackageVersionPage(PackageVersion pv) {
		this.pv = pv;
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		return NWUtils.tmpl(this, "PackageVersion.html");
	}

	@Override
	public String getTitle() {
		return "Package version";
	}

	/**
	 * @return associated package version
	 */
	public PackageVersion getPackageVersion() {
		return pv;
	}

	/**
	 * @return associated package
	 */
	public Package getPackage() {
		if (this.package_ == null) {
			Objectify objectify = ObjectifyService.begin();
			this.package_ = objectify.get(Package.class, this.pv.package_);
		}
		return this.package_;
	}

	/**
	 * @return true if the data should be editable
	 */
	public boolean getEditable() {
		UserService us = UserServiceFactory.getUserService();
		return us.isUserLoggedIn() && us.isUserAdmin();
	}

	/**
	 * @return associated license or null
	 */
	public License getLicense() {
		if (this.license == null) {
			Package p = getPackage();
			if (!p.license.isEmpty()) {
				Objectify ofy = ObjectifyService.begin();
				this.license = ofy.get(License.class, p.license);
			}
		}
		return license;
	}
}
