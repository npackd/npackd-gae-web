package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * /rep
 */
public class RepAction extends Action {
	/**
	 * -
	 */
	public RepAction() {
		super("^/rep$", ActionSecurityType.ANONYMOUS);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		return new RepPage();
	}
}
