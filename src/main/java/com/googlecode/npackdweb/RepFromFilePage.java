package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * Form for a repository upload.
 */
public class RepFromFilePage extends MyPage {
	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();
		w.start("form", "action", "/rep/upload", "enctype",
				"multipart/form-data", "method", "POST");
		w.t("Repository: ");
		w.e("input", "class", "form-control", "type", "file", "name",
				"repository");
		w.e("br");
		w.t("Please use one of the repository names to place the package versions there");
		w.e("br");
		w.t("Tag for package versions: ");
		w.e("input", "class", "form-control", "type", "text", "name", "tag");
		w.e("br");

		w.t("Overwrite:");
		w.e("input", "type", "checkbox", "name", "overwrite");
		w.t("If this checkbox is not selected, only new packages, package versions and licenses will be created");
		w.e("br");

		w.e("input", "class", "btn btn-default", "type", "submit", "value",
				"submit");
		w.e("br");
		w.end("form");

		return w.toString();
	}

	@Override
	public String getTitle() {
		return "Upload repository";
	}
}
