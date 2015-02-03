package com.googlecode.npackdweb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

public class RecentRepXMLPage extends Page {
	private String user;
	private String tag;

	/**
	 * @param user
	 *            email address or null
	 * @param tag
	 *            tag for package versions to filter or null
	 */
	public RecentRepXMLPage(String user, String tag) {
		this.user = user;
		this.tag = tag;
	}

	@Override
	public void create(HttpServletRequest request, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("application/xml");

		final String key =
				this.getClass().getName() + "@" + NWUtils.getDataVersion() +
						"@" + user + "@" + tag;
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers
				.getConsistentLogAndContinue(Level.INFO));
		byte[] value = (byte[]) syncCache.get(key); // read from cache
		if (value == null) {
			NWUtils.LOG.warning("Found no value in cache");
			try {
				Objectify ofy = ObjectifyService.begin();
				ArrayList<PackageVersion> pvs = new ArrayList<PackageVersion>();
				Query<PackageVersion> q =
						ofy.query(PackageVersion.class).limit(20)
								.order("-lastModifiedAt");
				if (user != null)
					q =
							q.filter(
									"lastModifiedBy =",
									new User(user, user.substring(user
											.indexOf('@'))));
				if (tag != null)
					q = q.filter("tags =", tag);
				pvs.addAll(q.list());
				Document d = RepXMLPage.toXML(ofy, pvs, false);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				NWUtils.serializeXML(d, baos);
				value = baos.toByteArray();
				syncCache.put(key, value); // populate cache
			} catch (Exception e) {
				throw (IOException) new IOException(e.getMessage())
						.initCause(e);
			}
		} else {
			NWUtils.LOG.warning("Found value in cache " + value.length +
					" bytes");
		}

		ServletOutputStream ros = resp.getOutputStream();
		ros.write(value);
		ros.close();
	}
}
