package com.googlecode.npackdweb.wlib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Always returns 404
 */
public class SendStatusAction extends Action {
	private int status;

	/**
	 * @param urlRegEx
	 *            regular expression for URLs. Example: "^/def$"
	 */
	public SendStatusAction(String urlRegEx, int status) {
		super(urlRegEx, ActionSecurityType.ANONYMOUS);
		this.status = status;
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		if (this.status / 100 == 2)
			resp.setStatus(this.status);
		else
			resp.sendError(status);
		return null;
	}
}
