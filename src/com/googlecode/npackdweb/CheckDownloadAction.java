package com.googlecode.npackdweb;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
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
	private static class Info {
		public byte[] sha1;
		public long size;
	}

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
		double rate = 512 * 1024 * 1024 / (24 * 60 * 60 * 1000);

		// average download size
		long downloaded = 10 * 1024 * 1024;

		Objectify ob = NWUtils.getObjectify();
		Query<PackageVersion> q = ob.query(PackageVersion.class);
		if (cursor != null)
			q.startCursor(Cursor.fromWebSafeString(cursor));
		q.limit(1);

		QueryResultIterator<PackageVersion> iterator = q.iterator();
		PackageVersion data = iterator.next();
		if (data != null) {
			NWUtils.LOG.warning("Checking " + data.package_ + "@"
			        + data.version);

			data.downloadCheckAt = new Date();
			data.downloadCheckError = "Unknown error";
			if (!data.url.isEmpty()) {
				try {
					Info info = download(data.url);
					downloaded = info.size;
					if (data.sha1.trim().isEmpty())
						data.downloadCheckError = null;
					else {
						String sha1_ = NWUtils.byteArrayToHexString(info.sha1);
						if (sha1_.equalsIgnoreCase(data.sha1)) {
							data.downloadCheckError = null;
						} else {
							data.downloadCheckError = "Wrong SHA1: "
							        + data.sha1 + " was expected, but " + sha1_
							        + " was found";
						}
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
			long delay = Math.round(downloaded / rate);
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

	private static Info download(String url) throws IOException,
	        InterruptedException, NoSuchAlgorithmException {
		Info info = new Info();
		MessageDigest crypt = MessageDigest.getInstance("SHA-1");

		URL u = new URL(url);
		URLFetchService s = URLFetchServiceFactory.getURLFetchService();

		long startPosition = 0;
		long segment = 10 * 1024 * 1024;
		while (true) {
			HTTPRequest ht = new HTTPRequest(u);
			ht.setHeader(new HTTPHeader("User-Agent",
			        "NpackdWeb/1 (compatible; MSIE 9.0)"));
			ht.getFetchOptions().setDeadline(10 * 60.0);
			ht.setHeader(new HTTPHeader("Range", "bytes=" + startPosition + "-"
			        + (startPosition + segment - 1)));
			HTTPResponse r = s.fetch(ht);
			if (r.getResponseCode() == 416) {
				if (startPosition == 0)
					throw new IOException(
					        "Empty response with HTTP error code 416");
				else
					break;
			}

			if (r.getResponseCode() != 206) {
				throw new IOException("HTTP response code: "
				        + r.getResponseCode());
			}
			byte[] content = r.getContent();
			crypt.update(content);

			startPosition += segment;
			info.size += content.length;

			if (content.length < segment)
				break;
		}

		info.sha1 = crypt.digest();
		return info;
	}
}
