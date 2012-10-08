package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Only allows the logged in users to call the action.
 */
public abstract class SecureAction extends Action {
	@Override
	public final Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		NWUtils.checkLogin(req);
		return securePerform(req, resp);
	}

	/**
	 * Secured action.
	 * 
	 * @param req
	 *            request
	 * @param resp
	 *            response
	 * @return create Page or null if sendRedirect() was called
	 * @throws IOException
	 */
	public abstract Page securePerform(HttpServletRequest req,
			HttpServletResponse resp) throws IOException;
}
