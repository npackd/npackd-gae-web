package com.googlecode.npackdweb;

import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import static com.googlecode.objectify.ObjectifyService.ofy;
import com.googlecode.objectify.cmd.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    /**
     * @param tag only package versions with this tag will be exported.
     */
    public RepXMLPage(String tag) {
        this.tag = tag;
    }

    @Override
    public void create(HttpServletRequest request, HttpServletResponse resp)
            throws IOException {
        Objectify ofy = ofy();
        final GcsService gcsService =
                GcsServiceFactory.createGcsService(RetryParams
                        .getDefaultInstance());

        GcsFilename fileName = new GcsFilename("npackd", tag + ".xml");
        GcsFileMetadata md = gcsService.getMetadata(fileName);

        if (md == null) {
            ExportRepsAction.export(gcsService, ofy, tag, false);
            md = gcsService.getMetadata(fileName);
        }

        NWUtils.serveFileFromGCS(gcsService, md, request, resp,
                "application/xml");
    }

    /**
     * @param ofy Objectify
     * @param tag package versions tag or null for "everything"
     * @param onlyReviewed true = only export reviewed package versions
     * @return XML for the whole repository definition
     */
    public static Document
            toXML(Objectify ofy, String tag, boolean onlyReviewed) {
        ArrayList<PackageVersion> pvs = new ArrayList<>();
        Query<PackageVersion> q =
                ofy.load().type(PackageVersion.class).chunk(500);
        if (tag != null) {
            q = q.filter("tags =", tag);
        }
        pvs.addAll(q.list());

        // remove untested package versions
        Iterator<PackageVersion> it = pvs.iterator();
        while (it.hasNext()) {
            PackageVersion pv = it.next();
            if (pv.tags.contains("untested")) {
                it.remove();
            }
        }

        return toXML(ofy, pvs, onlyReviewed, tag);
    }

    /**
     * @param ofy Objectify
     * @param pvs package versions
     * @param onlyReviewed true = only export reviewed package versions
     * @param tag package versions tag or null for "everything"
     * @return XML for the specified package versions
     */
    public static Document toXML(Objectify ofy, ArrayList<PackageVersion> pvs,
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
        List<Key<Package>> keys = new ArrayList<>();
        for (String s : pns) {
            keys.add(Key.create(Package.class, s));
        }
        Map<Key<Package>, Package> ps_ = ofy.load().keys(keys);
        List<Package> ps = new ArrayList<>();
        ps.addAll(ps_.values());
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
        List<Key<License>> lkeys = new ArrayList<>();
        for (String s : lns) {
            lkeys.add(Key.create(License.class, s));
        }
        Map<Key<License>, License> ls = ofy.load().keys(lkeys);
        List<License> licenses = new ArrayList<>();
        licenses.addAll(ls.values());
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
                p.description = "WARNING: the development was stopped. " +
                        "There will be no new versions of this software.\n" +
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
