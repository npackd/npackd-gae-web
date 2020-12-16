package com.googlecode.npackdweb.api;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;

/**
 * Creates XML for a package or updates a package.
 */
public class PackageXMLAction extends Action {

    /**
     * -
     */
    public PackageXMLAction() {
        super("^/api/p/([^/]+)$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (req.getMethod().equals("GET")) {
            String name = req.getRequestURI().substring(7);

            Package r = NWUtils.dsCache.getPackage(name, true);
            if (r == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Package " +
                        name + " not found");
                return null;
            } else {
                return new PackageXMLPage(name);
            }
        } else if (req.getMethod().equals("PATCH")) {
            String pw = NWUtils.dsCache.getSetting("AdminPassword", "");
            if (pw == null) {
                pw = "";
            }
            if (pw.trim().isEmpty()) {
                return new MessagePage("AdminPassword setting is not defined");
            }
            if (!pw.equals(req.getParameter("password"))) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }

            String name = req.getRequestURI().substring(7);

            Package r = NWUtils.dsCache.getPackage(name, true);
            if (r == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Package " +
                        name + " not found");
                return null;
            } else {
                if ("application/xml".equals(req.getContentType())) {
                    try {
                        Package old = r.copy();

                        DocumentBuilder db =
                                javax.xml.parsers.DocumentBuilderFactory.newInstance()
                                        .newDocumentBuilder();
                        final ServletInputStream stream =
                                req.getInputStream();
                        Document d = db.parse(stream);
                        boolean changed = false;

                        NodeList nl = d.getDocumentElement().getChildNodes();
                        for (int i = 0; i < nl.getLength(); i++) {
                            final Node item = nl.item(0);
                            if (item.getNodeType() == Node.ELEMENT_NODE &&
                                    item.getNodeName().equals("tag")) {
                                String tag = NWUtils.getTagContent_((Element) item);
                                if (!r.hasTag(tag)) {
                                    r.addTag(tag);
                                    changed = true;
                                }
                            }
                        }

                        if (changed) {
                            NWUtils.dsCache.savePackage(old, r, true);
                        }
                    } catch (SAXParseException e) {
                        throw new IOException("XML parsing error at " + e.getLineNumber() +
                                ":" + e.getColumnNumber() + ": " + e.getMessage(), e);
                    } catch (Exception e) {
                        throw new IOException(e);
                    }
                    return null;
                } else {
                    resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                            "Unsupported content type: " + req.getContentType());
                    return null;
                }
            }
        }

        return null;
    }
}
