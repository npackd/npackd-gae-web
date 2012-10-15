package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Creates XML for a whole repository definition.
 */
public class RepXMLAction extends Action {
	/**
	 * -
	 */
	public RepXMLAction() {
		super("^/rep/xml$", ActionSecurityType.ANONYMOUS);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		return new RepXMLPage(req.getParameter("tag"));
	}
}
