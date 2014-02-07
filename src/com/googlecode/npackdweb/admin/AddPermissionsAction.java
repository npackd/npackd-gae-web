package com.googlecode.npackdweb.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

/**
 * Shows a page where permissions to all packages could be granted to a user.
 */
public class AddPermissionsAction extends Action {
	/**
	 * -
	 */
	public AddPermissionsAction() {
		super("^/add-permissions$", ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		return new AddPermissionsPage();
	}
}
