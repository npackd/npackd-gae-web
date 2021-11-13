package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.Entity;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import java.util.logging.Level;

/**
 * Process all packages.
 /
 public class ProcessPackagesAction extends Action {

 /**
 * -
 *
 public ProcessPackagesAction() {
 super("^/process-packages$", ActionSecurityType.ADMINISTRATOR);
 }

 @Override
 public Page perform(HttpServletRequest req, HttpServletResponse resp)
 throws IOException {
 MapReduceSettings settings =
 new MapReduceSettings.Builder().setWorkerQueueName("default")
 .setBucketName("npackd").build();

 MapSpecification<Entity, Void, Void> ms =
 new MapSpecification.Builder<>(new DatastoreInput(
 "Package", 100),
 new ProcessPackagesMapper(),
 new NoOutput<Void, Void>()).build();
 String jobId = MapJob.start(ms, settings);

 return new MessagePage("Job ID: " + jobId);
 }
 }
 */

public class ProcessPackagesMapper{
    public void map(Entity value) {
        NWUtils.LOG.log(Level.INFO, "process-packages .map", value.getProperty(
                "name"));

        // fill Package.arch from the PackageVersion tags
        Package data = new Package(value);

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
