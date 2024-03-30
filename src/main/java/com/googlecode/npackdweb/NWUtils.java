package com.googlecode.npackdweb;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.urlfetch.*;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.cloudstorage.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.googlecode.npackdweb.db.DatastoreCache;
import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.db.Version;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.npackdweb.wlib.Page;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.*;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities
 */
public class NWUtils {
    private final static String[] INVALID_WINDOWS_FILE_NAMES =
            {"..", "CON", "PRN", "AUX", "NUL",
                    "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7",
                    "COM8",
                    "COM9",
                    "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7",
                    "LPT8",
                    "LPT9"};
    private final static Set<String> INVALID_WINDOW_FILE_NAMES_SET =
            new HashSet<>();

    static {
        Collections.addAll(INVALID_WINDOW_FILE_NAMES_SET,
                INVALID_WINDOWS_FILE_NAMES);
    }

    /**
     * gcsService.getMetadata takes about 16ms (28.12.2019). It is implemented
     * as HTTP HEAD.
     */
    private static final LoadingCache<String, GcsFileMetadata> METADATA_CACHE =
            CacheBuilder.
                    newBuilder()
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build(
                            new CacheLoader<String, GcsFileMetadata>() {
                                @Override
                                public GcsFileMetadata load(String filename)
                                        throws
                                        Exception {
                                    String tag = filename.substring(0,
                                            filename.length() - 4);

                                    final GcsService gcsService =
                                            GcsServiceFactory.createGcsService(
                                                    RetryParams
                                                            .getDefaultInstance());

                                    GcsFilename f = new GcsFilename("npackd",
                                            filename);
                                    GcsFileMetadata md = gcsService.
                                            getMetadata(f);
                                    if (md == null) {
                                        ExportRepAction.export(gcsService, tag,
                                                false);
                                        md = gcsService.getMetadata(f);
                                    }

                                    return md;
                                }
                            });

    /**
     * URL
     */
    public static final String WEB_SITE = "https://www.npackd.org";

    /**
     * My email.
     */
    public static final String THE_EMAIL = "tim.lebedkov@gmail.com";

    /**
     * Application log
     */
    public static final Logger LOG = Logger.getLogger(NWUtils.class.getName());

    /**
     * Datastore cache
     */
    public static DatastoreCache dsCache = new DatastoreCache();

    private static final String GPL_LICENSE =
            "\n    This file is part of Npackd.\n" +
                    "    \n" +
                    "    Npackd is free software: you can redistribute it and/or modify\n" +

                    "    it under the terms of the GNU General Public License as published by\n" +

                    "    the Free Software Foundation, either version 3 of the License, or\n" +
                    "    (at your option) any later version.\n" +
                    "    \n" +
                    "    Npackd is distributed in the hope that it will be useful,\n" +
                    "    but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +

                    "    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                    "    GNU General Public License for more details.\n" +
                    "    \n" +
                    "    You should have received a copy of the GNU General Public License\n" +

                    "    along with Npackd.  If not, see <http://www.gnu.org/licenses/>.\n    ";

    private static Configuration cfg;

    private static Configuration safeCfg;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String ID_LETTERS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Initializes FreeMarker. This method can be called multiple times from
     * many threads.
     *
     * @param ctx servlet context
     */
    public static synchronized void initFreeMarker(ServletContext ctx) {
        if (cfg == null) {
            // Initialize the FreeMarker configuration;
            // - Create a configuration instance
            cfg = new Configuration();

            // - Templates are stored in the WEB-INF/templates directory of the
            // Web
            // app.
            cfg.setServletContextForTemplateLoading(ctx, "WEB-INF/templates");
        }
        if (safeCfg == null) {
            // Initialize the FreeMarker configuration;
            // - Create a configuration instance
            safeCfg = new Configuration();
        }
    }

