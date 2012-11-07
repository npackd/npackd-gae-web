package com.googlecode.npackdweb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

/**
 * XML for a repository.
 */
public class RepXMLPage extends Page {
	private String tag;

	/**
	 * @param tag
	 *            only package versions with this tag will be exported. null
	 *            means all package versions will be exported
	 */
	public RepXMLPage(String tag) {
		this.tag = tag;
	}

	@Override
	public void create(HttpServletRequest request, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("application/xml");

		final String key = "RepXMLPage." + this.tag + "@"
				+ DefaultServlet.dataVersion.get();
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers
				.getConsistentLogAndContinue(Level.INFO));
		byte[] value = (byte[]) syncCache.get(key); // read from cache
		if (value == null) {
			NWUtils.LOG.warning("Found no value in cache");
			try {
				Document d = toXML();
				Transformer t = TransformerFactory.newInstance()
						.newTransformer();
				t.setOutputProperty(OutputKeys.INDENT, "yes");
				t.setOutputProperty(
						"{http://xml.apache.org/xslt}indent-amount", "4");
				t.setOutputProperty(
						"{http://xml.apache.org/xalan}line-separator", "\r\n");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				GZIPOutputStream gos = new GZIPOutputStream(baos);
				t.transform(new DOMSource(d.getDocumentElement()),
						new StreamResult(gos));
				gos.finish();
				gos.flush();
				value = baos.toByteArray();
				if (value.length < 1024 * 1024)
					syncCache.put(key, value); // populate cache
			} catch (Exception e) {
				throw (IOException) new IOException(e.getMessage())
						.initCause(e);
			}
		} else {
			NWUtils.LOG.warning("Found value in cache " + value.length
					+ " bytes");
		}

		GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(
				value));
		copy(gis, resp.getOutputStream());
		resp.getOutputStream().close();
	}

	private void copy(InputStream gis, OutputStream os) throws IOException {
		byte[] buffer = new byte[8 * 1024];
		int read;
		while ((read = gis.read(buffer)) != -1) {
			os.write(buffer, 0, read);
		}
	}

	/**
	 * @return XML for the whole repository definition
	 */
	public Document toXML() {
		Document d = NWUtils.newXMLRepository(true);

		Element root = d.getDocumentElement();

		// getting data
		Objectify ofy = NWUtils.getObjectify();
		ArrayList<PackageVersion> pvs = new ArrayList<PackageVersion>();
		Query<PackageVersion> q = ofy.query(PackageVersion.class)
				.chunkSize(500);
		if (this.tag != null)
			q.filter("tags =", tag);
		pvs.addAll(q.list());
		Collections.sort(pvs, new Comparator<PackageVersion>() {
			public int compare(PackageVersion a, PackageVersion b) {
				int r = a.package_.compareToIgnoreCase(b.package_);
				if (r == 0) {
					Version av = Version.parse(a.version);
					Version bv = Version.parse(b.version);
					r = av.compare(bv);
				}
				return r;
			}
		});
		Set<String> pns = new HashSet<String>();
		for (PackageVersion pv : pvs) {
			pns.add(pv.package_);
		}
		Map<String, Package> ps_ = ofy.get(Package.class, pns);
		List<Package> ps = new ArrayList<Package>();
		ps.addAll(ps_.values());
		Collections.sort(ps, new Comparator<Package>() {
			public int compare(Package a, Package b) {
				return a.name.compareToIgnoreCase(b.name);
			}
		});
		Set<String> lns = new HashSet<String>();
		for (Package p : ps) {
			if (!p.license.isEmpty())
				lns.add(p.license);
		}
		Map<String, License> ls = ofy.get(License.class, lns);
		List<License> licenses = new ArrayList<License>();
		licenses.addAll(ls.values());
		Collections.sort(licenses, new Comparator<License>() {
			@Override
			public int compare(License a, License b) {
				return a.name.compareToIgnoreCase(b.name);
			}
		});

		for (License l : licenses) {
			Element license = d.createElement("license");
			license.setAttribute("name", l.name);
			if (!l.title.isEmpty())
				NWUtils.e(license, "title", l.title);
			if (!l.url.isEmpty())
				NWUtils.e(license, "url", l.url);

			root.appendChild(license);
		}

		for (Package p : ps) {
			Element package_ = p.toXML(d);

			root.appendChild(package_);
		}

		String lastPackage = "";
		for (PackageVersion pv : pvs) {
			if (!pv.package_.equals(lastPackage)) {
				lastPackage = pv.package_;
				NWUtils.t(root, "\n\n    ");
			}

			Element version = pv.toXML(d);

			root.appendChild(version);
		}

		return d;
	}
}
