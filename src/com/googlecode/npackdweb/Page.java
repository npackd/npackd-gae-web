package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * An HTML page.
 */
public abstract class Page {
	/**
	 * Creates HTML
	 * 
	 * @return HTML
	 */
	public abstract String create(HttpServletRequest request)
			throws IOException;
}
