package com.googlecode.npackdweb;

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
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * XML for a repository.
 */
public class RepXMLPage extends Page {

    private final String tag;
    private final boolean create;

    /**
     * @param tag only package versions with this tag will be exported.
     * @param create true = create the file, false = redirect to a Github
     * release asset
     */
    public RepXMLPage(String tag, boolean create) {
        this.tag = tag;
        this.create = create;
    }

    @Override
    public void create(HttpServletRequest request, HttpServletResponse resp)
            throws IOException {
        if (!create) {
            resp.sendRedirect(
                    "https://github.com/tim-lebedkov/npackd/releases/download/v1/" +
                    tag + ".xml");
        } else {
            NWUtils.serveFileFromGCS("/var/lib/npackd-web/rep/" + tag +
                    ".xml", request, resp,
                    "application/xml");
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

        List<String> pns2 = new ArrayList<>();
        pns2.addAll(pns);

        List<Package> ps = NWUtils.dsCache.getPackages(pns2, true);

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
        List<String> lns2 = new ArrayList<>();
        lns2.addAll(lns);

        List<License> licenses = NWUtils.dsCache.getLicenses(lns2);

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
