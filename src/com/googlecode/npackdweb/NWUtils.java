package com.googlecode.npackdweb;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Utilities
 */
public class NWUtils {
	/** Application log */
	public static final Logger LOG = Logger.getLogger(NWUtils.class.getName());

	private static final String GPL_LICENSE = "\n    This file is part of Npackd.\n"
			+ "    \n"
			+ "    Npackd is free software: you can redistribute it and/or modify\n"
			+ "    it under the terms of the GNU General Public License as published by\n"
			+ "    the Free Software Foundation, either version 3 of the License, or\n"
			+ "    (at your option) any later version.\n"
			+ "    \n"
			+ "    Npackd is distributed in the hope that it will be useful,\n"
			+ "    but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
			+ "    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
			+ "    GNU General Public License for more details.\n"
			+ "    \n"
			+ "    You should have received a copy of the GNU General Public License\n"
			+ "    along with Npackd.  If not, see <http://www.gnu.org/licenses/>.\n    ";

	public static final Object EDITOR_1 = "chevdor@gmail.com";

	private static Configuration cfg;

	private static boolean objectifyInitialized;

	/**
	 * Thread local Objectify instance.
	 */
	public static ThreadLocal<Objectify> OBJECTIFY = new ThreadLocal<Objectify>() {
		@Override
		protected Objectify initialValue() {
			return ObjectifyService.begin();
		}
	};

	/**
	 * Login/Logout-footer
	 * 
	 * @param request
	 *            HTTP request
	 * @return HTML
	 * @throws IOException
	 */
	public static String getLoginFooter(HttpServletRequest request)
			throws IOException {
		UserService userService = UserServiceFactory.getUserService();

		String thisURL = request.getRequestURI();
		if (request.getQueryString() != null)
			thisURL += "?" + request.getQueryString();
		String res;
		if (request.getUserPrincipal() != null) {
			res = NWUtils.tmpl("basic/LogoutFooter.html", NWUtils.newMap(
					"name", request.getUserPrincipal().getName(), "logoutURL",
					userService.createLogoutURL(thisURL)));
		} else {
			res = NWUtils.tmpl("basic/LoginFooter.html", NWUtils.newMap(
					"loginURL", userService.createLoginURL(thisURL)));
		}
		return res;
	}

	/**
	 * Initializes FreeMarker. This method can be called multiple times from
	 * many threads.
	 * 
	 * @param ctx
	 *            servlet context
	 * @return FreeMarker configuration
	 */
	public static synchronized Configuration initFreeMarker(ServletContext ctx) {
		if (cfg == null) {
			// Initialize the FreeMarker configuration;
			// - Create a configuration instance
			cfg = new Configuration();

			// - Templates are stored in the WEB-INF/templates directory of the
			// Web
			// app.
			cfg.setServletContextForTemplateLoading(ctx, "WEB-INF/templates");
		}
		return cfg;
	}

	/**
	 * Formats a template
	 * 
	 * @param templateName
	 *            name of the template file
	 * @param values
	 *            values for the template
	 * @return formatted text
	 * @throws IOException
	 *             if the template cannot be read
	 */
	public static String tmpl(String templateName, Map<String, Object> values)
			throws IOException {
		Template t = cfg.getTemplate(templateName);
		StringWriter sw = new StringWriter();
		try {
			t.process(values, sw);
		} catch (TemplateException e) {
			throw new IOException("Error while processing FreeMarker template",
					e);
		}
		return sw.getBuffer().toString();
	}

	/**
	 * Formats a template
	 * 
	 * @param templateName
	 *            name of the template file
	 * @param key
	 *            key for the template
	 * @param value
	 *            value for the template
	 * @return formatted text
	 * @throws IOException
	 *             if the template cannot be read
	 */
	public static String tmpl(String templateName, String key, String value)
			throws IOException {
		return tmpl(templateName, newMap(key, value));
	}

