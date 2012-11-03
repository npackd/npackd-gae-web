package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * Shows a message
 */
public class MessagePage extends MyPage {
	private String msg;

	/**
	 * @param msg
	 *            message to be shown
	 */
	public MessagePage(String msg) {
		this.msg = msg;
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		return msg;
	}

	@Override
	public String getTitle() {
		return "Message";
	}
}
