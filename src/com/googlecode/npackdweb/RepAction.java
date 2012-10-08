package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * /rep
 */
public class RepAction extends SecureAction {
	@Override
	public Page securePerform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		return new RepPage();
	}
}
