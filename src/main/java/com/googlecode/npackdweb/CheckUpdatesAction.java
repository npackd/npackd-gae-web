package com.googlecode.npackdweb;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
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
        super("^/tasks/check-update$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String cursor = req.getParameter("cursor");

        Objectify ob = DefaultServlet.getObjectify();
        Query<Package> q = ob.query(Package.class);
        if (cursor != null) {
            q.startCursor(Cursor.fromWebSafeString(cursor));
        }
        q.limit(1);

        QueryResultIterator<Package> iterator = q.iterator();
        Package data;
        if (iterator.hasNext()) {
            data = iterator.next();
        } else {
            data = null;
        }
        if (data != null) {
            Package old = data.copy();
            NWUtils.LOG.log(Level.INFO, "check-update for {0}", data.name);
            Date noUpdatesCheck = null;
            if ("0".equals(req.getHeader("X-AppEngine-TaskRetryCount"))) {
                try {
                    Version v = data.findNewestVersion();
                    List<PackageVersion> versions = data.getSortedVersions(ob);
                    if (versions.size() > 0) {
                        PackageVersion pv = versions.get(versions.size() - 1);
                        int r = v.compare(Version.parse(pv.version));
                        if (r == 0) {
                            noUpdatesCheck = NWUtils.newDate();
                        } else if (r > 0) {
                            data.createDetectedVersion(ob, v);
                        }
                    }
                } catch (IOException e) {
                    // ignore
                }
            }

            if (noUpdatesCheck != null || data.noUpdatesCheck != null) {
                data.noUpdatesCheck = noUpdatesCheck;
                NWUtils.savePackage(ob, old, data, false);
            }

            NWUtils.LOG.log(Level.INFO, "check-update noUpdatesCheck= {0}",
                    data.noUpdatesCheck);

            cursor = iterator.getCursor().toWebSafeString();
        } else {
            cursor = null;
        }

        Queue queue = QueueFactory.getQueue("check-update");
        TaskOptions to = withUrl("/tasks/check-update");
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
