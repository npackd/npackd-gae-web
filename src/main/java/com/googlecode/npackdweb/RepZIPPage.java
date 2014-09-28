package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;

/**
 * ZIP for a repository.
 */
public class RepZIPPage extends Page {
	private final String tag;

	/**
	 * @param tag
	 *            only package versions with this tag will be exported.
	 */
	public RepZIPPage(String tag) {
		this.tag = tag;
	}

	@Override
	public void create(HttpServletRequest request, HttpServletResponse resp)
			throws IOException {
		Objectify ofy = DefaultServlet.getObjectify();
		ExportRepsAction.export(ofy, tag, false);

		NWUtils.serveFileFromGCS(tag + ".zip", request, resp, "application/zip");
	}
}
