package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * List of repositories.
 */
public class RepPage extends FramePage {
	private static final String[] TITLES = { "Stable", "Stable 64 bit", "Libs",
			"Unstable" };
	private static final String[] NAMES = { "stable", "stable64", "libs",
			"unstable" };

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter b = new HTMLWriter();
		b.start("ul");

		for (int i = 0; i < TITLES.length; i++) {
			b.start("li");
			b.e("a", "href", "/rep/xml?tag=" + NAMES[i], TITLES[i]);
			b.end("li");
		}
		b.end("ul");

		return b.toString();
	}

	@Override
	public String getTitle() {
		return "Repositories";
	}
}
