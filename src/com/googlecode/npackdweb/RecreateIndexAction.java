package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

/**
 * Recreates the index with packages.
 */
public class RecreateIndexAction extends Action {
	/**
	 * -
	 */
	public RecreateIndexAction() {
		super("^/recreate-index$", ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
	        throws IOException {
		NWUtils.recreateIndex();
		return new MessagePage("The index was successfully re-created");
	}
}
