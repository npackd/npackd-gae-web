package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.googlecode.npackdweb.Dependency;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.DatastoreCache;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import static com.googlecode.objectify.ObjectifyService.ofy;
import java.util.List;

public class CleanDependenciesMapper extends MapOnlyMapper<Entity, Void> {

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
        moveSourceForgeToOneServer(value);
    }

    private void moveSourceForgeToOneServer(Entity value) {
        Objectify ofy = ofy();

        PackageVersion pv = (PackageVersion) ofy.load().key(Key.create(value.
                getKey().toString())).now();
        PackageVersion oldpv = pv.copy();
        boolean save = false;

        final String PREFIX = "http://downloads.sourceforge.net/";

        if (pv.url.startsWith(PREFIX)) {
            pv.url = pv.url.replace(PREFIX, "https://ayera.dl.sourceforge.net/");
            save = true;
        }

        if (save) {
            System.out.println("Saving " + pv.name + " " + pv.url);
            DatastoreCache.savePackageVersion(oldpv, pv, true, false);
        }
    }

    private void moveFromGoogleCodeFiles(Entity value) {
        Objectify ofy = ofy();

        PackageVersion pv = (PackageVersion) ofy.load().key(Key.create(value.
                getKey().toString())).now();
        PackageVersion oldpv = pv.copy();
        boolean save = false;

        String[] parts = NWUtils.partition(pv.url, ".googlecode.com/files/");

        if (!parts[1].isEmpty()) {
            String[] parts2 = NWUtils.partition(parts[0], "://");
            if (!parts2[1].isEmpty()) {
                pv.url =
                        "https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/" +
                        parts2[1] + "/" + parts[1];
                save = true;
            }
        }

        if (save) {
            System.out.println("Saving " + pv.name + " " + pv.url);
            DatastoreCache.savePackageVersion(oldpv, pv, true, false);
        }
    }

    private void moveToFilesNpackdOrg(Entity value) {
        Objectify ofy = ofy();

        PackageVersion pv = (PackageVersion) ofy.load().key(Key.create(value.
                getKey().toString())).now();
        PackageVersion oldpv = pv.copy();
        boolean save = false;

        final String PREFIX = "http://dl.dropbox.com/u/17046326/files/";

        if (pv.url.startsWith(PREFIX)) {
            pv.url = "http://files.npackd.org/" + pv.url.substring(PREFIX.
                    length());
            save = true;
        }

        if (save) {
            System.out.println("Saving " + pv.name);
            DatastoreCache.savePackageVersion(oldpv, pv, true, false);
        }
    }

    public void removeNpackdCLCalls(Entity value) {
        Objectify ofy = ofy();

        PackageVersion pv = (PackageVersion) ofy.load().key(Key.create(value.
                getKey().toString())).now();
        PackageVersion oldpv = pv.copy();
        boolean save = false;

        for (int i = 0; i < pv.getFileCount(); i++) {
            String s = pv.getFileContents(i);
            List<String> lines = NWUtils.splitLines(s);
            for (int j = 0; j < lines.size() - 1;) {
                String line = lines.get(j);
                String line2 = lines.get(j + 1);
                String[] npackdCLParams = getNpackdCLParams(line, line2);
                boolean found = false;
                if (npackdCLParams != null) {
                    Dependency d = new Dependency();
                    if (d.setVersions(npackdCLParams[1]) == null) {
                        d.package_ = npackdCLParams[0];
                        int index = pv.findDependency(d);
                        if (index >= 0 &&
                                (pv.dependencyEnvVars.get(index).isEmpty() ||
                                pv.dependencyEnvVars
                                .get(index).equals(npackdCLParams[2]))) {
                            lines.remove(j);
                            lines.remove(j);
                            pv.dependencyEnvVars.set(index, npackdCLParams[2]);
                            pv.setFileContents(i, NWUtils.join("\r\n", lines));
                            found = true;
                            save = true;
                        }
                    }
                }

                if (!found) {
                    j++;
                }
            }
        }

        if (save) {
            System.out.println("Saving " + pv.name);
            DatastoreCache.savePackageVersion(oldpv, pv, true, false);
        }
    }

    private static String[] getNpackdCLParams(String line, String line2) {
        final String prefix =
                "set onecmd=\"%npackd_cl%\\npackdcl.exe\" \"path\" \"--package=";
        final String prefix2 =
                "for /f \"usebackq delims=\" %%x in (`%%onecmd%%`) do set ";
        String[] result = null;
        if (line.trim().toLowerCase().startsWith(prefix) &&
                line2.trim().toLowerCase().startsWith(prefix2)) {
            String[] parts = line.substring(prefix.length()).split("\"");
            String[] parts2 = line2.substring(prefix2.length()).split("=");
            if (parts.length > 2 && parts2.length > 0) {
                String p = parts[0].trim();
                String vs = parts[2].trim();
                if (vs.startsWith("--versions=")) {
                    Dependency d = new Dependency();
                    if (d.setVersions(vs.substring(11)) == null) {
                        String var = parts2[0].trim();
                        result = new String[]{p, vs.substring(11), var};
                    }
                }
            }
        }

        return result;
    }
}
