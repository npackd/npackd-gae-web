package com.googlecode.npackdweb;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

/**
 * Check a package version URL (download it).
 */
public class CheckDownloadAction extends Action {
	private static long DAY_IN_MS = 24L * 60 * 60 * 1000;

	/**
	 * -
	 */
	public CheckDownloadAction() {
		super("^/tasks/check-download$", ActionSecurityType.ANONYMOUS);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String cursor = req.getParameter("cursor");
		// NWUtils.LOG.warning("checking download at cursor " + cursor);

		// download rate in bytes per millisecond
		// 4 GiB per day
		double rate = 4.0 * 1024 * 1024 * 1024 / DAY_IN_MS;

		// average download size
		long downloaded = 10 * 1024 * 1024;

		Objectify ob = DefaultServlet.getObjectify();
		Query<PackageVersion> q = ob.query(PackageVersion.class);
		if (cursor != null)
			q.startCursor(Cursor.fromWebSafeString(cursor));
		q.limit(1);

		QueryResultIterator<PackageVersion> iterator = q.iterator();
		PackageVersion data;
		if (iterator.hasNext())
			data = iterator.next();
		else
			data = null;
		if (data != null) {
			if (!PackageVersion.DONT_CHECK_THIS_DOWNLOAD
					.equals(data.downloadCheckError)) {
				NWUtils.LOG.warning("Checking " + data.package_ + "@" +
						data.version);

				NWUtils.Info info = data.check(true);
				if (info != null)
					downloaded = info.size;
				NWUtils.savePackageVersion(ob, data, false);
			}

			cursor = iterator.getCursor().toWebSafeString();
		} else {
			cursor = null;
		}

		Queue queue = QueueFactory.getDefaultQueue();
		try {
			TaskOptions to = withUrl("/tasks/check-download");
			if (cursor != null)
				to.param("cursor", cursor);
			long delay = Math.round(downloaded / rate);
			if (delay > DAY_IN_MS / 2)
				delay = DAY_IN_MS / 2;
			NWUtils.LOG.warning("delay in ms: " + delay);
			to.countdownMillis(delay);

			// NWUtils.LOG.warning("adding task at cursor " + cursor);
			queue.add(to);
		} catch (TaskAlreadyExistsException e) {
			NWUtils.LOG.warning("task check-download already exists");
		}

		resp.setStatus(200);
		return null;
	}
}
