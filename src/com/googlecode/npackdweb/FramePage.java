package com.googlecode.npackdweb;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A page with a frame.
 */
public abstract class FramePage extends Page {
	@Override
	public final void create(HttpServletRequest request,
			HttpServletResponse resp) throws IOException {
		resp.setContentType("text/html; charset=UTF-8");
		Writer out = resp.getWriter();
		out.write(NWUtils.tmpl("basic/Frame.html", "title", getTitle(),
				"content", createContent(request), "login", NWUtils
						.getLoginFooter(request)));
		out.close();
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
