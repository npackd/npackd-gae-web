package com.googlecode.npackdweb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Upload a repository.
 */
public class RepUploadAction extends Action {
	private static final Logger log = Logger.getLogger(RepUploadAction.class
			.getName());

	private static final class Found {
		List<License> lics;
		public List<Package> ps;
		public List<PackageVersion> pvs;
	}

	/**
	 * -
	 */
	public RepUploadAction() {
		super("^/rep/upload$", ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		ServletFileUpload upload = new ServletFileUpload();

		FileItemIterator iterator;
		try {
			iterator = upload.getItemIterator(req);
			Found f = null;
			String tag = "unknown";
			while (iterator.hasNext()) {
				FileItemStream item = iterator.next();
				InputStream stream = item.openStream();

				try {
					if (item.isFormField()) {
						log.warning("Got a form field: " + item.getFieldName());
						if (item.getFieldName().equals("tag")) {
							BufferedReader r = new BufferedReader(
									new InputStreamReader(stream));
							tag = r.readLine();
							log.warning("Got a form field tag: " + tag);
						}
					} else {
						log.warning("Got an uploaded file: "
								+ item.getFieldName() + ", name = "
								+ item.getName());

						f = process(stream);
					}
				} finally {
					stream.close();
				}
			}
			if (f != null) {
				for (PackageVersion pv : f.pvs) {
					pv.tags.add(tag);
				}

				Objectify ofy = ObjectifyService.begin();
				ofy.put(f.lics);
				ofy.put(f.pvs);
				ofy.put(f.ps);
			}

		} catch (FileUploadException e) {
			throw (IOException) new IOException(e.getMessage()).initCause(e);
		}

		resp.sendRedirect("/");
		return null;
	}

	private Found process(InputStream stream) throws IOException {
		Found f = null;
		try {
			DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			Document d = db.parse(stream);
			f = process(d);
		} catch (Exception e) {
			throw (IOException) new IOException(e.getMessage()).initCause(e);
		}
		return f;
	}

	private Found process(Document d) {
		Found f = new Found();
		Element root = d.getDocumentElement();
		f.lics = processLicenses(root.getChildNodes());
		f.ps = processPackages(root.getChildNodes());
		f.pvs = processPackageVersions(root.getChildNodes());

		return f;
	}

	private List<License> processLicenses(NodeList children) {
		List<License> v = new ArrayList<License>();
		for (int i = 0; i < children.getLength(); i++) {
			Node ch = children.item(i);
			if (ch.getNodeType() == Element.ELEMENT_NODE
					&& ch.getNodeName().equals("license")) {
				Element license = (Element) ch;
				License lic = new License();
				lic.name = license.getAttribute("name");
				lic.title = NWUtils.getSubTagContent(license, "title", "");
				lic.url = NWUtils.getSubTagContent(license, "url", "");
				v.add(lic);
			}
		}
		return v;
	}

	private List<Package> processPackages(NodeList children) {
		List<Package> v = new ArrayList<Package>();
		for (int i = 0; i < children.getLength(); i++) {
			Node ch = children.item(i);
			if (ch.getNodeType() == Element.ELEMENT_NODE
					&& ch.getNodeName().equals("package")) {
				Element e = (Element) ch;
				Package p = new Package(e.getAttribute("name"));
				p.title = NWUtils.getSubTagContent(e, "title", "");
				p.url = NWUtils.getSubTagContent(e, "url", "");
				p.description = NWUtils.getSubTagContent(e, "description", "");
				p.icon = NWUtils.getSubTagContent(e, "icon", "");
				p.license = NWUtils.getSubTagContent(e, "license", "");
				v.add(p);
			}
		}
		return v;
	}

	private List<PackageVersion> processPackageVersions(NodeList children) {
		List<PackageVersion> v = new ArrayList<PackageVersion>();
		for (int i = 0; i < children.getLength(); i++) {
			Node ch = children.item(i);
			if (ch.getNodeType() == Element.ELEMENT_NODE
					&& ch.getNodeName().equals("version")) {
				Element e = (Element) ch;
				PackageVersion pv = createPackageVersion(e);
				v.add(pv);
			}
		}
		return v;
	}

	private PackageVersion createPackageVersion(Element e) {
		PackageVersion p = new PackageVersion();
		p.package_ = e.getAttribute("package");
		p.version = e.getAttribute("name");
		p.name = p.package_ + "@" + p.version;
		p.oneFile = e.getAttribute("type").equals("one-file");
		p.url = NWUtils.getSubTagContent(e, "url", "");
		p.sha1 = NWUtils.getSubTagContent(e, "sha1", "");
		p.detectMSI = NWUtils.getSubTagContent(e, "detect-msi", "");

		NodeList children = e.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node ch = children.item(i);
			if (ch.getNodeType() == Element.ELEMENT_NODE) {
				Element che = (Element) ch;
				if (che.getNodeName().equals("important-file")) {
					p.importantFilePaths.add(che.getAttribute("path"));
					p.importantFileTitles.add(che.getAttribute("title"));
				} else if (che.getNodeName().equals("file")) {
					p.filePaths.add(che.getAttribute("path"));
					p.fileContents.add(NWUtils.getTagContent_(che));
				} else if (che.getNodeName().equals("dependency")) {
					p.dependencyPackages.add(che.getAttribute("package"));
					p.dependencyVersionRanges.add(che.getAttribute("versions"));
					p.dependencyEnvVars.add(NWUtils.getSubTagContent(che,
							"variable", ""));
				}
			}
		}
		return p;
	}
}
