package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Key;
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
		w
		        .unencoded("<iframe src=\"javascript:''\" id=\"__gwt_historyFrame\" tabIndex='-1' style=\"position:absolute;width:0;height:0;border:0\"></iframe>");

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

		w.start("table", "id", "fields");

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
			        "size", "50");
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

		if (editable) {
			w.start("tr");
			w.e("td", "Dependencies:");
			w.start("td");
			w.e("button", "type", "button", "id", "addDep", "More");
			w.e("button", "type", "button", "id", "removeDep", "Less");
			w.start("ul", "id", "deps");
			for (int i = 0; i < pv.dependencyPackages.size(); i++) {
				String dp = pv.dependencyPackages.get(i);
				String dvr = pv.dependencyVersionRanges.get(i);

				w.start("li");
				w.e("input", "type", "text", "name", "depPackage." + i,
				        "value", dp, "size", "80");
				w.e("input", "name", "depVersions." + i, "type", "text",
				        "size", "20", "value", dvr);
				w.end("li");
			}
			w.end("ul");
			w.end("td");
		} else {
			w.start("tr");
			w.e("td", "Dependencies:");
			w.start("td");
			w.start("ul");
			for (int i = 0; i < pv.dependencyPackages.size(); i++) {
				Objectify ofy = NWUtils.getObjectify();
				Package dp = ofy.find(new Key<Package>(Package.class,
				        pv.dependencyPackages.get(i)));

				w.start("li");
				w
				        .e("a", "href", "/p/" + pv.dependencyPackages.get(i),
				                dp.title);
				w.t(" ");
				w.t(pv.dependencyVersionRanges.get(i));
				w.end("li");
			}
			w.end("ul");
			w.end("td");
			w.end("tr");
		}

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
			w.e("button", "type", "button", "id", "addFile", "More");
			w.e("button", "type", "button", "id", "removeFile", "Less");
			w.e("button", "type", "button", "id", "addNSISFiles",
			        "Add NSIS files");
			w.e("button", "type", "button", "id", "addInnoSetupFiles",
			        "Add Inno Setup files");
			w.end("td");
			w.end("tr");

			w.start("tr");
			w.e("td");
			w.start("td");
			w.start("div", "id", "files");
			for (int i = 0; i < pv.filePaths.size(); i++) {
				String path = pv.filePaths.get(i);
				String content = pv.getFileContents(i);

				w.start("div");
				w.e("div", "File path " + i + ":");
				w.e("input", "type", "text", "name", "path." + i, "value",
				        path, "size", "80");

				w.e("div", "File content " + i + ":");
				w.e("textarea", "name", "content." + i, "rows", "20", "cols",
				        "80", "wrap", "off", content);
				w.end("div");
			}
			w.end("div");
			w.end("td");
			w.end("tr");
		}

		if (editable) {
			w.start("tr");
			w.e("td", "Download check:");
			w.start("td");
			if (pv.downloadCheckAt != null) {
				String s = "Checked at " + pv.downloadCheckAt + ". ";
				if (pv.downloadCheckError == null)
					s += "The download completed successfully.";
				else
					s += "The download failed: " + pv.downloadCheckError;
				w.t(s);
			} else {
				w.t("Not yet checked");
			}
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
			Objectify objectify = NWUtils.getObjectify();
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
				Objectify ofy = NWUtils.getObjectify();
				this.license = ofy.get(License.class, p.license);
			}
		}
		return license;
	}

	@Override
	public String getHeadPart() {
		return "<script type=\"text/javascript\" language=\"javascript\" src=\"/com.googlecode.npackdweb.pv.PVEditor/com.googlecode.npackdweb.pv.PVEditor.nocache.js\"></script>\n";
	}
}
