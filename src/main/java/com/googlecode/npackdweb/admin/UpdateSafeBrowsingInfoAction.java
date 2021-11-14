package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.pv.PackageVersionDetailAction;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Check a package versions using Google Safe Browsing API.
 */
public class UpdateSafeBrowsingInfoAction extends Action {
    /**
     * -
     */
    public UpdateSafeBrowsingInfoAction() {
        super("^/cron/update-safe-browsing-info$",
                ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final Iterable<Entity> all =
                NWUtils.dsCache.getAllEntities("PackageVersion");

        List<PackageVersion> list = new ArrayList<>();
        for (Entity e: all) {
            list.add(new PackageVersion(e));
            if (list.size() == 100) {
                processBatch(list);
                list.clear();
            }
        }

        if (list.size() != 0)
            processBatch(list);

        return new MessagePage("OK");
    }

    private void processBatch(List<PackageVersion> list) throws IOException {
        NWUtils.LOG.log(Level.INFO, "Checking from {0} {1}", new Object[]{
            list.get(0).getPackage(),
            list.get(0).getVersion()});

        List<PackageVersion> toSave = new ArrayList<>();
        String[] urls = new String[list.size()];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = list.get(i).url;
            if (urls[i].trim().isEmpty()) {
                urls[i] = "https://www.google.com";
            }
        }
        String[] results = NWUtils.checkURLs(urls);
        for (int i = 0; i < results.length; i++) {
            PackageVersion data = list.get(i);
            PackageVersion old = data.copy();

            data.tags.remove("phishing");
            data.tags.remove("malware");
            data.tags.remove("unwanted");
            String v = results[i];
            if (!data.url.trim().isEmpty()) {
                switch (v) {
                    case "THREAT_TYPE_UNSPECIFIED":
                        data.addTag("unwanted");
                    case "MALWARE":
                        data.addTag("malware");
                    case "SOCIAL_ENGINEERING":
                        data.addTag("phishing");
                    case "UNWANTED_SOFTWARE":
                        data.addTag("unwanted");
                    case "POTENTIALLY_HARMFUL_APPLICATION":
                        data.addTag("unwanted");
                }
            }

            String oldStatus = old.hasTag("phishing") + " " + old.hasTag(
                    "malware") + " " +
                    old.hasTag("unwanted");
            String status = data.hasTag("phishing") + " " + data.hasTag(
                    "malware") + " " +
                    data.hasTag("unwanted");

            if (!oldStatus.equals(status)) {
                NWUtils.sendMailToAdmin(
                        "Google Safe Browsing API: got " + v +
                        " for " +
                        data.getPackage() + " " + data.getVersion() +
                        " (" +
                        PackageVersionDetailAction.getURL(data) +
                        ")");
                toSave.add(data);
            }
        }

        if (toSave.size() > 0) {
            List<Entity> toSave_ = new ArrayList<>();
            for (PackageVersion pv : toSave) {
                toSave_.add(pv.createEntity());
            }
            DatastoreService datastore = DatastoreServiceFactory.
                    getDatastoreService();
            datastore.put(toSave_);
            NWUtils.dsCache.incDataVersion();
        }
    }
}
