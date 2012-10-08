package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An action.
 */
public abstract class Action {
	/**
	 * Performs the action
	 * 
	 * @param req
	 *            request
	 * @param resp
	 *            response
	 * @return page to write or null if a sendRedirect() was used
	 * @throws IOException
	 */
	public abstract Page perform(HttpServletRequest req,
			HttpServletResponse resp) throws IOException;
}
