package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * Home page.
 */
public class HomePage extends MyPage {
	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		return NWUtils.tmpl("Home.html");
	}

	@Override
	public String getTitle() {
		return "Npackd";
	}
}
