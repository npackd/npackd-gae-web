package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Action for /
 */
public class HomeAction extends Action {
	/**
	 * -
	 */
	public HomeAction() {
		super("^/$");
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		return new HomePage();
	}
}
