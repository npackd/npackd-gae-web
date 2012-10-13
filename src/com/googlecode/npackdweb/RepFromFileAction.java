package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Form for a repository upload.
 */
public class RepFromFileAction extends Action {
	/**
	 * -
	 */
	public RepFromFileAction() {
		super("^/rep/from-file$", ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		return new RepFromFilePage();
	}
}
