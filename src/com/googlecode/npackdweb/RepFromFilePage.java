package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * Form for a repository upload.
 */
public class RepFromFilePage extends FramePage {
	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();
		w.e("h2", "Upload a file");
		w.start("form", "action", "/rep/upload", "enctype",
				"multipart/form-data", "method", "POST");
		w.t("Repository: ");
		w.e("input", "type", "file", "name", "repository");
		w.e("br");
		w.t("Tag: ");
		w.e("input", "type", "text", "name", "tag");
		w.e("br");
		w.e("input", "type", "submit", "value", "submit");
		w.e("br");
		w.end("form");

		w.e("br");

		w.e("h2", "Paste the text below");
		w.start("form", "action", "/rep/upload", "method", "POST");
		w.t("Repository: ");
		w.e("textarea", "rows", "20", "cols", "120", "name", "repository");
		w.e("br");
		w.t("Tag: ");
		w.e("input", "type", "text", "name", "tag");
		w.e("br");
		w.e("input", "type", "submit", "value", "submit");
		w.e("br");
		w.end("form");

		return w.toString();
	}

	@Override
	public String getTitle() {
		return "Upload repository";
	}
}
