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
				"wrap", "off", "style", "width: 100%", NWUtils
						.toString(this.xml));
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "Tag:");
		w.start("td");
		w.e("input", "type", "text", "name", "tag", "value", tag);
		w.end("td");
		w.end("tr");
		w.end("table");
		w.e("input", "type", "submit", "value", "submit");
		w.end("form");

		return w.toString();
	}

	@Override
	public String getTitle() {
		return "Upload repository";
	}
}
