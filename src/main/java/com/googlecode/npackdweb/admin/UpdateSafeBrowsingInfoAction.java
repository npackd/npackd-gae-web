package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.pv.PackageVersionDetailAction;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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
        super("^/tasks/update-safe-browsing-info$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String cursor = req.getParameter("cursor");

        Objectify ob = DefaultServlet.getObjectify();
        Query<PackageVersion> q = ob.query(PackageVersion.class);
        if (cursor != null) {
            q.startCursor(Cursor.fromWebSafeString(cursor));
        }

        final int BATCH_SIZE = 100;

        q.limit(BATCH_SIZE);

        /*
         if (!"0".equals(req.getHeader("X-AppEngine-TaskRetryCount"))) {
         throw new IOException("Retries are not allowed");
         }
         */
        final QueryResultIterator<PackageVersion> iterator = q.iterator();
        int n = 0;
        List<PackageVersion> list = new ArrayList<PackageVersion>();
        while (iterator.hasNext()) {
            PackageVersion data = iterator.next();
            list.add(data);
            n++;
        }

        if (n == BATCH_SIZE) {
            cursor = iterator.getCursor().toWebSafeString();
        } else {
            cursor = null;
        }

        if (n > 0) {
            NWUtils.LOG.log(Level.INFO, "Checking from {0} {1}", new Object[]{
                list.get(0).getPackage(),
                list.get(0).getVersion()});

            List<PackageVersion> toSave = new ArrayList<>();
            String[] urls = new String[list.size()];
            for (int i = 0; i < urls.length; i++) {
                urls[i] = list.get(i).url;
                if (urls[i].trim().isEmpty()) {
                    urls[i] = "https://www.google.com";
                }
            }
            String[] results = NWUtils.checkURLs(ob, urls);
            for (int i = 0; i < results.length; i++) {
                PackageVersion data = list.get(i);
                PackageVersion old = data.copy();

                data.tags.remove("phishing");
                data.tags.remove("malware");
                data.tags.remove("unwanted");
                String v = results[i];
                if (!data.url.trim().isEmpty()) {
                    switch (v) {
                        case "THREAT_TYPE_UNSPECIFIED":
                            data.addTag("unwanted");
                        case "MALWARE":
                            data.addTag("malware");
                        case "SOCIAL_ENGINEERING":
                            data.addTag("phishing");
                        case "UNWANTED_SOFTWARE":
                            data.addTag("unwanted");
                        case "POTENTIALLY_HARMFUL_APPLICATION":
                            data.addTag("unwanted");
                    }
                }

                String oldStatus = old.hasTag("phishing") + " " + old.hasTag(
                        "malware") + " " +
                        old.hasTag("unwanted");
                String status = data.hasTag("phishing") + " " + data.hasTag(
                        "malware") + " " +
                        data.hasTag("unwanted");

                if (!oldStatus.equals(status)) {
                    NWUtils.sendMailToAdmin(
                            "Google Safe Browsing API: got " + v +
                            " for " +
                            data.getPackage() + " " + data.getVersion() +
                            " (" +
                            PackageVersionDetailAction.getURL(data) +
                            ")");
                    toSave.add(data);
                }
            }

            if (toSave.size() > 0) {
                ob.put(toSave);
                NWUtils.incDataVersion();
            }
        }

        Queue queue = QueueFactory.getQueue("update-safe-browsing-info");
        TaskOptions to = withUrl("/tasks/update-safe-browsing-info");
        if (cursor != null) {
            to.param("cursor", cursor);
        }

        // 2 minutes
        to.countdownMillis(2 * 60 * 1000);

        // NWUtils.LOG.warning("adding task at cursor " + cursor);
        queue.add(to);

        resp.setStatus(200);
        return null;
    }
}
