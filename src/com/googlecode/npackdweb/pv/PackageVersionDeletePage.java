package com.googlecode.npackdweb.pv;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * Delete package version confirmation.
 */
public class PackageVersionDeletePage extends MyPage {
	private PackageVersion pv;

	/**
	 * @param pv
	 *            this version should be deleted
	 */
	public PackageVersionDeletePage(PackageVersion pv) {
		this.pv = pv;
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();
		w.start("form", "method", "post", "action",
				"/package-version/delete-confirmed");
		w.e("div", "Do you really want to delete " + pv.package_ + " " +
				pv.version + "?");
		w.e("input", "type", "hidden", "name", "name", "value", pv.name);
		w.start("div", "class", "btn-group");
		w.e("input", "class", "btn btn-default", "type", "submit", "value",
				"Delete");
		w.e("input", "class", "btn btn-default", "type", "button", "value",
				"Cancel", "onclick", "window.location.href='/p/" + pv.package_ +
						"/" + pv.version + "'");
		w.end("div");
		w.end("form");
		return w.toString();
	}

	@Override
	public String getTitle() {
		return "Confirmation";
	}
}
