package com.googlecode.npackdweb;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

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
		if (cursor != null)
			q.startCursor(Cursor.fromWebSafeString(cursor));
		q.limit(1);

		QueryResultIterator<Package> iterator = q.iterator();
		Package data;
		if (iterator.hasNext())
			data = iterator.next();
		else
			data = null;
		if (data != null) {
			NWUtils.LOG.info("check-update for " + data.name);
			Version v = null;
			try {
				if (!"0".equals(req.getHeader("X-AppEngine-TaskRetryCount")))
					throw new IOException("Retries are not allowed");

				v = data.findNewestVersion();
				List<PackageVersion> versions =
						ob.query(PackageVersion.class)
								.filter("package_ =", data.name).list();
				Collections.sort(versions, new Comparator<PackageVersion>() {
					@Override
					public int compare(PackageVersion a, PackageVersion b) {
						Version va = Version.parse(a.version);
						Version vb = Version.parse(b.version);
						return va.compare(vb);
					}
				});
				if (versions.size() > 0) {
					PackageVersion pv = versions.get(versions.size() - 1);
					int r = v.compare(Version.parse(pv.version));
					if (r == 0) {
						data.noUpdatesCheck = new Date();
						NWUtils.savePackage(ob, data, false);
					} else {
						if (data.noUpdatesCheck != null) {
							data.noUpdatesCheck = null;
							NWUtils.savePackage(ob, data, false);
						}
					}
				} else {
					if (data.noUpdatesCheck != null) {
						data.noUpdatesCheck = null;
						NWUtils.savePackage(ob, data, false);
					}
				}
			} catch (IOException e) {
				if (data.noUpdatesCheck != null) {
					data.noUpdatesCheck = null;
					NWUtils.savePackage(ob, data, false);
				}
			}

			NWUtils.LOG.info("check-update noUpdatesCheck= " +
					data.noUpdatesCheck);

			cursor = iterator.getCursor().toWebSafeString();
		} else {
			cursor = null;
		}

		Queue queue = QueueFactory.getQueue("check-update");
		TaskOptions to = withUrl("/tasks/check-update");
		if (cursor != null)
			to.param("cursor", cursor);

		// 2 minutes
		to.countdownMillis(2 * 60 * 1000);

		// NWUtils.LOG.warning("adding task at cursor " + cursor);
		queue.add(to);

		resp.setStatus(200);
		return null;
	}
}
