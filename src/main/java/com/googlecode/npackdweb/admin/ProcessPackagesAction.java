package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.Entity;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * Process all packages.
 */
public class ProcessPackagesAction extends Action {
    /**
     * -
     */
    public ProcessPackagesAction() {
        super("^/process-packages$", ActionSecurityType.ADMINISTRATOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final Iterable<Entity> all =
                NWUtils.dsCache.getAllEntities("Package");
        for (Entity e : all) {
            map(e);
        }

        return new MessagePage("OK");
    }

    private void map(Entity value) {
        NWUtils.LOG.log(Level.INFO, "process-packages .map", value.getProperty(
                "name"));

        Package data = new Package(value);

        NWUtils.LOG.log(Level.INFO, "process-packages for {0}", data.name);

        com.googlecode.npackdweb.db.Package old = data.copy();
        if (updateDetectionToGithubAPI(data)) {
            NWUtils.dsCache.savePackage(old, data, false);
        }
    }

    /**
     * Update the detection URL so that it points to Github API instead of the
     * download page.
     *
     * @param p a package
     * @return true if the package was changed
     */
    private boolean updateDetectionToGithubAPI(Package p) {
        boolean res = false;

        final String prefix = "https://github.com/";
        final String suffix = "/releases/latest";
        String s = p.discoveryPage;
        if (s.startsWith(prefix) && s.endsWith(suffix)) {
            s = "https://api.github.com/repos/" + s.substring(prefix.length());
            p.discoveryPage = s;
            res = true;
        }

        return res;
    }

    private void fillArch(Package p) {
        // fill Package.arch from the PackageVersion tags
        List<PackageVersion> versions = NWUtils.dsCache.
                getSortedVersions(p.name);
        if (versions.size() > 0) {
            PackageVersion pv = versions.get(versions.size() - 1);
            if (pv.hasTag("stable64")) {
                p.addTag("stable64");
            } else if (pv.hasTag("stable")) {
                p.addTag("stable");
            } else if (pv.hasTag("libs")) {
                p.addTag("libs");
            } else if (pv.hasTag("unstable")) {
                p.addTag("unstable");
            }
        }
    }
}
