package com.googlecode.npackdweb.admin;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.db.Version;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class CheckUpdatesMapper {

    public void map(Package data) {

        com.googlecode.npackdweb.db.Package old = data.copy();
        NWUtils.LOG.log(Level.INFO, "check-update for {0}", data.name);
        Date noUpdatesCheck = null;
        try {
            Version v = data.findNewestVersion();
            List<PackageVersion> versions = NWUtils.dsCache.
                    getSortedVersions(data.name);
            if (versions.size() > 0) {
                PackageVersion pv = versions.get(versions.size() - 1);
                int r = v.compare(Version.parse(pv.version));
                if (r == 0) {
                    noUpdatesCheck = NWUtils.newDate();
                } else if (r > 0) {
                    data.createDetectedVersion(v, 0);
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
