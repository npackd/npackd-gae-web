package com.googlecode.npackdweb;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.db.Version;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * XML for a repository.
 */
public class RepXMLPage extends Page {

    private final String tag;

    /**
     * @param tag only package versions with this tag will be exported.
     */
    public RepXMLPage(String tag) {
        this.tag = tag;
    }

    @Override
    public void create(HttpServletRequest request, HttpServletResponse resp)
            throws IOException {

        final GcsFileMetadata md;
        try {
            md = NWUtils.getMetadata(tag + ".xml");
            NWUtils.serveFileFromGCS(md, request, resp,
                    "application/xml");
        } catch (ExecutionException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    /**
     * @param tag package versions tag or null for "everything"
     * @param onlyReviewed true = only export reviewed package versions
     * @return XML for the whole repository definition
     */
    public static Document
            toXML(String tag, boolean onlyReviewed) {
        List<PackageVersion> pvs = NWUtils.dsCache.findPackageVersions(tag,
                null, 0);

        // remove untested package versions
        Iterator<PackageVersion> it = pvs.iterator();
        while (it.hasNext()) {
            PackageVersion pv = it.next();
            if (pv.tags.contains("untested")) {
                it.remove();
            }
        }

        return toXML(pvs, onlyReviewed, tag);
    }

    /**
     * @param tag package tag or null for "everything"
     * @param onlyReviewed true = only export reviewed package versions
     * @return XML for the whole repository definition
     */
    public static Document
            toXMLByPackageTag(String tag, boolean onlyReviewed) {
        List<Package> ps = NWUtils.dsCache.findPackages(tag,
                null, 0);
        Set<String> packageNames = new HashSet<>(ps.size());
        for (Package p : ps) {
            packageNames.add(p.name);
        }

        List<PackageVersion> pvs = NWUtils.dsCache.findPackageVersions(null,
                null, 0);

        // remove untested package versions
        Iterator<PackageVersion> it = pvs.iterator();
        while (it.hasNext()) {
            PackageVersion pv = it.next();
            if (pv.tags.contains("untested") || pv.tags.contains("unstable") ||
                    !packageNames.contains(
                            pv.package_)) {
                it.remove();
            }
        }

        return toXML(pvs, onlyReviewed, tag);
    }

    /**
     * @param pvs package versions
     * @param onlyReviewed true = only export reviewed package versions
     * @param tag package versions tag or null for "everything"
     * @return XML for the specified package versions
     */
    public static Document toXML(List<PackageVersion> pvs,
            boolean onlyReviewed, String tag) {
        Collections.sort(pvs, new Comparator<PackageVersion>() {
            @Override
            public int compare(PackageVersion a, PackageVersion b) {
                int r = a.package_.compareToIgnoreCase(b.package_);
                if (r == 0) {
                    Version av = Version.parse(a.version);
                    Version bv = Version.parse(b.version);
                    r = av.compare(bv);
                }
                return r;
            }
        });
        Set<String> pns = new HashSet<>();
        for (PackageVersion pv : pvs) {
            if (!pv.tags.contains("not-reviewed") || !onlyReviewed) {
                pns.add(pv.package_);
            }
        }
        List<com.google.appengine.api.datastore.Key> keys = new ArrayList<>();
        for (String s : pns) {
            keys.add(KeyFactory.createKey("Package", s));
        }

        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        Map<com.google.appengine.api.datastore.Key, Entity> ps2 = datastore.get(
                keys);

        List<Package> ps = new ArrayList<>();
        for (Entity e : ps2.values()) {
            ps.add(new Package(e));
        }

        Collections.sort(ps, new Comparator<Package>() {
            @Override
            public int compare(Package a, Package b) {
                return a.name.compareToIgnoreCase(b.name);
            }
        });
        Set<String> lns = new HashSet<>();
        for (Package p : ps) {
            if (!p.license.isEmpty()) {
                lns.add(p.license);
            }
        }
        List<com.google.appengine.api.datastore.Key> lkeys = new ArrayList<>();
        for (String s : lns) {
            lkeys.add(KeyFactory.createKey("License", s));
        }
        Map<com.google.appengine.api.datastore.Key, Entity> ls = datastore.get(
                lkeys);
        List<License> licenses = new ArrayList<>();
        for (Entity e : ls.values()) {
            licenses.add(new License(e));
        }

        Collections.sort(licenses, new Comparator<License>() {
            @Override
            public int compare(License a, License b) {
                return a.name.compareToIgnoreCase(b.name);
            }
        });

        Document d = NWUtils.newXMLRepository(true);
        Element root = d.getDocumentElement();

        for (License l : licenses) {
            Element license = l.toXML(d);

            root.appendChild(license);
        }

        for (Package p : ps) {
            if ("stable".equals(tag)) {
                p.description += "\ni686";
            } else if ("stable64".equals(tag)) {
                p.description += "\nx86_64";
            }

            if (p.hasTag("end-of-life")) {
                p.description = "WARNING: " +
                        "there will be no new versions of this package.\n" +
                        p.description;
            }
            if (p.hasTag("same-url")) {
                p.description =
                        "WARNING: this package always installs the newest " +
                        "version of the software.\n" +
                        p.description;
            }
            Element package_ = p.toXML(d);

            root.appendChild(package_);
        }

        String lastPackage = "";
        for (PackageVersion pv : pvs) {
            if (!pv.package_.equals(lastPackage)) {
                lastPackage = pv.package_;
                NWUtils.t(root, "\n\n    ");
            }

            if (!pv.tags.contains("not-reviewed") || !onlyReviewed) {
                Element version = pv.toXML(d);

                root.appendChild(version);
            }
        }

        return d;
    }
}
