package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * Form for a repository upload.
 */
public class RepFromFilePage extends FramePage {
	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		return NWUtils.tmpl(this, "RepFromFile.html");
	}

	@Override
	public String getTitle() {
		return "Upload repository";
	}
}
