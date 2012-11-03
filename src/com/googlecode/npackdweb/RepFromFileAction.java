package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

/**
 * Form for a repository upload.
 */
public class RepFromFileAction extends Action {
	/**
	 * -
	 */
	public RepFromFileAction() {
		super("^/rep/from-file$", ActionSecurityType.EDITOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		return new RepFromFilePage();
	}
}
