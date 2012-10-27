package com.googlecode.npackdweb.wlib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Always returns 404
 */
public class Error404Action extends Action {
	/**
	 * @param urlRegEx
	 *            regular expression for URLs. Example: "^/def$"
	 */
	public Error404Action(String urlRegEx) {
		super(urlRegEx);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.sendError(404);
		return null;
	}

}
