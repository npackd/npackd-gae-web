package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * Default servlet for HTML pages.
 */
@SuppressWarnings("serial")
public class DefaultServlet extends HttpServlet {
	private List<Pattern> urlPatterns = new ArrayList<Pattern>();
	private List<Action> actions = new ArrayList<Action>();

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String pi = req.getRequestURI();
		// NWUtils.LOG.severe("getPathInfo(): " + pi);
		if (pi == null) {
			pi = "/";
		}

		Action found = null;
		for (int i = 0; i < urlPatterns.size(); i++) {
			Pattern p = urlPatterns.get(i);
			Matcher m = p.matcher(pi);
			if (m.matches()) {
				found = actions.get(i);
				break;
			}
		}

		if (found != null) {
			UserService us = UserServiceFactory.getUserService();
			boolean ok = true;
			switch (found.getSecurityType()) {
			case ANONYMOUS:
				break;
			case LOGGED_IN:
				if (us.getCurrentUser() == null) {
					ok = false;
					resp.sendRedirect(us.createLoginURL(req.getRequestURI()));
				}
				break;
			case ADMINISTRATOR:
				if (us.getCurrentUser() == null) {
					ok = false;
					resp.sendRedirect(us.createLoginURL(req.getRequestURI()));
				} else if (!us.isUserAdmin()) {
					ok = false;
					resp.setContentType("text/plain");
					resp.getWriter().write("Not an admin");
					resp.getWriter().close();
				}
				break;
			default:
				throw new InternalError("Unknown security type");
			}

			if (ok) {
				Page p = found.perform(req, resp);
				if (p != null) {
					p.create(req, resp);
				}
			}
		} else {
			throw new IOException("Unknown command: " + pi);
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		NWUtils.initObjectify();
		NWUtils.initFreeMarker(getServletContext());

		/* repository */
		registerAction(new RepDeleteAction());
		registerAction(new RepAddAction());
		registerAction(new RepUploadAction());
		registerAction(new RepFromFileAction());
		registerAction(new RepDetailAction());
		registerAction(new RepAction());
		registerAction(new RepXMLAction());

		/* package */
		registerAction(new PackagesAction());
		registerAction(new PackageDetailAction());
		registerAction(new PackageNewAction());
		registerAction(new PackageSaveAction());
		registerAction(new PackageDeleteAction());

		/* package version */
		registerAction(new PackageVersionDetailAction());
		registerAction(new PackageVersionNewAction());
		registerAction(new PackageVersionSaveAction());

		registerAction(new HomeAction());
	}

	/**
	 * Registers an action
	 * 
	 * @param action
	 *            registered action
	 */
	private void registerAction(Action action) {
		urlPatterns.add(Pattern.compile(action.getURLRegExp()));
		actions.add(action);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}