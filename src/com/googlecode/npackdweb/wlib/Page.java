package com.googlecode.npackdweb.wlib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An HTML page.
 */
public abstract class Page {
	/**
	 * Creates the content of a page
	 * 
	 * @param request
	 *            HTTP request
	 * @param resp
	 *            HTTP response
	 * @throws IOException
	 */
	public abstract void create(HttpServletRequest request,
			HttpServletResponse resp) throws IOException;
}
