package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import java.util.List;
import java.util.logging.Level;

public class ProcessPackagesMapper extends MapOnlyMapper<Entity, Void> {

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
        NWUtils.LOG.log(Level.INFO, "process-packages .map", value.getProperty(
                "name"));

        // fill Package.arch from the PackageVersion tags
        Package data = new Package(value);

        NWUtils.LOG.log(Level.INFO, "process-packages for {0}", data.name);

        com.googlecode.npackdweb.db.Package old = data.copy();

        List<PackageVersion> versions = NWUtils.dsCache.
                getSortedVersions(data.name);
        if (versions.size() > 0) {
            PackageVersion pv = versions.get(versions.size() - 1);
            if (pv.hasTag("stable64")) {
                data.addTag("stable64");
            } else if (pv.hasTag("stable")) {
                data.addTag("stable");
            } else if (pv.hasTag("libs")) {
                data.addTag("libs");
            } else if (pv.hasTag("unstable")) {
                data.addTag("unstable");
            }
            NWUtils.dsCache.savePackage(old, data, false);
        }
    }
}
