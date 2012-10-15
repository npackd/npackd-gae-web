package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * List of repositories.
 */
public class RepPage extends FramePage {
	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		return NWUtils.tmpl(this, "Rep.html");
	}

	@Override
	public String getTitle() {
		return "Repositories";
	}
}
