package com.googlecode.npackdweb.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * Page where permissions to all packages can be granted to a user.
 */
public class AddPermissionsPage extends MyPage {
	/** email of a user */
	public String email = "";

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();

		w.start("form", "method", "post", "action",
				"/add-permissions-confirmed");
		w.t("User email: ");
		w.e("input", "type", "text", "name", "email", "value", email);
		w.e("br");
		w.e("input", "type", "submit");
		w.end("form");

		return w.toString();
	}

	@Override
	public String getTitle() {
		return "Add permissions for a user to all packages";
	}

	@Override
	public String validate() {
		return NWUtils.validateEmail(email);
	}

	@Override
	public void fill(HttpServletRequest req) {
		this.email = req.getParameter("email");
	}
}
