package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

/**
 * Licenses
 */
public class LicensesAction extends Action {
	/**
	 * -
	 */
	public LicensesAction() {
		super("^/l$", ActionSecurityType.ANONYMOUS);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String start_ = req.getParameter("start");
		if (start_ == null)
			start_ = "0";
		int start;
		try {
			start = Integer.parseInt(start_);
		} catch (NumberFormatException e) {
			start = 0;
		}

		return new LicensesPage(start);
	}
}
