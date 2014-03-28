package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;

import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * Shows XML for a repository.
 */
public class EditAsXMLPage extends MyPage {
	private Document xml;
	private String tag;

	/**
	 * @param xml
	 *            XML for a repository
	 * @param tag
	 *            tag that should be assigned to all package versions
	 */
	public EditAsXMLPage(Document xml, String tag) {
		this.xml = xml;
		this.tag = tag;
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();
		w.start("form", "action", "/rep/upload", "method", "POST");
		w.start("table", "style", "width: 100%");
		w.start("tr");
		w.e("td", "Repository:");
		w.start("td");
		w.e("textarea", "rows", "20", "cols", "120", "name", "repository",
				"wrap", "off", "style", "width: 100%",
				NWUtils.toString(this.xml));
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "Tag for package versions:");
		w.start("td");
		w.e("input", "type", "text", "name", "tag", "value", tag);
		w.t("Please use one of these default tags to place the package versions in the right repository: stable, stable64, unstable, libs");
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "Overwrite:");
		w.start("td");
		w.e("input", "type", "checkbox", "name", "overwrite");
		w.t("If this checkbox is not selected, only new packages, package versions and licenses will be created");
		w.end("td");
		w.end("tr");

		w.end("table");
		w.e("input", "type", "submit", "class", "btn btn-default", "value",
				"submit");
		w.end("form");

		return w.toString();
	}

	@Override
	public String getTitle() {
		return "Upload repository";
	}
}
