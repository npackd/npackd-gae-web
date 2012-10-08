package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * A page with a frame.
 */
public abstract class FramePage extends Page {
	@Override
	public final String create(HttpServletRequest request) throws IOException {
		return NWUtils.tmpl("basic/Frame.html", "title", getTitle(), "content",
				createContent(request), "login", NWUtils
						.getLoginFooter(request));
	}

	/**
	 * Creates HTML without the header and the footer.
	 * 
	 * @return HTML
	 */
	public abstract String createContent(HttpServletRequest request)
			throws IOException;

	/**
	 * @return page title
	 */
	public abstract String getTitle();
}
