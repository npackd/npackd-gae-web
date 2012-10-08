package com.googlecode.npackdweb;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
			Page p = found.perform(req, resp);
			if (p != null) {
				String html = p.create(req);
				resp.setContentType("text/html; charset=UTF-8");
				Writer out = resp.getWriter();
				out.write(html);
				out.close();
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

		urlPatterns.add(Pattern.compile("^/rep/delete$"));
		actions.add(new RepDeleteAction());

		urlPatterns.add(Pattern.compile("^/rep/add$"));
		actions.add(new RepAddAction());

		urlPatterns.add(Pattern.compile("^/rep/(\\d+)$"));
		actions.add(new RepDetailAction());

		urlPatterns.add(Pattern.compile("^/rep$"));
		actions.add(new RepAction());

		urlPatterns.add(Pattern.compile("^/p$"));
		actions.add(new PackagesAction());

		urlPatterns.add(Pattern.compile("^/p/(\\d+)$"));
		actions.add(new PackageDetailAction());

		urlPatterns.add(Pattern.compile("^/p/new$"));
		actions.add(new PackageNewAction());

		urlPatterns.add(Pattern.compile("^/p/save$"));
		actions.add(new PackageSaveAction());

		urlPatterns.add(Pattern.compile("^/p/delete$"));
		actions.add(new PackageDeleteAction());

		urlPatterns.add(Pattern.compile("^/$"));
		actions.add(new HomeAction());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}