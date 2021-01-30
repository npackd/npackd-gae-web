package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.db.Version;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;

public class CheckUpdatesMapper extends MapOnlyMapper<Entity, Void> {

    private static final long serialVersionUID = 1L;

    private transient DatastoreMutationPool pool;

    @Override
    public void beginSlice() {
        this.pool = DatastoreMutationPool.create();
    }

    @Override
    public void endSlice() {
        this.pool.flush();
    }

    @Override
    public void map(Entity value) {
        Package data = new Package(value);

        com.googlecode.npackdweb.db.Package old = data.copy();
        NWUtils.LOG.log(Level.INFO, "check-update for {0}", data.name);
        Date noUpdatesCheck = null;
        try {
            Matcher found = data.findNewestVersion();
            Version v = NWUtils.parseVersion(found.group(1));
            String match = found.group();
            List<PackageVersion> versions = NWUtils.dsCache.
                    getSortedVersions(data.name);
            if (versions.size() > 0) {
                PackageVersion pv = versions.get(versions.size() - 1);
                int r = v.compare(Version.parse(pv.version));
                if (r == 0) {
                    noUpdatesCheck = NWUtils.newDate();
                } else if (r > 0) {
                    data.createDetectedVersion(match, v, 0);
                }
            }
        } catch (IOException e) {
            // ignore
        }

        if (noUpdatesCheck != null || data.noUpdatesCheck != null) {
            data.noUpdatesCheck = noUpdatesCheck;
            NWUtils.dsCache.savePackage(old, data, false);
        }

        NWUtils.LOG.log(Level.INFO, "check-update noUpdatesCheck= {0}",
                data.noUpdatesCheck);
    }
}
