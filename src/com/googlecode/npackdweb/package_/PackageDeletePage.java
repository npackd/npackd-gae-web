package com.googlecode.npackdweb.package_;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.Package;
import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * Delete package version confirmation.
 */
public class PackageDeletePage extends MyPage {
	private Package pv;

	/**
	 * @param pv
	 *            this version should be deleted
	 */
	public PackageDeletePage(Package pv) {
		this.pv = pv;
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();
		w
				.start("form", "method", "post", "action",
						"/package/delete-confirmed");
		w.e("div", "Do you really want to delete " + pv.name + " " + "?");
		w.e("input", "type", "hidden", "name", "name", "value", pv.name);
		w.e("input", "class", "input", "type", "submit", "value", "Delete");
		w.e("input", "class", "input", "type", "button", "value", "Cancel",
				"onclick", "window.location.href='/p/" + pv.name + "'");
		w.end("form");
		return w.toString();
	}

	@Override
	public String getTitle() {
		return "Confirmation";
	}
}
