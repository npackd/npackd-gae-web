package com.googlecode.npackdweb.pv;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.PackageVersion;
import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * Copy a package version.
 */
public class CopyPackageVersionPage extends MyPage {
	private PackageVersion pv;

	/**
	 * -
	 * 
	 * @param p
	 *            package version that should be copied
	 */
	public CopyPackageVersionPage(PackageVersion p) {
		this.pv = p;
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();
		w.start("form", "method", "post", "action",
				"/package-version/copy-confirmed");
		w.e("div", "Copy " + pv.package_ + " " + pv.version + "?");
		w.e("input", "type", "hidden", "name", "name", "value", pv.name);
		w.e("input", "type", "text", "name", "version", "value", pv.version,
				"size", "20");
		w.e("br");
		w.e("input", "class", "input", "type", "submit", "value", "Create");
		w.e("input", "class", "input", "type", "button", "value", "Cancel",
				"onclick", "window.location.href='/p/" + pv.package_ + "/"
						+ pv.version + "'");
		w.end("form");
		return w.toString();
	}

	@Override
	public String getTitle() {
		return "Copy package version";
	}
}
