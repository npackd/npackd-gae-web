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
 * Check a package for updates.
 */
public class CheckUpdatesAction extends Action {

    /**
     * -
     */
    public CheckUpdatesAction() {
        super("^/cron/check-update$", ActionSecurityType.ANONYMOUS);
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
                        new CheckUpdatesMapper(),
                        new NoOutput<Void, Void>()).build();
        String jobId = MapJob.start(ms, settings);

        return new MessagePage("Job ID: " + jobId);
    }
}
