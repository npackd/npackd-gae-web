package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.MapJob;
import com.google.appengine.tools.mapreduce.MapReduceSettings;
import com.google.appengine.tools.mapreduce.MapSpecification;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.mapreduce.outputs.NoOutput;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Check a package version using Google Safe Browsing API.
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
        MapReduceSettings settings =
                new MapReduceSettings.Builder().setWorkerQueueName("default")
                .setBucketName("npackd").build();

        MapSpecification<Entity, Void, Void> ms =
                new MapSpecification.Builder<>(new DatastoreInput(
                                "PackageVersion", 50),
                        new UpdateSafeBrowsingInfoMapper(),
                        new NoOutput<Void, Void>()).build();
        String jobId = MapJob.start(ms, settings);

        return new MessagePage("Job ID: " + jobId);
    }
}
