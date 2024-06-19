package com.googlecode.npackdweb.api;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.*;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.npackdweb.wlib.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * XML for a repository.
 */
public class RepXMLPage extends Page {

    private final String tag;
    private final String create;
    private final boolean extra;

    /**
     * @param tag only package versions with this tag will be exported.
     * @param create "true" = create the file, "now" = re-create file release
     * asset, other values = redirect to a GitHub,
     * @param extra true = export non-standard fields
     */
    public RepXMLPage(String tag, String create, boolean extra) {
        this.tag = tag;
        this.create = create;
        this.extra = extra;
    }

    @Override
    public void create(HttpServletRequest request, HttpServletResponse resp)
            throws IOException {
        if ("true".equals(this.create)) {
            final GcsFileMetadata md;
            try {
                md = NWUtils.getMetadata(
                        tag + (this.extra ? "_extra" : "") + ".xml");
                NWUtils.serveFileFromGCS(md, request, resp,
                        "application/xml");
            } catch (ExecutionException ex) {
                throw new IOException(ex.getMessage());
            }
        } else if ("now".equals(create) && NWUtils.isAdminLoggedIn()) {
            byte[] d = toXMLByPackageTag2(this.tag, this.extra);
            resp.setContentType("application/xml");
            resp.getOutputStream().write(d);
        } else {
            resp.sendRedirect(
                    "https://npackd.github.io/npackd/repository/" +
                            tag + ".xml");
        }
    }

    /**
     * @param tag package versions tag or null for "everything"
     * @param onlyReviewed true = only export reviewed package versions
     * @param extra true = export non-standard fields
     * @return XML for the whole repository definition
     */
    public static byte[]
    toXML2(String tag, boolean onlyReviewed, boolean extra) {
        List<PackageVersion> pvs = NWUtils.dsCache.findPackageVersions(tag,
                null, 0, 0);

        // remove untested package versions
        Iterator<PackageVersion> it = pvs.iterator();
        while (it.hasNext()) {
            PackageVersion pv = it.next();
            if (pv.tags.contains("untested")) {
                it.remove();
            }
        }

        String addDescription;
        if ("stable".equals(tag)) {
            addDescription = "\ni686";
        } else if ("stable64".equals(tag)) {
            addDescription = "\nx86_64";
        } else {
            addDescription = "";
        }

        return toXML2(pvs, onlyReviewed, addDescription,
                extra).getContent().
                toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * @param tag package tag or null for "everything"
     * @param extra true = export non-standard fields
     * @return XML for the whole repository definition
     */
    public static byte[]
    toXMLByPackageTag2(String tag, boolean extra) {
        List<Package> ps = NWUtils.dsCache.findPackages(tag,
                null, null, 0);

        String addDescription;
        if ("stable".equals(tag)) {
            addDescription = "\ni686";
        } else if ("stable64".equals(tag)) {
            addDescription = "\nx86_64";
        } else {
            addDescription = "";
        }

        return toXML3(ps, addDescription,
                extra).getContent().
                toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * @param pvs package versions
     * @param onlyReviewed true = only export reviewed package versions
     * @param addDescription additional text added to the end of package
     * descriptions for each package
     * @param extra true = export non-standard fields
     * @return XML for the specified package versions
     */
    public static HTMLWriter toXML2(List<PackageVersion> pvs,
                                    boolean onlyReviewed, String addDescription,
                                    boolean extra) {
        pvs.sort(new Comparator<PackageVersion>() {
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

        ps.sort(new Comparator<Package>() {
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

        licenses.sort(new Comparator<License>() {
            @Override
            public int compare(License a, License b) {
                return a.name.compareToIgnoreCase(b.name);
            }
        });

        HTMLWriter d = NWUtils.newXMLRepository(true);

        for (License l : licenses) {
            l.toXML(d);
        }

        for (Package p : ps) {
            p.description += addDescription;

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
            p.toXML(d, extra);
        }

        String lastPackage = "";
        for (PackageVersion pv : pvs) {
            if (!pv.package_.equals(lastPackage)) {
                lastPackage = pv.package_;
                d.t("\n");
            }

            if (!pv.tags.contains("not-reviewed") || !onlyReviewed) {
                pv.toXML(d, extra);
            }
        }

        d.end("root");

        return d;
    }

    /**
     * @param ps packages
     * @param addDescription additional text added to the end of package
     * descriptions for each package
     * @param extra true = export non-standard fields
     * @return XML for the specified package versions
     */
    public static HTMLWriter toXML3(List<Package> ps,
                                    String addDescription,
                                    boolean extra) {
        ps.sort(new Comparator<Package>() {
            @Override
            public int compare(Package a, Package b) {
                return a.name.compareToIgnoreCase(b.name);
            }
        });

        HTMLWriter d = NWUtils.newXMLRepository(true);

        // Licenses
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
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        Map<com.google.appengine.api.datastore.Key, Entity> ls = datastore.get(
                lkeys);
        List<License> licenses = new ArrayList<>();
        for (Entity e : ls.values()) {
            licenses.add(new License(e));
        }
        licenses.sort(new Comparator<License>() {
            @Override
            public int compare(License a, License b) {
                return a.name.compareToIgnoreCase(b.name);
            }
        });
        for (License l : licenses) {
            l.toXML(d);
        }

        // packages
        for (Package p : ps) {
            p.description += addDescription;

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
            p.toXML(d, extra);
        }

        // package versions
        for (Package p : ps) {
            final List<PackageVersion> pvs =
                    NWUtils.dsCache.getPackageVersions(p.name);
            pvs.sort(new Comparator<PackageVersion>() {
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

            for (PackageVersion pv : pvs) {
                if (!pv.tags.contains("not-reviewed") && !pv.tags.contains(
                        "untested") && !pv.tags.contains(
                        "unstable")) {
                    pv.toXML(d, extra);
                }
            }
            d.t("\n");
        }

        d.end("root");

        return d;
    }
}
