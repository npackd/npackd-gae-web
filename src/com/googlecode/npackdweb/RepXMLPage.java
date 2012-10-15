package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
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

		try {
			Document d = toXML();
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
					"4");
			t.transform(new DOMSource(d.getDocumentElement()),
					new StreamResult(resp.getOutputStream()));
			resp.getOutputStream().close();
		} catch (Exception e) {
			throw (IOException) new IOException(e.getMessage()).initCause(e);
		}
	}

	/**
	 * @return XML for the whole repository definition
	 */
	public Document toXML() {
		Document d;
		try {
			d = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument();
		} catch (ParserConfigurationException e) {
			throw (InternalError) new InternalError(e.getMessage())
					.initCause(e);
		}

		Element root = d.createElement("root");
		d.appendChild(root);
		NWUtils.e(root, "spec-version", "2");

		// getting data
		Objectify ofy = ObjectifyService.begin();
		ArrayList<PackageVersion> pvs = new ArrayList<PackageVersion>();
		Query<PackageVersion> q = ofy.query(PackageVersion.class)
				.chunkSize(500);
		if (this.tag != null)
			q.filter("tags =", tag);
		for (PackageVersion p : q.fetch())
			pvs.add(p);
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

		for (License l : ls.values()) {
			Element license = d.createElement("license");
			license.setAttribute("name", l.name);
			if (!l.title.isEmpty())
				NWUtils.e(license, "title", l.title);
			if (!l.url.isEmpty())
				NWUtils.e(license, "url", l.url);

			root.appendChild(license);
		}

		for (Package p : ps) {
			Element package_ = d.createElement("package");
			package_.setAttribute("name", p.name);
			if (!p.title.isEmpty())
				NWUtils.e(package_, "title", p.title);
			if (!p.url.isEmpty())
				NWUtils.e(package_, "url", p.url);
			if (!p.description.isEmpty())
				NWUtils.e(package_, "description", p.description);
			if (!p.icon.isEmpty())
				NWUtils.e(package_, "icon", p.icon);

			root.appendChild(package_);
		}

		String lastPackage = "";
		for (PackageVersion pv : pvs) {
			if (!pv.package_.equals(lastPackage)) {
				lastPackage = pv.package_;
				NWUtils.t(root, "\n\n    ");
			}

			Element version = d.createElement("version");
			version.setAttribute("name", pv.version);
			version.setAttribute("package", pv.package_);
			if (pv.oneFile)
				version.setAttribute("type", "one-file");
			for (int i = 0; i < pv.importantFilePaths.size(); i++) {
				Element importantFile = d.createElement("important-file");
				version.appendChild(importantFile);
				importantFile
						.setAttribute("path", pv.importantFilePaths.get(i));
				importantFile.setAttribute("title", pv.importantFileTitles
						.get(i));
			}
			for (int i = 0; i < pv.filePaths.size(); i++) {
				Element file = d.createElement("file");
				version.appendChild(file);
				file.setAttribute("path", pv.filePaths.get(i));
				NWUtils.t(file, pv.fileContents.get(i));
			}
			if (!pv.url.isEmpty())
				NWUtils.e(version, "url", pv.url);
			if (!pv.sha1.isEmpty())
				NWUtils.e(version, "sha1", pv.sha1);
			for (int i = 0; i < pv.dependencyPackages.size(); i++) {
				Element dependency = d.createElement("dependency");
				version.appendChild(dependency);
				dependency
						.setAttribute("package", pv.dependencyPackages.get(i));
				dependency.setAttribute("versions", pv.dependencyVersionRanges
						.get(i));
				if (!pv.dependencyEnvVars.get(i).isEmpty())
					NWUtils.e(dependency, "variable", pv.dependencyEnvVars
							.get(i));
			}
			if (!pv.detectMSI.isEmpty())
				NWUtils.e(version, "detect-msi", pv.detectMSI);
			for (int i = 0; i < pv.detectFilePaths.size(); i++) {
				Element detectFile = d.createElement("detect-file");
				version.appendChild(detectFile);
				NWUtils.e(detectFile, "path", pv.detectFilePaths.get(i));
				NWUtils.e(detectFile, "sha1", pv.detectFileSHA1s.get(i));
			}

			root.appendChild(version);
		}

		return d;
	}
}
