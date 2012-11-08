package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * List of repositories.
 */
public class RepPage extends MyPage {
	private static final String[] TITLES = { "Stable", "Stable 64 bit", "Libs",
	        "Unstable" };
	private static final String[] NAMES = { "stable", "stable64", "libs",
	        "unstable" };

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter b = new HTMLWriter();

		b.t("These repositories are re-created daily:");
		b.start("ul");
		for (int i = 0; i < TITLES.length; i++) {
			b.start("li");
			b.e("a", "href", "/rep/xml?tag=" + NAMES[i], TITLES[i]);
			b.end("li");
		}
		b.end("ul");

		b.e("br");
		b
		        .t("This repository contains 20 last changed package versions and should be used for testing only: ");
		b.e("a", "href", "/rep/recent-xml",
		        "20 recently modified package versions");

		return b.toString();
	}

	@Override
	public String getTitle() {
		return "Repositories";
	}
}
