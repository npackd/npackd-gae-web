package com.googlecode.npackdweb;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

/**
 * Check a package version URL (download it).
 */
public class CheckDownloadAction extends Action {
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

		Objectify ob = NWUtils.OBJECTIFY.get();
		Query<PackageVersion> q = ob.query(PackageVersion.class);
		if (cursor != null)
			q.startCursor(Cursor.fromWebSafeString(cursor));
		q.limit(1);

		QueryResultIterator<PackageVersion> iterator = q.iterator();
		PackageVersion data = iterator.next();
		if (data != null) {
			data.downloadCheckAt = new Date();
			data.downloadCheckError = "Unknown error";
			if (!data.url.isEmpty()) {
				try {
					URLFetchService s = URLFetchServiceFactory
							.getURLFetchService();
					HTTPRequest ht = new HTTPRequest(new URL(data.url));
					ht.getFetchOptions().setDeadline(10 * 60.0);
					HTTPResponse r = s.fetch(ht);
					if (r.getResponseCode() / 100 != 2) {
						data.downloadCheckError = "HTTP response code: "
								+ r.getResponseCode();
					} else {
						data.downloadCheckError = null;
					}
				} catch (Exception e) {
					data.downloadCheckError = "Error downloading: "
							+ e.getMessage();
				}
			} else {
				data.downloadCheckError = "URL is empty";
			}
			ob.put(data);
			DefaultServlet.dataVersion.incrementAndGet();

			cursor = iterator.getCursor().toWebSafeString();
		} else {
			cursor = null;
		}

		Queue queue = QueueFactory.getDefaultQueue();
		try {
			TaskOptions to = withUrl("/tasks/check-download");
			if (cursor != null)
				to.param("cursor", cursor);

			// NWUtils.LOG.warning("adding task at cursor " + cursor);
			queue.add(to);
		} catch (TaskAlreadyExistsException e) {
			NWUtils.LOG.warning("task check-download already exists");
		}

		resp.setStatus(200);
		return null;
	}
}
