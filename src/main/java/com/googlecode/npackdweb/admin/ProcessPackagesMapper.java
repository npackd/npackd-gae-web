package com.googlecode.npackdweb.admin;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import java.util.logging.Level;

public class ProcessPackagesMapper {

    public void map(Package data) {
        NWUtils.LOG.log(Level.INFO, "process-packages for {0}", data.name);

        com.googlecode.npackdweb.db.Package old = data.copy();

        /*List<PackageVersion> versions = NWUtils.dsCache.
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
        }*/
        NWUtils.dsCache.savePackage(old, data, false);
    }
}