	/**
	 * Formats a template
	 * 
	 * @param templateName
	 *            name of the template file
	 * @param keysAndValues
	 *            key1, value1, key2, value2, ...
	 * @return formatted text
	 * @throws IOException
	 *             if the template cannot be read
	 */
	public static String tmpl(String templateName, String... keysAndValues)
			throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 0; i < keysAndValues.length; i += 2) {
			map.put(keysAndValues[i], keysAndValues[i + 1]);
		}
		return tmpl(templateName, map);
	}

	/**
	 * Formats a template
	 * 
	 * @param templateName
	 *            name of the template file
	 * @param keysAndValues
	 *            key1, value1, key2, value2, ...
	 * @return formatted text
	 * @throws IOException
	 *             if the template cannot be read
	 */
	public static String tmpl(Page page, String templateName,
			String... keysAndValues) throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 0; i < keysAndValues.length; i += 2) {
			map.put(keysAndValues[i], keysAndValues[i + 1]);
		}
		map.put("page", page);
		return tmpl(templateName, map);
	}

	/**
	 * Check if the user is logged in.
	 * 
	 * @param request
	 *            HTTP request
	 * @throws IOException
	 *             if the user is not logged in
	 */
	public static void checkLogin(HttpServletRequest request)
			throws IOException {
		if (request.getUserPrincipal() == null) {
			throw new IOException("Login required");
		}
	}

	/**
	 * Writes an HTML response
	 * 
	 * @param resp
	 *            response
	 * @param templateName
	 *            name of the template
	 * @param root
	 *            root object
	 * @throws IOException
	 *             if the template cannot be read
	 */
	public static void serve(HttpServletResponse resp, String templateName,
			Map<String, String> root) throws IOException {
		// Get the template object
		Template t = cfg.getTemplate(templateName);

		// Prepare the HTTP response:
		// - Use the char set of template for the output
		// - Use text/html MIME-type
		resp.setContentType("text/html; charset=" + t.getEncoding());
		Writer out = resp.getWriter();

		// Merge the data-model and the template
		try {
			t.process(root, out);
		} catch (TemplateException e) {
			throw new IOException("Error while processing FreeMarker template",
					e);
		}
	}

	/**
	 * Creates a new Map with only one value
	 * 
	 * @param key
	 *            key
	 * @param value
	 *            value
	 * @return map with the specified value
	 */
	public static Map<String, Object> newMap(String key, String value) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(key, value);
		return map;
	}

	/**
	 * Creates a new Map with 2 values
	 * 
	 * @param key
	 *            key
	 * @param value
	 *            value
	 * @param key2
	 *            second key
	 * @param value2
	 *            second value
	 * @return map with the specified values
	 */
	public static Map<String, Object> newMap(String key, String value,
			String key2, String value2) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(key, value);
		map.put(key2, value2);
		return map;
	}

	/**
	 * Initializes Objectify.
	 */
	public static synchronized void initObjectify() {
		if (!objectifyInitialized) {
			ObjectifyService.register(Repository.class);
			ObjectifyService.register(Package.class);
			ObjectifyService.register(PackageVersion.class);
			ObjectifyService.register(License.class);
			objectifyInitialized = true;
		}
	}

	/**
	 * Returns the content of a sub-tag.
	 * 
	 * @param tag
	 *            an XML tag
	 * @param subtag
	 *            name of the sub-tag
	 * @param def
	 *            this value is used if the sub-tag is missing
	 * @return the content of the sub-tag
	 */
	public static String getSubTagContent(Element tag, String subtag, String def) {
		NodeList nl = tag.getElementsByTagName(subtag);
		String r = def;
		if (nl.getLength() > 0) {
			Element a = (Element) nl.item(0);
			Node first = a.getFirstChild();
			if (first != null)
				r = first.getNodeValue();
		}
		return r;
	}

	/**
	 * @param che
	 *            a tag
	 * @return text content of the tag
	 */
	public static String getTagContent_(Element che) {
		if (che.getFirstChild() != null)
			return che.getFirstChild().getNodeValue();
		else
			return "";
	}

	/**
	 * Adds a sub-tag with the specified text.
	 * 
	 * @param parent
	 *            parent tag
	 * @param subtag
	 *            sub-tag
	 * @param content
	 *            content of the sub-tag
	 */
	public static void e(Element parent, String subtag, String content) {
		Document d = parent.getOwnerDocument();
		Element ch = d.createElement(subtag);
		Text t = d.createTextNode(content);
		ch.appendChild(t);
		parent.appendChild(ch);
	}

	/**
	 * Adds text to the specified tag.
	 * 
	 * @param tag
	 * @param txt
	 */
	public static void t(Element tag, String txt) {
		Text t = tag.getOwnerDocument().createTextNode(txt);
		tag.appendChild(t);
	}

	/**
	 * Joins the strings with the specified delimiter.
	 * 
	 * @param del
	 *            delimiter
	 * @param txts
	 *            strings
	 * @return joined text
	 */
	public static String join(String del, List<String> txts) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < txts.size(); i++) {
			if (i != 0)
				sb.append(del);
			sb.append(txts.get(i));
		}
		return sb.toString();
	}

	/**
	 * Splits the text on the specified separator
	 * 
	 * @param txt
	 *            a text
	 * @param separator
	 *            separator character
	 * @return parts
	 */
	public static List<String> split(String txt, char separator) {
		List<String> r = new ArrayList<String>();
		while (true) {
			txt = txt.trim();
			int p = txt.indexOf(separator);
			if (p < 0) {
				if (!txt.isEmpty())
					r.add(txt);
				break;
			} else {
				String before = txt.substring(0, p).trim();
				txt = txt.substring(p + 1);
				if (!before.isEmpty())
					r.add(before);
			}
		}

		return r;
	}

	/**
	 * Splits lines.
	 * 
	 * @param txt
	 *            a multiline text
	 * @return text lines
	 */
	public static List<String> splitLines(String txt) {
		BufferedReader br = new BufferedReader(new StringReader(txt));
		List<String> r = new ArrayList<String>();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				r.add(line);
			}
		} catch (IOException e) {
			throw (InternalError) new InternalError(e.getMessage())
					.initCause(e);
		}
		return r;
	}

	/**
	 * @return empty XML document
	 */
	public static Document newXML() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument();
		} catch (ParserConfigurationException e) {
			throw (InternalError) new InternalError(e.getMessage())
					.initCause(e);
		}
	}

	/**
	 * @param gpl
	 *            true = add GPL license
	 * @return empty repository
	 */
	public static Document newXMLRepository(boolean gpl) {
		Document d = NWUtils.newXML();

		Element root = d.createElement("root");
		d.appendChild(root);
		if (gpl) {
			Comment comment = d.createComment(GPL_LICENSE);
			// root.getParentNode().insertBefore(root, comment);
			root.appendChild(comment);
		}

		NWUtils.e(root, "spec-version", "2");
		return d;
	}

	/**
	 * Converts XML to a string
	 * 
	 * @param xml
	 *            XML
	 * @return string
	 * @throws IOException
	 */
	public static String toString(Document xml) throws IOException {
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
					"4");
			t.setOutputProperty("{http://xml.apache.org/xalan}line-separator",
					"\r\n");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			t.transform(new DOMSource(xml.getDocumentElement()),
					new StreamResult(baos));
			baos.close();

			return baos.toString("UTF-8");
		} catch (Exception e) {
			throw (IOException) new IOException(e.getMessage()).initCause(e);
		}
	}

	/**
	 * Creates an <input type="button"> that changes window.location.href
	 * 
	 * @param w
	 *            HTML output
	 * @param title
	 *            button title
	 * @param url
	 *            new URL
	 */
	public static void jsButton(HTMLWriter w, String title, String url) {
		w.e("input", "class", "input", "type", "button", "value", title,
				"onclick", "window.location.href='" + url + "'");
	}

	/**
	 * @return the number of packages
	 */
	public static int countPackages() {
		final String key = NWUtils.class.getName() + ".countPackages@"
				+ DefaultServlet.dataVersion.get();
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers
				.getConsistentLogAndContinue(Level.INFO));
		Integer value = (Integer) syncCache.get(key); // read from cache
		if (value == null) {
			Objectify ofy = NWUtils.OBJECTIFY.get();
			value = ofy.query(Package.class).count();
			syncCache.put(key, value); // populate cache
		}
		return value;
	}

	/**
	 * @return true if a user is logged in that can edit packages
	 */
	public static boolean isEditorLoggedIn() {
		UserService us = UserServiceFactory.getUserService();
		User u = us.getCurrentUser();
		return us.isUserLoggedIn()
				&& (us.isUserAdmin() || u.getEmail().equals(NWUtils.EDITOR_1));
	}
}
