package com.googlecode.npackdweb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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

	private static Configuration cfg;

	private static boolean objectifyInitialized;

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
	 * Splits the text on ","
	 * 
	 * @param txt
	 *            a text
	 * @return parts
	 */
	public static List<String> split(String txt) {
		List<String> r = new ArrayList<String>();
		while (true) {
			txt = txt.trim();
			int p = txt.indexOf(',');
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
}
