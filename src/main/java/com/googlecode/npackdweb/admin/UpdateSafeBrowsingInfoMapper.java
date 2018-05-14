package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.pv.PackageVersionDetailAction;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import static com.googlecode.objectify.ObjectifyService.ofy;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class UpdateSafeBrowsingInfoMapper extends MapOnlyMapper<Entity, Void> {

    private static final long serialVersionUID = 1L;

    private List<com.googlecode.objectify.Key<PackageVersion>> keys =
            new ArrayList<>();

    @Override
    public void beginSlice() {
        this.keys.clear();
    }

    @Override
    public void endSlice() {
        try {
            process();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public void map(Entity value) {
        keys.add(Key.create(value.getKey()));
    }

    private void process() throws IOException {
        Objectify ofy = ofy();
        Map<Key<PackageVersion>, PackageVersion> items = ofy.load().keys(keys);

        List<PackageVersion> list = new ArrayList<>();
        list.addAll(items.values());
        process(list);
    }

    private void process(List<PackageVersion> list) throws IOException {
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
            Objectify ofy = ofy();

            ofy.save().entities(toSave);
            NWUtils.dsCache.incDataVersion();
        }
    }
}
