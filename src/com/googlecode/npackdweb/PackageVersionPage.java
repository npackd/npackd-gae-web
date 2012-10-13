package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * Packages.
 */
public class PackageVersionPage extends FramePage {
	private PackageVersion pv;

	/**
	 * @param pv
	 *            a package version or null
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

	public String getName() {
		return pv == null ? "" : pv.name;
	}
}
