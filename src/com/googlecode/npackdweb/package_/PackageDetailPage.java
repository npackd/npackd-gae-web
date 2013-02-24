package com.googlecode.npackdweb.package_;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.markdown4j.Markdown4jProcessor;

import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.License;
import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.Package;
import com.googlecode.npackdweb.PackageVersion;
import com.googlecode.npackdweb.QueryCache;
import com.googlecode.npackdweb.Version;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

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
	 *            a package or null
	 * @param editable
	 *            true if the data should be editable
	 */
	public PackageDetailPage(Package p, boolean editable) {
		this.p = p;
		this.editable = editable;

		Objectify ofy = NWUtils.getObjectify();
		versions = new ArrayList<PackageVersion>();
		if (p != null) {
			for (PackageVersion pv : ofy.query(PackageVersion.class).filter(
			        "package_ =", p.name).fetch())
				versions.add(pv);

			if (!p.license.isEmpty())
				this.license = ofy.find(License.class, p.license);
		}
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();
		w.start("h3");
		if (p == null || p.icon.isEmpty()) {
			w.e("img", "src", "/App.png");
		} else {
			w.e("img", "src", p.icon, "style",
			        "max-width: 32px; max-height: 32px");
		}
		if (p != null)
			w.t(" " + p.title);
		else
			w.t(" New package");
		w.end("h3");

		if (editable) {
			w.start("form", "method", "post", "action", "/package/save");
			if (p != null)
				w.e("input", "type", "hidden", "name", "name", "value", p.name);
		}

		w.start("table", "border", "0");
		w.start("tr");
		w.e("td", "ID:");
		if (p != null)
			w.e("td", p.name);
		else {
			w.start("td");
			w.e("input", "type", "text", "name", "name", "value", "", "size",
			        "80");
			w.end("td");
		}
		w.end("tr");

		if (editable) {
			w.start("tr");
			w.e("td", "Title:");
			w.start("td");
			w.e("input", "type", "text", "name", "title", "value",
			        p == null ? "" : p.title, "size", "80");
			w.end("td");
			w.end("tr");
		}

		w.start("tr");
		w.e("td", "Product home page:");
		w.start("td");
		if (editable) {
			w.e("input", "type", "text", "name", "url", "value", p == null ? ""
			        : p.url, "size", "120");
			if (p != null && !p.url.isEmpty()) {
				w.start("a", "href", p.url, "target", "_blank");
				w.e("img", "src", "/Link.png");
				w.end("a");
			}
		} else {
			w.e("a", "href", p.url, p.url);
		}
		w.end("td");
		w.end("tr");

		if (editable) {
			w.start("tr");
			w.e("td", "Icon:");
			w.start("td");
			w.e("input", "type", "text", "name", "icon", "value",
			        p == null ? "" : p.icon, "size", "120");
			w.end("td");
			w.end("tr");
		}

		w.start("tr");
		w.e("td", "Description:");
		w.start("td");
		if (editable) {
			w.t("You can use the ");
			w.e("a", "href",
			        "http://daringfireball.net/projects/markdown/syntax",
			        "target", "_blank", "Markdown syntax");
			w.t(" in the following text area");
			w.e("br");
			w.e("textarea", "rows", "10", "name", "description", "cols", "80",
			        p == null ? "" : p.description);
		} else {
			Markdown4jProcessor mp = new Markdown4jProcessor();
			try {
				w.unencoded(mp.process(p.description));
			} catch (IOException e) {
				w.t(p.description + " Failed to parse the Markdown syntax: "
				        + e.getMessage());
			}
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
				w.e("option", "value", lic.name, "selected", p != null
				        && lic.name.equals(p.license) ? "selected" : null,
				        lic.title);
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
			        p == null ? "" : p.comment);
			w.end("td");
			w.end("tr");
		}

		w.start("tr");
		w.e("td", "Versions:");
		w.start("td");
		List<PackageVersion> pvs = this.getVersions();
		Collections.sort(pvs, new Comparator<PackageVersion>() {
			public int compare(PackageVersion a, PackageVersion b) {
				Version va = Version.parse(a.version);
				Version vb = Version.parse(b.version);
				return va.compare(vb);
			}
		});
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

		w.start("tr");
		w.e("td", "Created:");
		w.start("td");
		w.t(p == null ? "" : p.createdAt.toString());
		w.end("td");
		w.end("tr");

		if (editable) {
			w.start("tr");
			w.e("td", "Created by:");
			w.start("td");
			w
			        .t(p == null ? UserServiceFactory.getUserService()
			                .getCurrentUser().getNickname() : p.createdBy
			                .getNickname());
			w.end("td");
			w.end("tr");

			w.start("tr");
			w.e("td", "Discovery page (URL):");
			w.start("td");
			w.e("input", "type", "text", "name", "discoveryPage", "value",
			        p == null ? "" : p.discoveryPage, "size", "120");
			w.end("td");
			w.end("tr");

			w.start("tr");
			w.e("td", "Discovery regular expression:");
			w.start("td");
			w.e("input", "type", "text", "name", "discoveryRE", "value",
			        p == null ? "" : p.discoveryRE, "size", "40");
			w.end("td");
			w.end("tr");

			w.start("tr");
			w.e("td", "Discovery URL pattern:");
			w.start("td");
			w.e("input", "type", "text", "name", "discoveryURLPattern",
			        "value", p == null ? "" : p.discoveryURLPattern, "size",
			        "120");
			w.end("td");
			w.end("tr");
		}

		w.end("table");

		if (editable) {
			w.e("input", "class", "input", "type", "submit", "value", "Save");
			if (p != null) {
				NWUtils.jsButton(w, "Edit as XML", "/rep/edit-as-xml?package="
				        + p.name);
				NWUtils.jsButton(w, "Delete", "/package/delete?id=" + p.name);
			}
			w.end("form");
		}

		return w.toString();
	}

	@Override
	public String getTitle() {
		return p == null ? "Package" : p.title;
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
			Objectify ofy = NWUtils.getObjectify();
			this.licenses = new ArrayList<License>();
			String cacheSuffix = "@" + DefaultServlet.dataVersion.get();
			Query<License> q = ofy.query(License.class).order("title");
			List<Key<License>> keys = QueryCache.getKeys(ofy, q, cacheSuffix);
			Map<Key<License>, License> k2v = ofy.get(keys);
			this.licenses.addAll(k2v.values());
		}
		return licenses;
	}
}
