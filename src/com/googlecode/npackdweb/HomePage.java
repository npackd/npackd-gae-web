package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * Home page.
 */
public class HomePage extends MyPage {
	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();
		w.start("a", "href",
				"http://code.google.com/p/windows-package-manager/");
		w.e("img", "src", "/Npackd-1.15.6.png", "style", "padding: 1em");
		w.end("a");
		w.start("div");
		w.e("img", "src", "/Logo.png");
		w.t(" Npackd (pronounced \"unpacked\") is an application "
				+ "store/package manager/marketplace for applications for "
				+ "Windows. It helps you to find and install software, "
				+ "keep your system up-to-date and uninstall it if no longer "
				+ "necessary. You can watch this short video to better "
				+ "understand how it works. The process of installing and "
				+ "uninstalling applications is completely automated "
				+ "(silent or unattended installation and un-installation). "
				+ "There is also a command line based version of Npackd.");
		w.end("div");

		return w.toString();
	}

	@Override
	public String getTitle() {
		return "Npackd";
	}
}
