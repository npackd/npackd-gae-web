package com.googlecode.npackdweb;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

/**
 * XML for a repository.
 */
public class RepXMLPage extends Page {
	private final String tag;

	/**
	 * @param tag
	 *            only package versions with this tag will be exported.
	 */
	public RepXMLPage(String tag) {
		this.tag = tag;
	}

	@Override
	public void create(HttpServletRequest request, HttpServletResponse resp)
			throws IOException {
		Objectify ofy = DefaultServlet.getObjectify();
		ExportRepsAction.export(ofy, tag, false);

		final GcsService gcsService =
				GcsServiceFactory.createGcsService(RetryParams
						.getDefaultInstance());

		GcsFilename fileName = new GcsFilename("npackd", tag + ".xml");
		GcsFileMetadata md = gcsService.getMetadata(fileName);

		SimpleDateFormat httpDateFormat =
				new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

		String ims = request.getHeader("If-Modified-Since");
		boolean serve = true;
		if (ims != null) {
			Date lastSeen;
			try {
				lastSeen = httpDateFormat.parse(ims);
				if (lastSeen.getTime() >= md.getLastModified().getTime())
					serve = false;
			} catch (ParseException e) {
				// ignore
			}
		} else {
			String inm = request.getHeader("If-None-Match");
			if (inm != null) {
				String[] split = inm.split(",");
				if (Arrays.asList(split).contains(md.getEtag()))
					serve = false;
			}
		}

		resp.setContentType("application/xml");
		resp.setHeader("ETag", md.getEtag());

		resp.setHeader("Last-Modified",
				httpDateFormat.format(md.getLastModified()));

		BlobstoreService blobstoreService =
				BlobstoreServiceFactory.getBlobstoreService();
		BlobKey blobKey =
				blobstoreService.createGsBlobKey("/gs/npackd/" + tag + ".xml");

		if (serve)
			blobstoreService.serve(blobKey, resp);
		else
			resp.sendError(304);
	}

	/**
	 * @param ofy
	 *            Objectify
	 * @param tag
	 *            package versions tag or null for "everything"
	 * @param onlyReviewed
	 *            true = only export reviewed package versions
	 * @return XML for the whole repository definition
	 */
	public static Document
			toXML(Objectify ofy, String tag, boolean onlyReviewed) {
		ArrayList<PackageVersion> pvs = new ArrayList<PackageVersion>();
		Query<PackageVersion> q =
				ofy.query(PackageVersion.class).chunkSize(500);
		if (tag != null)
			q.filter("tags =", tag);
		pvs.addAll(q.list());

		return toXML(ofy, pvs, onlyReviewed);
	}

	/**
	 * @param ofy
	 *            Objectify
	 * @param pvs
	 *            package versions
	 * @param onlyReviewed
	 *            true = only export reviewed package versions
	 * @return XML for the specified package versions
	 */
	public static Document toXML(Objectify ofy, ArrayList<PackageVersion> pvs,
			boolean onlyReviewed) {
		Collections.sort(pvs, new Comparator<PackageVersion>() {
			@Override
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
			if (pv.reviewed || !onlyReviewed)
				pns.add(pv.package_);
		}
		Map<String, Package> ps_ = ofy.get(Package.class, pns);
		List<Package> ps = new ArrayList<Package>();
		ps.addAll(ps_.values());
		Collections.sort(ps, new Comparator<Package>() {
			@Override
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

		Document d = NWUtils.newXMLRepository(true);
		Element root = d.getDocumentElement();

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

			if (pv.reviewed || !onlyReviewed) {
				Element version = pv.toXML(d);

				root.appendChild(version);
			}
		}

		return d;
	}
}
