package com.googlecode.npackdweb.pv;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * Packages.
 */
public class PackageVersionRecognizePage extends MyPage {
	/** download URL */
	public String url;

	/** error message or null */
	private String error;

	/**
	 * -
	 */
	public PackageVersionRecognizePage() {
		this.url = "";
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();

		if (error != null) {
			w.e("p", "class", "bg-danger", this.error);
		}

		w.start("form", "class", "form-horizontal", "method", "post", "action",
				"/package-version/recognize");
		w.start("div", "class", "btn-group");
		w.e("input", "class", "btn btn-default", "type", "submit", "title",
				"Recognize", "value", "Recognize", "id", "recognize");
		w.end("div");

		w.start("table", "id", "fields");

		w.start("tr");
		w.e("td", "Download:");
		w.start("td");
		w.e("input", "style", "display: inline; width: 90%", "class",
				"form-control", "type", "text", "name", "url", "value", url,
				"size", "120", "id", "url", "title",
				"http: or https: address of the package binary");
		w.e("div", "class", "glyphicon glyphicon-link", "id", "url-link",
				"style", "cursor: pointer; font-size: 20px; font-weight: bold");
		w.end("td");
		w.end("tr");

		w.end("table");

		w.end("form");

		return w.toString();
	}

	@Override
	public String getTitle() {
		return "Create a package version";
	}

	/**
	 * @return true if the data should be editable
	 */
	public boolean getEditable() {
		return UserServiceFactory.getUserService().getCurrentUser() != null;
	}

	public void fillForm(HttpServletRequest req) {
		url = req.getParameter("url");
	}

	@Override
	public String validate() {
		String r = null;
		if (r == null) {
			if (!this.url.trim().isEmpty()) {
				r = NWUtils.validateURL(this.url);
			}
		}

		return r;
	}

	/**
	 * @param error
	 *            new error message or null
	 */
	public void setErrorMessage(String error) {
		this.error = error;
	}
}
