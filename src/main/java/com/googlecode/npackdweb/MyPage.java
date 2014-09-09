package com.googlecode.npackdweb;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.npackdweb.wlib.Page;

/**
 * A page with a frame.
 */
public abstract class MyPage extends Page {
	/** error message shown at the top of the page or null */
	public String error;

	/** informational message or null */
	public String info;

	@Override
	public final void create(HttpServletRequest request,
			HttpServletResponse resp) throws IOException {
		resp.setContentType("text/html; charset=UTF-8");
		Writer out = resp.getWriter();

		out.write(NWUtils.tmpl("Frame.html", "title", getTitle(), "titleHTML",
				getTitleHTML(), "content", createContent(request), "head",
				getHeadPart(), "menu", createMenu(request), "error", error,
				"info", info, "generator", this.getClass().getName(),
				"bodyBottom", createBodyBottom(request)));
		out.close();
	}

	/**
	 * Creates HTML to be inserted before </body>
	 * 
	 * @return HTML
	 */
	public String createBodyBottom(HttpServletRequest request)
			throws IOException {
		return "";
	}

	/**
	 * Creates HTML without the header and the footer.
	 * 
	 * @return HTML
	 */
	public abstract String createContent(HttpServletRequest request)
			throws IOException;

	/**
	 * @return page title
	 */
	public abstract String getTitle();

	/**
	 * @return page title as HTML. The default implementation just converts the
	 *         return value of {@link #getTitle()} to HTML
	 */
	public String getTitleHTML() {
		HTMLWriter w = new HTMLWriter();
		w.t(getTitle());
		return w.toString();
	}

	/**
	 * @return HTML code that should be inserted in <head>
	 */
	public String getHeadPart() {
		return "";
	}

	/**
	 * Validates the data.
	 * 
	 * @return error message or null
	 */
	public String validate() {
		return null;
	}

	/**
	 * Fills the fields from HTTP parameters (e.g. a <form>).
	 * 
	 * @param req
	 *            HTTP request
	 */
	public void fill(HttpServletRequest req) {
	}

	/**
	 * @return true = create the search form in the menu
	 */
	public boolean needsSearchFormInTheMenu() {
		return true;
	}

	private String createMenu(HttpServletRequest request) {
		try {
			return NWUtils.tmpl("Menu.html", "admin",
					NWUtils.isAdminLoggedIn() ? "true" : null, "login",
					MyPage.getLoginHeader(request), "searchForm",
					needsSearchFormInTheMenu() ? "true" : null);
		} catch (IOException e) {
			throw (InternalError) new InternalError(e.getMessage())
					.initCause(e);
		}
	}

	/**
	 * Login/Logout-footer
	 * 
	 * @param request
	 *            HTTP request
	 * @return HTML
	 * @throws IOException
	 */
	private static String getLoginHeader(HttpServletRequest request)
			throws IOException {
		UserService userService = UserServiceFactory.getUserService();

		String thisURL = request.getRequestURI();
		if (request.getQueryString() != null)
			thisURL += "?" + request.getQueryString();
		HTMLWriter res = new HTMLWriter();
		if (request.getUserPrincipal() != null) {
			res.t("Hello, " + request.getUserPrincipal().getName() +
					"!  You can ");
			res.e("a", "href", userService.createLogoutURL(thisURL), "sign out");
			res.t(".");
		} else {
			res.e("a", "href", userService.createLoginURL(thisURL), "Log on");
		}
		return res.toString();
	}
}
