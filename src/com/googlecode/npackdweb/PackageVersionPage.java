package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Objectify;

/**
 * Packages.
 */
public class PackageVersionPage extends MyPage {
	private PackageVersion pv;
	private Package package_;
	private License license;

	/**
	 * @param pv
	 *            a package version
	 */
	public PackageVersionPage(PackageVersion pv) {
		this.pv = pv;
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();
		Package p = getPackage();
		License lic = getLicense();

		w.start("h3");
		if (p.icon.isEmpty()) {
			w.e("img", "src", "/App.png");
		} else {
			w.e("img", "src", p.icon, "style",
					"max-width: 32px; max-height: 32px");
		}
		w.t(" " + p.title);
		w.end("h3");

		boolean editable = getEditable();
		if (editable) {
			w
					.start("form", "method", "post", "action",
							"/package-version/save");
			w.e("input", "type", "hidden", "name", "name", "value", pv.name);
		}

		w.start("table");

		w.start("tr");
		w.e("td", "Full internal name:");
		w.e("td", p.name);
		w.end("tr");

		w.start("tr");
		w.e("td", "Project site:");
		w.start("td");
		w.e("a", "href", p.url, p.url);
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "Description:");
		w.e("td", p.description);
		w.end("tr");

		w.start("tr");
		w.e("td", "License:");
		w.start("td");
		if (lic == null)
			w.t("unknown");
		else
			w.e("a", "href", lic.url, lic.title);
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "Version:");
		w.e("td", pv.version);
		w.end("tr");

		w.start("tr");
		w.e("td", "Download:");
		w.start("td");
		if (editable) {
			w.e("input", "type", "text", "name", "url", "value", pv.url,
					"size", "120");
		} else {
			w.e("a", "href", pv.url, pv.url);
		}
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "SHA1:");
		w.start("td");
		if (editable) {
			w.e("input", "type", "text", "name", "sha1", "value", pv.sha1,
					"size", "45");
		} else {
			w.t(pv.sha1);
		}
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "Detect MSI GUID:");
		w.start("td");
		if (editable) {
			w.e("input", "type", "text", "name", "detectMSI", "value",
					pv.detectMSI, "size", "43");
		} else {
			w.t(pv.detectMSI);
		}
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "Dependencies:");
		w.start("td");
		for (int i = 0; i < pv.dependencyPackages.size(); i++) {
			w.e("a", "href", "/p/" + pv.dependencyPackages.get(i), pv
					.getDependencyPackages().get(i));
			w.t(" ");
			w.t(pv.dependencyVersionRanges.get(i));
		}
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "Type:");
		w.start("td");
		if (editable) {
			w.e("input", "type", "radio", "name", "type", "value", "one-file",
					"checked", pv.oneFile ? "checked" : null, "one file");
			w.e("input", "type", "radio", "name", "type", "value", "zip",
					"checked", !pv.oneFile ? "checked" : null, "zip");
		} else {
			w.t(pv.oneFile ? "one file" : "zip");
		}
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "Tags:");
		w.start("td");
		if (editable) {
			w.e("input", "type", "text", "name", "tags", "value", NWUtils.join(
					", ", pv.tags), "size", "80");
		} else {
			w.t(NWUtils.join(", ", pv.tags));
		}
		w.end("td");
		w.end("tr");

		if (editable) {
			w.start("tr");
			w.e("td", "Important files:");
			w.start("td");
			w.start("textarea", "rows", "5", "name", "importantFiles", "cols",
					"80");
			for (int i = 0; i < pv.importantFilePaths.size(); i++) {
				w.t(pv.importantFilePaths.get(i) + " "
						+ pv.importantFileTitles.get(i) + "\n");
			}
			w.end("textarea");
			w.end("td");
			w.end("tr");
		}

		if (editable) {
			w.start("tr");
			w.e("td", "Text files:");
			w.start("td");
			for (int i = 0; i < pv.filePaths.size(); i++) {
				if (i != 0)
					w.t(", ");
				String c = pv.filePaths.get(i);
				w.e("a", "href", "/p/" + pv.package_ + "/" + pv.version
						+ "/file?path=" + pv.filePaths.get(i), c == null ? "-"
						: c);
			}
			w.end("textarea");
			w.end("td");
			w.end("tr");
		}

		w.end("table");

		if (editable) {
			w.e("input", "class", "input", "type", "submit", "value", "Save");
			w
					.e("input", "class", "input", "type", "button", "value",
							"Copy", "onclick",
							"this.form.action='/package-version/copy'; this.form.submit()");
			NWUtils.jsButton(w, "Edit as XML", "/rep/edit-as-xml?package="
					+ pv.package_ + "&version=" + pv.version);
			w
					.e("input", "class", "input", "type", "button", "value",
							"Delete", "onclick",
							"this.form.action='/package-version/delete'; this.form.submit()");
			w.end("form");
		}

		return w.toString();
	}

	@Override
	public String getTitle() {
		return "Package version";
	}

	/**
	 * @return associated package version
	 */
	public PackageVersion getPackageVersion() {
		return pv;
	}

	/**
	 * @return associated package
	 */
	public Package getPackage() {
		if (this.package_ == null) {
			Objectify objectify = NWUtils.OBJECTIFY.get();
			this.package_ = objectify.get(Package.class, this.pv.package_);
		}
		return this.package_;
	}

	/**
	 * @return true if the data should be editable
	 */
	public boolean getEditable() {
		return NWUtils.isEditorLoggedIn();
	}

	/**
	 * @return associated license or null
	 */
	public License getLicense() {
		if (this.license == null) {
			Package p = getPackage();
			if (!p.license.isEmpty()) {
				Objectify ofy = NWUtils.OBJECTIFY.get();
				this.license = ofy.get(License.class, p.license);
			}
		}
		return license;
	}
}
