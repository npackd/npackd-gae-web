package com.googlecode.npackdweb.pv;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * Copy a package version.
 */
public class CopyPackageVersionPage extends MyPage {
	private PackageVersion pv;
	private String error;
	private String newVersion;

	/**
	 * -
	 * 
	 * @param p
	 *            package version that should be copied
	 */
	public CopyPackageVersionPage(PackageVersion p) {
		this.pv = p;
		this.newVersion = p.version;
	}

	/**
	 * -
	 * 
	 * @param p
	 *            package version that should be copied
	 * @param error
	 *            error message or null
	 * @param newVersion
	 *            new version
	 */
	public CopyPackageVersionPage(PackageVersion p, String error,
			String newVersion) {
		this.pv = p;
		this.error = error;
		this.newVersion = newVersion;
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();
		if (error != null) {
			w.e("p", "class", "bg-danger", this.error);
		}
		w.start("form", "class", "form-horizontal", "method", "post", "action",
				"/package-version/copy-confirmed");
		w.e("div", "Copy " + pv.package_ + " " + pv.version + "?");
		w.e("input", "type", "hidden", "name", "name", "value", pv.name);
		w.e("input", "type", "text", "name", "version", "value", newVersion,
				"size", "20");
		w.e("br");

		w.start("div", "class", "btn-group");
		w.e("input", "class", "btn btn-default", "type", "submit", "value",
				"Create");
		w.e("input", "class", "btn btn-default", "type", "button", "value",
				"Cancel", "onclick", "window.location.href='/p/" + pv.package_ +
						"/" + pv.version + "'");
		w.end("div");

		w.end("form");
		return w.toString();
	}

	@Override
	public String getTitle() {
		return "Copy package version";
	}
}
