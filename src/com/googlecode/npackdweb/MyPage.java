package com.googlecode.npackdweb;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Page;

/**
 * A page with a frame.
 */
public abstract class MyPage extends Page {
	@Override
	public final void create(HttpServletRequest request,
	        HttpServletResponse resp) throws IOException {
		resp.setContentType("text/html; charset=UTF-8");
		Writer out = resp.getWriter();

		String thisURL = request.getRequestURI();
		if (request.getQueryString() != null)
			thisURL += "?" + request.getQueryString();

		out.write(NWUtils.tmpl("Frame.html", "title", getTitle(), "content",
		        createContent(request), "login", NWUtils
		                .getLoginFooter(request), "head", getHeadPart()));
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

	/**
	 * @return HTML code that should be inserted in <head>
	 */
	public String getHeadPart() {
		return "";
	}
}