    /**
     * Formats a template.
     *
     * @param template content of the template
     * @param values keys and values for the template
     * @return formatted text
     */
    public static String tmplString(String template,
                                    Map<String, String> values) {
        String r = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            r = r.replace(entry.getKey(), entry.getValue());
        }
        return r;
    }

    /**
     * Formats a template
     *
     * @param templateName name of the template file
     * @param values values for the template
     * @return formatted text
     * @throws IOException if the template cannot be read
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
     * @param templateName name of the template file under WEB-INF/templates
     * @param keysAndValues key1, value1, key2, value2, ...
     * @return formatted text
     * @throws IOException if the template cannot be read
     */
    public static String tmpl(String templateName, String... keysAndValues)
            throws IOException {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return tmpl(templateName, map);
    }

    /**
     * Formats a template
     *
     * @param page the object will be stored using the name "page"
     * @param templateName name of the template file
     * @param keysAndValues key1, value1, key2, value2, ...
     * @return formatted text
     * @throws IOException if the template cannot be read
     */
    public static String tmpl(Page page, String templateName,
                              String... keysAndValues) throws IOException {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        map.put("page", page);
        return tmpl(templateName, map);
    }

    /**
     * Writes an HTML response
     *
     * @param resp response
     * @param templateName name of the template
     * @param root root object
     * @throws IOException if the template cannot be read
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
     * Creates a new Map with 2 values
     *
     * @param key key
     * @param value value
     * @param key2 second key
     * @param value2 second value
     * @return map with the specified values
     */
    public static Map<String, Object> newMap(String key, String value,
                                             String key2, String value2) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        map.put(key2, value2);
        return map;
    }

    /**
     * Returns the content of a sub-tag.
     *
     * @param tag an XML tag
     * @param subtag name of the sub-tag
     * @param def this value is used if the sub-tag is missing
     * @return the content of the sub-tag
     */
    public static String
    getSubTagContent(Element tag, String subtag, String def) {
        NodeList nl = tag.getElementsByTagName(subtag);
        String r = def;
        if (nl.getLength() > 0) {
            Element a = (Element) nl.item(0);
            Node first = a.getFirstChild();
            if (first != null) {
                r = first.getNodeValue();
            }
        }
        return r;
    }

    /**
     * @param che a tag
     * @return text content of the tag
     */
    public static String getTagContent_(Element che) {
        if (che.getFirstChild() != null) {
            return che.getFirstChild().getNodeValue();
        } else {
            return "";
        }
    }

    /**
     * Adds a sub-tag with the specified text.
     *
     * @param parent parent tag
     * @param subtag sub-tag
     * @param content content of the sub-tag
     */
    public static void e(Element parent, String subtag, String content) {
        Document d = parent.getOwnerDocument();
        Element ch = d.createElement(subtag);
        Text t = d.createTextNode(content);
        ch.appendChild(t);
        parent.appendChild(ch);
    }

    /**
     * Adds a sub-tag with the specified text and an attribute.
     *
     * @param parent parent tag
     * @param subtag sub-tag
     * @param attr attribute name
     * @param attrValue attribute value
     * @param content content of the sub-tag or null
     */
    public static void e(Element parent, String subtag, String attr,
                         String attrValue, String content) {
        Document d = parent.getOwnerDocument();
        Element ch = d.createElement(subtag);
        ch.setAttribute(attr, attrValue);
        if (content != null) {
            Text t = d.createTextNode(content);
            ch.appendChild(t);
        }
        parent.appendChild(ch);
    }

    /**
     * Adds a sub-tag with the specified text and an attribute.
     *
     * @param parent parent tag
     * @param subtag sub-tag
     * @param attr attribute name
     * @param attrValue attribute value
     * @param attr2 second attribute name
     * @param attrValue2 second attribute value
     * @param content content of the sub-tag or null
     */
    public static void e(Element parent, String subtag, String attr,
                         String attrValue, String attr2, String attrValue2,
                         String content) {
        Document d = parent.getOwnerDocument();
        Element ch = d.createElement(subtag);
        ch.setAttribute(attr, attrValue);
        ch.setAttribute(attr2, attrValue2);
        if (content != null) {
            Text t = d.createTextNode(content);
            ch.appendChild(t);
        }
        parent.appendChild(ch);
    }

    /**
     * Adds text to the specified tag.
     */
    public static void t(Element tag, String txt) {
        Text t = tag.getOwnerDocument().createTextNode(txt);
        tag.appendChild(t);
    }

    /**
     * Joins the strings with the specified delimiter.
     *
     * @param del delimiter
     * @param txts strings
     * @return joined text
     */
    public static String join(String del, List<String> txts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < txts.size(); i++) {
            if (i != 0) {
                sb.append(del);
            }
            sb.append(txts.get(i));
        }
        return sb.toString();
    }

    /**
     * Splits the text on the specified separator
     *
     * @param txt a text
     * @param separator separator character
     * @return parts
     */
    public static List<String> split(String txt, char separator) {
        List<String> r = new ArrayList<>();
        while (true) {
            txt = txt.trim();
            int p = txt.indexOf(separator);
            if (p < 0) {
                if (!txt.isEmpty()) {
                    r.add(txt);
                }
                break;
            } else {
                String before = txt.substring(0, p).trim();
                txt = txt.substring(p + 1);
                if (!before.isEmpty()) {
                    r.add(before);
                }
            }
        }

        return r;
    }

    /**
     * Splits lines.
     *
     * @param txt a multiline text
     * @return text lines
     */
    public static List<String> splitLines(String txt) {
        BufferedReader br = new BufferedReader(new StringReader(txt));
        List<String> r = new ArrayList<>();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                r.add(line);
            }
        } catch (IOException e) {
            throw new InternalError(e.getMessage());
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
            throw new InternalError(e.getMessage());
        }
    }

    /**
     * @param gpl true = add GPL license
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

        NWUtils.e(root, "spec-version", "3.5");
        return d;
    }

    /**
     * Converts XML to a string
     *
     * @param xml XML
     * @return string
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
            throw new IOException(e);
        }
    }

    /**
     * Creates an &lt;input type="button"&gt; that changes window.location.href
     *
     * @param w HTML output
     * @param txt button title
     * @param url new URL
     * @param title tooltip
     */
    public static void jsButton(HTMLWriter w, String txt, String url,
                                String title) {
        w.e("input", "class", "btn btn-default", "type", "button", "value",
                txt, "onclick", "window.location.href='" + url + "'", "title",
                title);
    }

    /**
     * Creates a text field for URL input.
     *
     * @param w HTML output
     * @param name input name
     * @param value value of the input field
     * @param title title
     */
    public static void inputURL(HTMLWriter w, String name, String value,
                                String title) {
        w.start("div", "class", "nw-input-url");
        w.e("input", "style",
                "display: inline; width: 90%", "class",
                "form-control",
                "type", "url", "name", name, "value", value,
                "size", "120", "title", title);
        w.e("span", "class", "glyphicon glyphicon-link",
                "style", "cursor: pointer; font-size: 20px; font-weight: bold");
        w.end("div");
    }

    /**
     * Creates an &lt;input type="button"&gt; that changes window.location.href
     *
     * @param w HTML output
     * @param txt button title
     * @param onClick JavaScript for "onclick"
     * @param title tooltip
     */
    public static void jsButton_(HTMLWriter w, String txt, String onClick,
                                 String title) {
        w.e("input", "class", "btn btn-default", "type", "button", "value",
                txt, "onclick", onClick, "title", title);
    }

    /**
     * Creates an &lt;input type="button"&gt; that changes window.location.href
     *
     * @param w HTML output
     * @param txt button title
     * @param url new URL
     * @param title tooltip
     * @param enabled true = the button is enabled
     */
    public static void jsButton(HTMLWriter w, String txt, String url,
                                String title, boolean enabled) {
        w.e("input", "class", "btn btn-default", "type", "button", "value",
                txt, "onclick", "window.location.href='" + url + "'", "title",
                title, "disabled", enabled ? null : "disabled");
    }

    /**
     * @return true if an admin is logged in
     */
    public static boolean isAdminLoggedIn() {
        UserService us = UserServiceFactory.getUserService();
        return us.isUserLoggedIn() && us.isUserAdmin();
    }

    /**
     * Serializes XML
     *
     * @param d XML document
     * @param gos output
     * @throws java.io.IOException any error
     */
    public static void serializeXML(Document d, OutputStream gos)
            throws IOException {
        Transformer t;
        try {
            t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                    "1");
            t.setOutputProperty("{http://xml.apache.org/xalan}line-separator",
                    "\r\n");
            t.transform(new DOMSource(d.getDocumentElement()),
                    new StreamResult(gos));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Converts an array of bytes into its hex representation.
     *
     * @param b data
     * @return "abc46758"
     */
    public static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    /**
     * @return index for packages
     */
    public static Index getIndex() {
        IndexSpec spec = IndexSpec.newBuilder().setName("Packages").build();
        return SearchServiceFactory.getSearchService().getIndex(spec);
    }

    /**
     * Compares 2 users.
     *
     * @param a first user
     * @param b second user
     * @return true if the users are equal
     */
    public static boolean isEqual(User a, User b) {
        return isEmailEqual(a.getEmail(), b.getEmail());
    }

    /**
     * Compares 2 email addresses.
     *
     * @param a first email address
     * @param b second email address
     * @return true if the addresses are equal
     */
    public static boolean isEmailEqual(String a, String b) {
        return a.equalsIgnoreCase(b);
    }

    /**
     * Encodes URL parameters.
     * http://stackoverflow.com/questions/724043/http-url
     * -address-encoding-in-java
     *
     * @param input value of an URL parameter
     * @return encoded value
     */
    public static String encode(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch) {
        if (ch > 128) {
            return true;
        }
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }

    /**
     * Creates a "select" tag.
     *
     * @param id Id of the element
     * @param name name of the element
     * @param value selected value or null
     * @param titles titles
     * @param values values
     * @return HTML
     */
    public static String createSelect(String id, String name, String value,
                                      List<String> titles,
                                      List<String> values) {
        HTMLWriter w = new HTMLWriter();
        w.start("select", "class", "form-control", "name", name, "id", id);
        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i);
            String v = values.get(i);
            w.e("option", "value", v, "selected",
                    v.equals(value) ? "selected" : null, title);
        }
        w.end("select");
        return w.toString();
    }

    /**
     * Validates an URL. Only http: and https: are allowed as protocol.
     *
     * @param url_ the entered text
     * @param copyright true = check that servers like "softpedia.com" are not
     * used
     * @return error message or null
     */
    public static String validateURL(String url_, boolean copyright) {
        String msg = null;
        try {
            URL url = new URL(url_.trim());
            String p = url.getProtocol();
            if (!p.equals("http") && !p.equals("https")) {
                msg = "Invalid protocol: " + p;
            }
            if (msg == null && url.getHost().isEmpty()) {
                msg = "Empty host: " + url_;
            }
            if (msg == null && copyright) {
                String host = url.getHost().toLowerCase();
                if (host.endsWith("softpedia.com")) {
                    msg = "Cannot use this host for copyright reasons: " + url;
                }
            }
        } catch (MalformedURLException e) {
            msg = e.getMessage();
        }
        return msg;
    }

    /**
     * Validates an SHA1 checksum.
     *
     * @param sha1 text entered by the user
     * @return error message or null
     */
    public static String validateSHA1(String sha1) {
        if (sha1.length() != 40) {
            return "Wrong length: " + sha1;
        } else {
            for (int i = 0; i < sha1.length(); i++) {
                char c = sha1.charAt(i);
                if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >=
                        'A' && c <= 'F'))) {
                    return "Wrong character at position " + (i + 1) + " in " +
                            sha1;
                }
            }
        }

        return null;
    }

    /**
     * Validates an SHA-256 checksum.
     *
     * @param sha text entered by the user
     * @return error message or null
     */
    public static String validateSHA256(String sha) {
        if (sha.length() != 64) {
            return "Wrong length: " + sha;
        } else {
            for (int i = 0; i < sha.length(); i++) {
                char c = sha.charAt(i);
                if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >=
                        'A' && c <= 'F'))) {
                    return "Wrong character at position " + (i + 1) + " in " +
                            sha;
                }
            }
        }

        return null;
    }

    /**
     * Validates a GUID
     *
     * @param guid text entered by the user
     * @return error message or null
     */
    public static String validateGUID(String guid) {
        String err = null;
        if (guid.length() != 38) {
            err = "A GUID must be 38 characters long";
        } else {
            for (int i = 0; i < guid.length(); i++) {
                char c = guid.charAt(i);
                boolean valid;
                if (i == 9 || i == 14 || i == 19 || i == 24) {
                    valid = c == '-';
                } else if (i == 0) {
                    valid = c == '{';
                } else if (i == 37) {
                    valid = c == '}';
                } else {
                    valid =
                            (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') ||
                                    (c >= 'A' && c <= 'F');
                }

                if (!valid) {
                    err = "Wrong character at position " + (i + 1);
                    break;
                }
            }
        }
        return err;
    }

    /**
     * Validates the name of an environment variable.
     *
     * @param n text entered by the user
     * @return error message or null
     */
    public static String validateEnvVarName(String n) {
        String err = null;

        // TODO: not yet complete
        if (n.contains(" ")) {
            err = "Environment variable names cannot contain spaces";
        }
        return err;
    }

    /**
     * Validates an email
     *
     * @param email text entered
     * @return error message or null
     */
    public static String validateEmail(String email) {
        String result = null;
        if (email.indexOf('@') < 0) {
            result = "Missing the @ character";
        }
        if (result == null) {
            try {
                InternetAddress emailAddr = new InternetAddress(email);
                emailAddr.validate();
            } catch (AddressException ex) {
                result = ex.getMessage();
            }
        }
        return result;
    }

    /**
     * Validates a relative file path used e.g. for "important-file"
     *
     * @param path relative path
     * @return error message or null
     */
    public static String validateRelativePath(String path) {
        String result = null;

        if (path.isEmpty()) {
            result = "Empty path";
        }

        if (result == null) {
            final char[] INVALID_CHARS = {'<', '>', ':', '"', '|', '?', '*'};
            for (int i = 0; i < INVALID_CHARS.length; i++) {
                if (path.indexOf(INVALID_CHARS[i]) >= 0) {
                    result = "Invalid character: " + INVALID_CHARS[i];
                    break;
                }
            }
        }

        if (result == null) {
            for (int i = 0; i < path.length(); i++) {
                char c = path.charAt(i);
                if (c < ' ') {
                    result = "Invalid character: #" + Integer.toString(c);
                    break;
                }
            }
        }

        if (result == null) {
            List<String> parts = NWUtils.split(path.replace('/', '\\'), '\\');
            for (int i = 0; i < parts.size(); i++) {
                String part = parts.get(i).toUpperCase();
                if (INVALID_WINDOW_FILE_NAMES_SET.contains(part)) {
                    result = "Invalid name part: " + part;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Parse a more relaxed version number.
     *
     * @param version version number
     * @return parsed and normalized version number
     */
    public static Version parseVersion(String version)
            throws NumberFormatException {
        version = version.replace('-', '.');
        version = version.replace('+', '.');
        version = version.replace('_', '.');

        // process version numbers like 2.0.6b
        if (!version.isEmpty()) {
            char c =
                    Character.toLowerCase(version.charAt(version.length() - 1));
            if (c >= 'a' && c <= 'z') {
                version = version.substring(0, version.length() - 1) + "." +
                        (c - 'a' + 1);
            }
        }

        Version v = Version.parse(version);
        v.normalize();

        return v;
    }

    /**
     * Sends an email to the administrator.
     *
     * @param body body of the email
     * @param to receiver email address
     */
    public static void sendMailTo(String body, String to) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(THE_EMAIL, "Admin"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(WEB_SITE);
            msg.setText(body);
            Transport.send(msg);

            LOG.log(Level.INFO, "Sent a mail to {0}, Body: {1}",
                    new Object[]{to, body});
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot send a mail to " + to, e);
        }
    }

    /**
     * Sends an email to the administrator.
     *
     * @param string body of the email
     */
    public static void sendMailToAdmin(String string) {
        sendMailTo(string, "admins");
    }

    /**
     * As "new Date()", but sets the hours to 22.
     *
     * @return new Date object
     */
    public static Date newDate() {
        Date d = new Date();
        if (isAdminLoggedIn()) {
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            c.set(Calendar.HOUR_OF_DAY, 22);
            d = c.getTime();
        }
        return d;
    }

    /**
     * @param filename "tag.xml" or "tag.zip"
     * @return Google Srorage file metadata cached for 10 minutes
     * @throws ExecutionException error accessing the service
     */
    public static GcsFileMetadata getMetadata(String filename) throws
            ExecutionException {
        return METADATA_CACHE.get(filename);
    }

    /**
     * @return 00:00 today
     */
    public static Date newDay() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * Analyze the text via a Lucene analyzer.
     *
     * @param txt any English text
     * @return analyzed text for searching
     */
    public static String analyzeText(String txt) {
        MyEnglishAnalyzer a = new MyEnglishAnalyzer();
        TokenStream ts = a.tokenStream("text", txt);
        CharTermAttribute charTermAttribute =
                ts.addAttribute(CharTermAttribute.class);
        StringBuilder sb = new StringBuilder();
        try {
            ts.reset();
            while (ts.incrementToken()) {
                String term = charTermAttribute.toString();
                if (sb.length() != 0) {
                    sb.append(' ');
                }
                sb.append(term);
            }
            ts.end();
            ts.close();
            //NWUtils.LOG.warning("Analyzed " + txt + ": " + sb.toString());
            return sb.toString();
        } catch (IOException ex) {
            NWUtils.LOG.log(Level.WARNING, "Analyzing " + txt, ex);
            return txt;
        }
    }

    /**
     * Information about a downloaded file
     */
    public static class Info {

        /**
         * computed SHA1
         */
        public byte[] sha1;

        /**
         * size of the file
         */
        public long size;
    }

    /**
     * Computes SHA-256 for a string.
     *
     * @param value a string
     * @return SHA-256 of the UTF-8 encoding of the supplied value
     */
    public static byte[] stringSHA256(String value) {
        MessageDigest crypt;
        try {
            crypt = MessageDigest.getInstance("SHA-256");
            crypt.update(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IOError(ex);
        }

        return crypt.digest();
    }

    /**
     * Downloads a file
     *
     * @param url http: or https: URL
     * @param algorithm SHA-1 or SHA-256
     * @param maxSize maximum size of the file or 0 for "unlimited". If the file
     * is bigger than the specified size, the download will be cancelled and an
     * IOException will be thrown
     * @return info about the downloaded file
     * @throws IOException file cannot be downloaded
     * @throws NoSuchAlgorithmException if SHA1 cannot be computed
     */
    public static Info download(String url, String algorithm, long maxSize)
            throws IOException, NoSuchAlgorithmException {
        Info info = new Info();
        MessageDigest crypt = MessageDigest.getInstance(algorithm);

        URL u = new URL(url);
        URLFetchService s = URLFetchServiceFactory.getURLFetchService();

        long startPosition = 0;

        // limit 32 MiB:
        // https://developers.google.com/appengine/docs/java/urlfetch/
        long segment = 30 * 1024 * 1024;
        while (true) {
            HTTPRequest ht = new HTTPRequest(u);
            ht.setHeader(new HTTPHeader("User-Agent",
                    "NpackdWeb/1 (compatible; MSIE 9.0)"));
            ht.getFetchOptions().setDeadline(10 * 60.0);
            ht.setHeader(new HTTPHeader("Range", "bytes=" + startPosition +
                    "-" + (startPosition + segment - 1)));
            HTTPResponse r = s.fetch(ht);
            if (r.getResponseCode() == 416) {
                if (startPosition == 0) {
                    throw new IOException(
                            "Empty response with HTTP error code 416");
                } else {
                    break;
                }
            }

            byte[] content = r.getContent();
            if (r.getResponseCode() != 206 && r.getResponseCode() != 200) {
                throw new IOException("HTTP response code: " +
                        r.getResponseCode());
            }
            crypt.update(content);

            startPosition += segment;
            info.size += content.length;

            if (maxSize > 0 && info.size > maxSize) {
                throw new IOException("Maximum download size of " + maxSize +
                        " exceeded");
            }

            if (content.length < segment || r.getResponseCode() == 200) {
                break;
            }
        }

        info.sha1 = crypt.digest();
        return info;
    }

    private static void copyData(InputStream in, OutputStream out) throws
            IOException {
        byte[] buffer = new byte[8 * 1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }

    /**
     * Downloads a file. This method ignores protocol switches http <-> https.
     * HTTPURLConnection cannot handle this by default. URLFetchService only can
     * download 32 MiB
     *
     * @param url URL
     * @param os the data will be stored here.
     * @throws IOException failure
     */
    private static void upload(URL url, OutputStream os)
            throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);

        for (int i = 0; i < 5; i++) {
            conn.setRequestProperty("User-Agent",
                    "NpackdWeb/1 (compatible; MSIE 9.0)");

            boolean redirect = false;

            // normally, 3xx is redirect
            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                        status == HttpURLConnection.HTTP_MOVED_PERM ||
                        status == HttpURLConnection.HTTP_SEE_OTHER) {
                    redirect = true;
                }
            }

            // System.out.println("Response Code ... " + status);
            if (redirect) {
                // get redirect url from "location" header field
                String newUrl = conn.getHeaderField("Location");

                // get the cookie if need, for login
                String cookies = conn.getHeaderField("Set-Cookie");

                // open the new connnection again
                conn = (HttpURLConnection) new URL(newUrl).openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setRequestProperty("Cookie", cookies);

                // System.out.println("Redirect to URL : " + newUrl);
            } else {
                InputStream is = conn.getInputStream();
                copyData(is, os);
                is.close();
                os.close();
                return;
            }
        }

        throw new IOException("There were too many redirects.");
    }

    /**
     * Upload a file to archive.org. See https://archive.org/help/abouts3.txt
     *
     * @param url http: or https: URL
     * @param archiveURL URL on s3.us.archive.org
     * @param accessKey access key for s3.us.archive.org
     * @param password password on archive.org
     * @throws IOException file cannot be downloaded
     */
    public static void archive(String url, String archiveURL, String accessKey,
                               String password)
            throws IOException {
        HttpURLConnection archive = (HttpURLConnection) new URL(archiveURL).
                openConnection();
        archive.setDoOutput(true);
        archive.setRequestMethod("PUT");
        archive.setRequestProperty("User-Agent",
                "NpackdWeb/1 (compatible; MSIE 9.0)");
        archive.setRequestProperty("Authorization",
                "LOW " + accessKey + ":" + password);
        archive.setRequestProperty("x-amz-auto-make-bucket",
                "1");
        archive.setRequestProperty("x-archive-meta01-collection",
                "open_source_software");
        archive.setRequestProperty("x-archive-meta-title",
                "File");
        archive.setInstanceFollowRedirects(true);
        upload(new URL(url), archive.getOutputStream());
        if (archive.getResponseCode() / 100 != 2) {
            throw new IOException("Archive.org request failed: " + archive.
                    getResponseCode());
        }
    }

    /**
     * Changes an email address so it cannot be easily parsed.
     *
     * @param email an email address or null
     * @param domain server domain
     * @return HTML. abc dot def at bla dot com
     */
    public static String obfuscateEmail(String email, String domain) {
        if (email == null) {
            return "unknown";
        } else {
            HTMLWriter w = new HTMLWriter();
            int index = email.indexOf('@');
            if (index > 0) {
                Editor e =
                        NWUtils.dsCache.findEditor(NWUtils.email2user(email));
                if (e == null) {
                    e = new Editor(email2user(email));
                    NWUtils.dsCache.saveEditor(e);
                }
                String before = email.substring(0, index);
                if (before.length() > 10) {
                    before = before.substring(0, 10) + "...";
                }

                // only https is supported
                w.start("a", "href", "https://" + domain + "/recaptcha?id=" +
                        e.id);
                w.t(before);
                w.end("a");
            } else {
                w.t(email);
            }
            return w.toString();
        }
    }

    /**
     * @return secure ID
     */
    public static String generateSecureId() {
        // 62^10 ~ 8E17
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(ID_LETTERS.charAt(SECURE_RANDOM.nextInt(ID_LETTERS
                    .length())));
        }
        return sb.toString();
    }

    /**
     * Reads a text file from the class path.
     *
     * @param stream input. This stream will be closed.
     * @return contents of the text file
     */
    public static String readUTF8Resource(InputStream stream) {
        try {
            StringBuilder builder = new StringBuilder();
            try {
                Reader reader =
                        new BufferedReader(new InputStreamReader(stream,
                                StandardCharsets.UTF_8));
                char[] buffer = new char[8192];
                int read;
                while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                    builder.append(buffer, 0, read);
                }
            } finally {
                // Potential issue here: if this throws an IOException,
                // it will mask any others. Normally I'd use a utility
                // method which would log exceptions and swallow them
                stream.close();
            }
            return builder.toString();
        } catch (IOException e) {
            throw new InternalError(e.getMessage());
        }
    }

    /**
     * Serves an existing file from the Google Cloud Storage. This method uses
     * If-Modified-Since and similar HTTP headers and sends 304 if the file was
     * not changed.
     *
     * @param md GCS file meta data
     * @param request HTTP request
     * @param resp HTTP response
     * @param contentType MIME type of the response
     * @throws IOException error reading or sending the file
     */
    public static void serveFileFromGCS(GcsFileMetadata md,
                                        HttpServletRequest request,
                                        HttpServletResponse resp,
                                        String contentType) throws IOException {
        SimpleDateFormat httpDateFormat =
                new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

        String ims = request.getHeader("If-Modified-Since");
        boolean serve = true;
        if (ims != null) {
            Date lastSeen;
            try {
                lastSeen = httpDateFormat.parse(ims);
                if (lastSeen.getTime() >= md.getLastModified().getTime()) {
                    serve = false;
                }
            } catch (ParseException e) {
                // ignore
            }
        } else {
            String inm = request.getHeader("If-None-Match");
            if (inm != null) {
                String[] split = inm.split(",");
                if (Arrays.asList(split).contains(md.getEtag())) {
                    serve = false;
                }
            }
        }

        resp.setContentType(contentType);
        resp.setHeader("ETag", md.getEtag());

        resp.setHeader("Last-Modified",
                httpDateFormat.format(md.getLastModified()));

        BlobstoreService blobstoreService =
                BlobstoreServiceFactory.getBlobstoreService();
        GcsFilename f = md.getFilename();
        BlobKey blobKey =
                blobstoreService.createGsBlobKey("/gs/" + f.getBucketName() +
                        "/" + f.getObjectName());

        if (serve) {
            blobstoreService.serve(blobKey, resp);
        } else {
            resp.sendError(304);
        }
    }

    /**
     * Creates a user object from an email.
     *
     * @param email email
     * @return create user object
     */
    public static User email2user(String email) {
        return new User(email, email.substring(email.indexOf('@')));
    }

    /**
     * Creates a &lt;script&gt; tag
     *
     * @param w output
     * @param src value of the "src" attribute
     * @return the same writer
     */
    public static HTMLWriter linkScript(HTMLWriter w, final String src) {
        return w.e("script", "type", "text/javascript", "src", src);
    }

    /**
     * Creates a star for a package
     *
     * @param w output
     * @param package_ full package name
     * @param filled true = filled star
     * @param starred the amount of people who starred the package
     * @return the same writer
     */
    public static HTMLWriter star(HTMLWriter w, final String package_,
                                  boolean filled, int starred) {
        String txt;
        if (starred == 1) {
            txt = "1 user starred this package";
        } else if (starred > 1) {
            txt = starred + " users starred this package";
        } else {
            txt = "";
        }

        String n = String.valueOf(starred);
        w.start("span", "class", "nw-star");
        if (filled) {
            w.e("span", "class", "star glyphicon glyphicon-star", "style",
                    "cursor: pointer; color:#337ab7", "data-package", package_,
                    "data-starred", n, "data-filled", "true");
        } else {
            w.e("span", "class", "star glyphicon glyphicon-star-empty", "style",
                    "cursor: pointer", "data-package", package_,
                    "data-starred", n, "data-filled", "false");
        }
        w.e("small", txt);
        w.end("span");

        return w;
    }

    /**
     * Checks URLs using the Google Safe Browsing Lookup API
     *
     * @param urls this URLs will be checked. At most 500 URLs can be processed
     * at once.
     * @return threat types or empty strings if everything is OK.
     * THREAT_TYPE_UNSPECIFIED Unknown. MALWARE Malware threat type.
     * SOCIAL_ENGINEERING Social engineering threat type. UNWANTED_SOFTWARE
     * Unwanted software threat type. POTENTIALLY_HARMFUL_APPLICATION
     * Potentially harmful application threat type.
     * @throws java.io.IOException there was a communication problem, the server
     * is unavailable, over quota or something different.
     */
    public static String[] checkURLs(String[] urls) throws
            IOException {
        String[] result = new String[urls.length];

        try {
            JSONObject request = new JSONObject();
            JSONObject client = new JSONObject();
            client.put("clientId", "npackdweb");
            client.put("clientVersion", "2");
            request.put("client", client);
            JSONObject threatInfo = new JSONObject();
            threatInfo.put("threatTypes", new JSONArray(Arrays.asList(
                    "THREAT_TYPE_UNSPECIFIED", "MALWARE",
                    "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE",
                    "POTENTIALLY_HARMFUL_APPLICATION")));
            threatInfo.put("platformTypes", new JSONArray(Arrays.asList(
                    "ANY_PLATFORM")));
            threatInfo.put("threatEntryTypes", new JSONArray(Arrays.
                    asList("URL", "EXECUTABLE")));
            JSONArray threatEntries = new JSONArray();
            for (String url : urls) {
                JSONObject e = new JSONObject();
                e.put("url", url);
                threatEntries.put(e);
            }
            threatInfo.put("threatEntries", threatEntries);
            request.put("threatInfo", threatInfo);

            Arrays.fill(result, "");
            URL u = new URL(
                    "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" +
                            NWUtils.dsCache.getSetting("PublicAPIKey", ""));
            URLFetchService s = URLFetchServiceFactory.getURLFetchService();

            HTTPRequest ht = new HTTPRequest(u, HTTPMethod.POST);
            LOG.info(request.toString());
            ht.setHeader(new HTTPHeader("Content-Type", "application/json"));
            ht.setPayload(request.toString().getBytes(StandardCharsets.UTF_8));
            HTTPResponse r = s.fetch(ht);
            int rc = r.getResponseCode();
            if (rc == 200) {
                JSONObject json = new JSONObject(new String(r.getContent(),
                        StandardCharsets.UTF_8));
                //LOG.info(json.toString());
                JSONArray matches = json.optJSONArray("matches");
                if (matches != null) {
                    for (int i = 0; i < matches.length(); i++) {
                        JSONObject match = matches.getJSONObject(i);
                        String url = match.getJSONObject("threat").getString(
                                "url");
                        for (int j = 0; j < urls.length; j++) {
                            if (urls[j].equals(url)) {
                                result[j] = match.getString("threatType");
                                break;
                            }
                        }
                    }
                }
            } else {
                throw new IOException(
                        "Error " + rc + " from the Google Safe Browsing API " +
                                new String(r.getContent(),
                                        StandardCharsets.UTF_8));
            }
        } catch (MalformedURLException | JSONException ex) {
            throw new IOException(ex);
        }

        return result;
    }

    /**
     * Splits a string in two parts on the delimiter.
     *
     * @param s the string
     * @param del delimiter
     * @return [first string, second string] or [s, ""] if the delimiter was not
     * found
     */
    public static String[] partition(String s, String del) {
        int p = s.indexOf(del);
        String[] r = new String[2];
        if (p < 0) {
            r[0] = s;
            r[1] = "";
        } else {
            r[0] = s.substring(0, p);
            r[1] = s.substring(p + del.length());
        }

        return r;
    }

    /**
     * Retrieve a string from a Datastore entity.
     *
     * @param e an entity
     * @param propertyName name of the property
     * @return property value or null
     */
    public static String getString(com.google.appengine.api.datastore.Entity e,
                                   String propertyName) {
        Object obj = e.getProperty(propertyName);
        String result;
        if (obj instanceof com.google.appengine.api.datastore.Text) {
            result = ((com.google.appengine.api.datastore.Text) obj).getValue();
        } else {
            result = (String) obj;
        }
        if (result != null) {
            result = result.intern();
        }
        return result;
    }

    /**
     * Retrieve a long from a Datastore entity.
     *
     * @param e an entity
     * @param propertyName name of the property
     * @return property value or 0
     */
    public static long getLong(com.google.appengine.api.datastore.Entity e,
                               String propertyName) {
        Long obj = (Long) e.getProperty(propertyName);
        long result;
        if (obj == null) {
            result = 0;
        } else {
            result = obj.longValue();
        }
        return result;
    }

    /**
     * Retrieve a string list from a Datastore entity.
     *
     * @param e an entity
     * @param propertyName name of the property
     * @return property value != null
     */
    public static List<String> getStringList(
            com.google.appengine.api.datastore.Entity e,
            String propertyName) {
        List<String> obj = (List<String>) e.getProperty(propertyName);
        if (obj == null) {
            obj = new ArrayList<>();
        }
        for (int i = 0; i < obj.size(); i++) {
            obj.set(i, obj.get(i).intern());
        }
        return obj;
    }

    /**
     * Retrieve a user list from a Datastore entity.
     *
     * @param e an entity
     * @param propertyName name of the property
     * @return property value != null
     */
    public static List<User> getUserList(
            com.google.appengine.api.datastore.Entity e,
            String propertyName) {
        List<User> obj = (List<User>) e.getProperty(propertyName);
        if (obj == null) {
            obj = new ArrayList<>();
        }
        return obj;
    }
}
