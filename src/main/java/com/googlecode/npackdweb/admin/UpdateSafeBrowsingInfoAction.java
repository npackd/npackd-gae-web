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
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;
import java.io.IOException;
import java.util.List;
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
        q.limit(1);

        QueryResultIterator<PackageVersion> iterator = q.iterator();
        PackageVersion data;
        if (iterator.hasNext()) {
            data = iterator.next();
        } else {
            data = null;
        }
        if (data != null) {
            NWUtils.LOG.info("update-safe-browsing-info for " + data.
                    getPackage() +
                    " " + data.getVersion());
            if (!"0".equals(req.getHeader("X-AppEngine-TaskRetryCount"))) {
                throw new IOException("Retries are not allowed");
            }

            PackageVersion old = data.copy();

            if (!data.url.trim().isEmpty()) {
                try {
                    String v = NWUtils.checkURL(ob, data.url);
                    NWUtils.LOG.info("Google Safe Browsing API: got " + v);
                    if (!v.isEmpty()) {
                        NWUtils.sendMailToAdmin(
                                "Google Safe Browsing API: got " + v + " for " +
                                data.getPackage() + " " + data.getVersion());
                    }
                    List<String> parts = NWUtils.split(v, ',');
                    for (String part : parts) {
                        data.addTag(part);
                    }
                } catch (IOException e) {
                    NWUtils.LOG.severe(e.getMessage());
                }
            } else {
                data.tags.remove("phishing");
                data.tags.remove("malware");
                data.tags.remove("unwanted");
            }

            String oldStatus = old.hasTag("phishing") + " " + old.hasTag(
                    "malware") + " " +
                    old.hasTag("unwanted");
            String status = data.hasTag("phishing") + " " + data.hasTag(
                    "malware") + " " +
                    data.hasTag("unwanted");

            if (!oldStatus.equals(status)) {
                NWUtils.savePackageVersion(ob, old, data, false, false);
            }

            cursor = iterator.getCursor().toWebSafeString();
        } else {
            cursor = null;
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
