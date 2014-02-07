package com.googlecode.npackdweb.admin;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

/**
 * Re-saves package versions.
 */
public class ResavePackageVersionsAction extends Action {
    /**
     * -
     */
    public ResavePackageVersionsAction() {
        super("^/tasks/resave-package-versions$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String cursor = req.getParameter("cursor");

        Objectify ob = DefaultServlet.getObjectify();
        Query<PackageVersion> q = ob.query(PackageVersion.class);
        if (cursor != null)
            q.startCursor(Cursor.fromWebSafeString(cursor));
        q.limit(100);

        QueryResultIterator<PackageVersion> iterator = q.iterator();

        int n = 0;
        List<PackageVersion> list = new ArrayList<PackageVersion>();
        while (iterator.hasNext()) {
            PackageVersion data = iterator.next();
            data.reviewed = true;
            list.add(data);
            n++;
        }

        ob.put(list);

        if (n == 100) {
            cursor = iterator.getCursor().toWebSafeString();
        } else {
            cursor = null;
            NWUtils.sendMailToAdmin("cursor == nul for /tasks/resave-package-version");
        }

        Queue queue = QueueFactory.getQueue("resave");
        try {
            TaskOptions to = withUrl("/tasks/resave-package-versions");
            if (cursor != null)
                to.param("cursor", cursor);

            queue.add(to);
        } catch (TaskAlreadyExistsException e) {
            NWUtils.LOG
                    .warning("task /tasks/resave-package-version already exists");
        }

        resp.setStatus(200);
        return null;
    }
}
