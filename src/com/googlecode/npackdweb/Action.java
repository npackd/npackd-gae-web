package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An action.
 */
public abstract class Action {
	private ActionSecurityType securityType = ActionSecurityType.ANONYMOUS;
	private String urlRegEx;

	/**
	 * @param urlRegEx
	 *            regular expression for URLs. Example: "^/def$"
	 */
	public Action(String urlRegEx) {
		this.urlRegEx = urlRegEx;
	}

	/**
	 * @param urlRegEx
	 *            regular expression for URLs. Example: "^/def$"
	 * @param st
	 *            permission to call this action
	 */
	public Action(String urlRegEx, ActionSecurityType st) {
		this.urlRegEx = urlRegEx;
		this.securityType = st;
	}

	/**
	 * @return regular expression for URLs. Example: "^/def$"
	 */
	public String getURLRegExp() {
		return urlRegEx;
	}

	/**
	 * Performs the action
	 * 
	 * @param req
	 *            request
	 * @param resp
	 *            response
	 * @return page to write or null if a sendRedirect() was used
	 * @throws IOException
	 */
	public abstract Page perform(HttpServletRequest req,
			HttpServletResponse resp) throws IOException;

	/**
	 * @return permission to call this action
	 */
	public ActionSecurityType getSecurityType() {
		return securityType;
	}

	/**
	 * @param t
	 *            permission to call this action
	 */
	public void setSecurityType(ActionSecurityType t) {
		this.securityType = t;
	}
}
