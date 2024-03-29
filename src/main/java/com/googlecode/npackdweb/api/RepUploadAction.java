package com.googlecode.npackdweb.api;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Upload a repository.
 */
public class RepUploadAction extends Action {

    private static final class Found {

        List<License> lics;
        public List<Package> ps;
        public List<PackageVersion> pvs;
    }

    private static final class Stats {

        public int pOverwritten, pAppended;
        public int pvOverwritten, pvAppended;
        public int licOverwritten, licAppended;
    }

    /**
     * -
     */
    public RepUploadAction() {
        super("^/rep/upload$", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        List<String> messages = new ArrayList<>();

        Found f = null;
        String tag = "unknown";
        boolean overwrite = false;
        if (ServletFileUpload.isMultipartContent(req)) {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator;
            try {
                iterator = upload.getItemIterator(req);
                while (iterator.hasNext()) {
                    FileItemStream item = iterator.next();
                    InputStream stream = item.openStream();

                    try {
                        if (item.isFormField()) {
                            if (item.getFieldName().equals("tag")) {
                                BufferedReader r =
                                        new BufferedReader(
                                                new InputStreamReader(stream));
                                tag = r.readLine();
                            } else if (item.getFieldName()
                                    .equals("overwrite")) {
                                overwrite = true;
                            }
                        } else {
                            f = process(stream);
                        }
                    } finally {
                        stream.close();
                    }
                }
            } catch (Exception e) {
                messages.add("Error reading the data: " + e.getMessage());
            }
        } else {
            tag = req.getParameter("tag");
            String rep = req.getParameter("repository");
            overwrite = req.getParameter("overwrite") != null;
            try {
                f = process(new ByteArrayInputStream(rep.getBytes(
                        StandardCharsets.UTF_8)));
            } catch (IOException e) {
                messages.add("Error reading the data: " + e.getMessage());
            }
        }

        if (f != null) {
            boolean isAdmin = NWUtils.isAdminLoggedIn();

            for (PackageVersion pv : f.pvs) {
                pv.addTag(tag);
            }

            // determine the keys for all objects
            /* implement caching later
             List<Key<?>> keys = new ArrayList<>();
             for (License lic : f.lics) {
             keys.add(lic.createKey());
             }
             for (PackageVersion pv : f.pvs) {
             keys.add(pv.createKey());
             }
             for (Package p : f.ps) {
             keys.add(p.createKey());
             }
             */
            Stats stats = new Stats();

            // process licenses first. Only admins can change the license.
            if (!f.lics.isEmpty()) {
                if (isAdmin) {
                    for (License lic : f.lics) {
                        License existing = NWUtils.dsCache.getLicense(lic.name,
                                true);
                        if (existing != null) {
                            if (overwrite) {
                                NWUtils.dsCache.saveLicense(lic, true);
                                stats.licOverwritten++;
                            } else {
                                messages.add("The license " + lic.name +
                                        " exists already. It will not be overwritten.");
                            }
                        } else {
                            NWUtils.dsCache.saveLicense(lic, true);
                            stats.licAppended++;
                        }
                    }
                } else {
                    messages.add("Only an administrator can change licenses");
                }
            }

            // process packages before the package versions
            Map<String, Package> packagesCache = new HashMap<>();
            for (Package p : f.ps) {
                Package p_ = NWUtils.dsCache.getPackage(p.name, true);

                if (p_ != null && !p_.isCurrentUserPermittedToModify()) {
                    messages.add(
                            "You do not have permission to modify this package: " +
                                    p.name);
                    packagesCache.put(p_.name, p_);
                } else if (p_ != null) {
                    if (overwrite) {
                        // overwriting should not change the permissions
                        p.permissions.clear();
                        p.permissions.addAll(p_.permissions);

                        NWUtils.dsCache.savePackage(p_, p, true);
                        stats.pOverwritten++;
                        packagesCache.put(p.name, p);
                    } else {
                        messages.add("The package " + p.name +
                                " exists already. It will not be overwritten.");
                        packagesCache.put(p_.name, p_);
                    }
                } else {
                    NWUtils.dsCache.savePackage(null, p, true);
                    stats.pAppended++;
                    packagesCache.put(p.name, p);
                }
            }

            // process package versions
            for (PackageVersion pv : f.pvs) {
                pv.addTag("untested");

                Package p = NWUtils.dsCache.getPackage(pv.package_, true);
                PackageVersion existing = NWUtils.dsCache.getPackageVersion(
                        pv.name);
                if (p != null && !p.isCurrentUserPermittedToModify()) {
                    messages.add(
                            "You do not have permission to modify this package: " +
                                    pv.package_);
                } else {
                    if (p == null) {
                        p = packagesCache.get(pv.package_);
                    }

                    if (p == null) {
                        p = new Package(pv.package_);
                        p.title = p.name;
                        NWUtils.dsCache.savePackage(null, p, true);
                        stats.pAppended++;
                    }

                    if (existing != null) {
                        if (overwrite) {
                            NWUtils.dsCache.
                                    savePackageVersion(existing, pv, true,
                                            true);
                            stats.pvOverwritten++;
                        } else {
                            messages.add("The package version " + pv.package_ +
                                    " " + pv.name +
                                    " exists already. It will not be overwritten.");
                        }
                    } else {
                        NWUtils.dsCache.savePackageVersion(null, pv, true,
                                true);
                        stats.pvAppended++;
                    }
                }
            }

            messages.add(stats.pOverwritten + " packages overwritten, " +
                    stats.pvOverwritten + " package versions overwritten, " +
                    stats.licOverwritten + " licenses overwritten, " +
                    stats.pAppended + " packages appended, " +
                    stats.pvAppended + " package versions appended, " +
                    stats.licAppended + " licenses appended");
        } else {
            messages.add("No data found");
        }

        return new MessagePage(messages);
    }

    private Found process(InputStream stream) throws IOException {
        Found f;
        try {
            DocumentBuilder db =
                    javax.xml.parsers.DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder();
            Document d = db.parse(stream);
            f = process(d);
        } catch (SAXParseException e) {
            throw new IOException("XML parsing error at " + e.getLineNumber() +
                    ":" + e.getColumnNumber() + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException(e);
        }
        return f;
    }

    private Found process(Document d) throws IOException {
        Found f = new Found();
        Element root = d.getDocumentElement();
        f.lics = processLicenses(root.getChildNodes());
        f.ps = processPackages(root.getChildNodes());
        f.pvs = processPackageVersions(root.getChildNodes());

        return f;
    }

    private List<License> processLicenses(NodeList children) {
        List<License> v = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node ch = children.item(i);
            if (ch.getNodeType() == Element.ELEMENT_NODE &&
                    ch.getNodeName().equals("license")) {
                Element license = (Element) ch;
                License lic = new License();
                lic.name = license.getAttribute("name");
                lic.title = NWUtils.getSubTagContent(license, "title", "");
                lic.url = NWUtils.getSubTagContent(license, "url", "");
                v.add(lic);
            }
        }
        return v;
    }

    private List<Package> processPackages(NodeList children) {
        List<Package> v = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node ch = children.item(i);
            if (ch.getNodeType() == Element.ELEMENT_NODE &&
                    ch.getNodeName().equals("package")) {
                Element e = (Element) ch;
                Package p = Package.parse(e);
                v.add(p);
            }
        }
        return v;
    }

    private List<PackageVersion> processPackageVersions(NodeList children)
            throws IOException {
        List<PackageVersion> v = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node ch = children.item(i);
            if (ch.getNodeType() == Element.ELEMENT_NODE &&
                    ch.getNodeName().equals("version")) {
                Element el = (Element) ch;

                PackageVersion pv = new PackageVersion("p", "1");
                try {
                    pv.parseXML(el);
                } catch (NumberFormatException ex) {
                    throw new IOException(ex);
                }
                v.add(pv);
            }
        }
        return v;
    }
}
