package com.googlecode.npackdweb.package_;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.License;
import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.Package;
import com.googlecode.npackdweb.PackageVersion;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * A package.
 */
public class PackageDetailPage extends MyPage {
	private Package p;
	private List<PackageVersion> versions;
	private License license;
	private boolean editable;
	private List<License> licenses;

	/**
	 * @param p
	 *            a package
	 * @param editable
	 *            true if the data should be editable
	 */
	public PackageDetailPage(Package p, boolean editable) {
		this.p = p;
		this.editable = editable;

		Objectify ofy = ObjectifyService.begin();
		versions = new ArrayList<PackageVersion>();
		if (p != null) {
			for (PackageVersion pv : ofy.query(PackageVersion.class).filter(
					"package_ =", p.name).fetch())
				versions.add(pv);

			if (!p.license.isEmpty())
				this.license = ofy.get(License.class, p.license);
		}
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();
		w.start("h3");
		if (p.icon.isEmpty()) {
			w.e("img", "src", "/App.png");
		} else {
			w.e("img", "src", p.icon, "style",
					"max-width: 32px; max-height: 32px");
		}
		w.t(" " + p.title);
		w.end("h3");

		if (editable) {
			w.start("form", "method", "post", "action", "/package/save");
			w.e("input", "type", "hidden", "name", "name", "value", p.name);
		}

		w.start("table", "border", "0");
		w.start("tr");
		w.e("td", "ID:");
		w.e("td", p.name);
		w.end("tr");

		if (editable) {
			w.start("tr");
			w.e("td", "Title:");
			w.start("td");
			w.e("input", "type", "text", "name", "title", "value", p.title,
					"size", "80");
			w.end("td");
			w.end("tr");
		}

		w.start("tr");
		w.e("td", "Product home page:");
		w.start("td");
		if (editable) {
			w.e("input", "type", "text", "name", "url", "value", p.url, "size",
					"120");
		} else {
			w.e("a", "href", p.url, p.url);
		}
		w.end("td");
		w.end("tr");

		if (editable) {
			w.start("tr");
			w.e("td", "Icon:");
			w.start("td");
			w.e("input", "type", "text", "name", "icon", "value", p.icon,
					"size", "120");
			w.end("td");
			w.end("tr");
		}

		w.start("tr");
		w.e("td", "Description:");
		w.start("td");
		if (editable) {
			w.e("textarea", "rows", "5", "name", "description", "cols", "80",
					p.description);
		} else {
			w.t(p.description);
		}
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "License:");
		w.start("td");
		if (editable) {
			w.start("select", "name", "license");
			w.e("option", "value", "");
			for (License lic : this.getLicenses()) {
				w.e("option", "value", lic.name, "selected", lic.name
						.equals(p.license) ? "selected" : null, lic.title);
			}
			w.end("select");
		} else {
			if (p.license.isEmpty())
				w.t("unknown");
			else
				w.e("a", "href", getLicense().url, getLicense().title);
		}
		w.end("td");
		w.end("tr");

		if (editable) {
			w.start("tr");
			w.e("td", "Comment:");
			w.start("td");
			w.e("textarea", "rows", "5", "name", "comment", "cols", "80",
					p.comment);
			w.end("td");
			w.end("tr");
		}

		w.start("tr");
		w.e("td", "Versions:");
		w.start("td");
		List<PackageVersion> pvs = this.getVersions();
		for (int i = 0; i < pvs.size(); i++) {
			PackageVersion pv = pvs.get(i);
			if (i != 0)
				w.t(", ");
			w
					.e("a", "href", "/p/" + pv.package_ + "/" + pv.version,
							pv.version);
		}
		w.end("td");
		w.end("tr");
		w.end("table");

		if (editable) {
			w.e("input", "class", "input", "type", "submit", "value", "Save");
			NWUtils.jsButton(w, "Edit as XML", "/rep/edit-as-xml?package="
					+ p.name);
			w.end("form");
		}

		return w.toString();
	}

	@Override
	public String getTitle() {
		return "Package";
	}

	/**
	 * @return package shown on this page or null
	 */
	public Package getPackage() {
		return p;
	}

	/**
	 * @return versions of this package
	 */
	public List<PackageVersion> getVersions() {
		return versions;
	}

	/**
	 * @return license of the package or null
	 */
	public License getLicense() {
		return license;
	}

	/**
	 * @return true if the data should be editable
	 */
	public boolean getEditable() {
		return editable;
	}

	/**
	 * @return list of all licenses
	 */
	public List<License> getLicenses() {
		if (this.licenses == null) {
			Objectify ofy = ObjectifyService.begin();
			this.licenses = new ArrayList<License>();
			for (License p : ofy.query(License.class).fetch())
				this.licenses.add(p);
		}
		return licenses;
	}
}
