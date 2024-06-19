package com.googlecode.npackdweb;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Exports repositories to the GCS.
 */
public class ExportRepsAction extends Action {

    /**
     * -
     */
    public ExportRepsAction() {
        super("^/cron/export-reps$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        List<Repository> rs = NWUtils.dsCache.findAllRepositories();
        for (Repository r : rs) {
            Queue queue = QueueFactory.getDefaultQueue();
            queue.add(TaskOptions.Builder.withUrl("/cron/export-rep")
                    .param("tag", r.name)
                    .retryOptions(RetryOptions.Builder.withDefaults()
                            .taskRetryLimit(1)));
        }
        resp.setStatus(200);
        return null;
    }
}
