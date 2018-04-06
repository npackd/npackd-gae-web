package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskOptions;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import static com.googlecode.objectify.ObjectifyService.ofy;
import com.googlecode.objectify.cmd.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

        Objectify ob = ofy();
        Query<PackageVersion> q = ob.load().type(PackageVersion.class);
        if (cursor != null) {
            q = q.startAt(Cursor.fromWebSafeString(cursor));
        }
        q = q.limit(100);

        QueryResultIterator<PackageVersion> iterator = q.iterator();

        int n = 0;
        List<PackageVersion> list = new ArrayList<>();
        while (iterator.hasNext()) {
            PackageVersion data = iterator.next();
            list.add(data);
            n++;
        }

        ob.save().entities(list);

        if (n == 100) {
            cursor = iterator.getCursor().toWebSafeString();
        } else {
            cursor = null;
            NWUtils.sendMailToAdmin(
                    "cursor == nul for /tasks/resave-package-version");
        }

        Queue queue = QueueFactory.getQueue("resave");
        try {
            TaskOptions to = withUrl("/tasks/resave-package-versions");
            if (cursor != null) {
                to.param("cursor", cursor);
            }

            queue.add(to);
        } catch (TaskAlreadyExistsException e) {
            NWUtils.LOG
                    .warning("task /tasks/resave-package-version already exists");
        }

        resp.setStatus(200);
        return null;
    }
}
