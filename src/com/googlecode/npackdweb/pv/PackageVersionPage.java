package com.googlecode.npackdweb.pv;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.markdown4j.Markdown4jProcessor;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.Dependency;
import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.Version;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Packages.
 */
public class PackageVersionPage extends MyPage {
	private Package package_;
	private License license;
	private boolean new_;

	private String packageName;
	private String version;
	private String url, sha, detectMSI;
	private List<String> dependencyPackages;
	private List<String> dependencyVersionRanges;
	private List<String> dependencyEnvVars;
	private boolean oneFile;
	private List<String> tags;
	private List<String> importantFilePaths;
	private List<String> importantFileTitles;
	private List<String> filePaths;
	private List<String> fileContents;
	private String downloadCheckError;
	private Date downloadCheckAt;
	private Date lastModifiedAt;
	private User lastModifiedBy;
	private boolean reviewed;

	/** error message or null */
	private String error;

	/**
	 * -
	 */
	public PackageVersionPage() {
		this.packageName = "";
		this.version = "";
		this.url = "";
		this.sha = "";
		this.detectMSI = "";
		this.dependencyPackages = new ArrayList<String>();
		this.dependencyVersionRanges = new ArrayList<String>();
		this.dependencyEnvVars = new ArrayList<String>();
		this.oneFile = false;
		this.tags = new ArrayList<String>();
		this.importantFilePaths = new ArrayList<String>();
		this.importantFileTitles = new ArrayList<String>();
		this.filePaths = new ArrayList<String>();
		this.fileContents = new ArrayList<String>();
		this.downloadCheckAt = null;
		this.downloadCheckError = null;
		this.lastModifiedAt = null;
		this.reviewed = false;

		UserService us = UserServiceFactory.getUserService();
		if (us.isUserLoggedIn())
			this.lastModifiedBy = us.getCurrentUser();
		else
			this.lastModifiedBy =
					new User("tim.lebedkov@gmail.com", "gmail.com");
	}

	/**
	 * @param pv
	 *            a package version
	 * @param new_
	 *            true = a new package version will be created, false = an an
	 *            existing package version will be edited
	 */
	public PackageVersionPage(PackageVersion pv, boolean new_) {
		this();
		if (pv != null)
			fillForm(pv);
		this.new_ = new_;
	}

	private void fillForm(PackageVersion pv) {
		this.packageName = pv.package_;
		this.version = pv.version.toString();
		this.url = pv.url;
		this.sha = pv.sha1;
		this.detectMSI = pv.detectMSI;
		this.dependencyPackages = new ArrayList<String>();
		this.dependencyPackages.addAll(pv.dependencyPackages);
		this.dependencyVersionRanges = new ArrayList<String>();
		this.dependencyVersionRanges.addAll(pv.dependencyVersionRanges);
		this.dependencyEnvVars = new ArrayList<String>();
		this.dependencyEnvVars.addAll(pv.dependencyEnvVars);
		this.oneFile = pv.oneFile;
		this.tags = new ArrayList<String>();
		this.tags.addAll(pv.tags);
		this.importantFilePaths = new ArrayList<String>();
		this.importantFilePaths.addAll(pv.importantFilePaths);
		this.importantFileTitles = new ArrayList<String>();
		this.importantFileTitles.addAll(pv.importantFileTitles);
		this.filePaths = new ArrayList<String>();
		this.filePaths.addAll(pv.filePaths);
		this.fileContents = new ArrayList<String>();
		for (int i = 0; i < this.filePaths.size(); i++) {
			this.fileContents.add(pv.getFileContents(i));
		}
		this.downloadCheckAt = pv.downloadCheckAt;
		this.downloadCheckError = pv.downloadCheckError;
		this.lastModifiedAt = pv.lastModifiedAt;
		this.reviewed = pv.reviewed;
		this.lastModifiedBy = pv.lastModifiedBy;
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		HTMLWriter w = new HTMLWriter();

		Objectify ofy = DefaultServlet.getObjectify();
		Package p = getPackage();
		License lic = getLicense(ofy);

		if (!reviewed)
			w.e("p", "class", "bg-danger",
					"This package version is not yet reviewed and may be unsafe");

		if (error != null) {
			w.e("p", "class", "bg-danger", this.error);
		}

		w.start("h3");
		if (p.icon.isEmpty()) {
			w.e("img", "src", "/App.png");
		} else {
			w.e("img", "src", p.icon, "style",
					"max-width: 32px; max-height: 32px");
		}
		w.t(" ");
		w.e("a", "href", "/p/" + p.name, p.title);
		w.unencoded(" <div class='g-plusone' data-size='medium' data-annotation='inline' data-width='300' data-href='" +
				"https://npackd.appspot.com/p/" + p.name + "'></div>");
		w.end("h3");

		boolean editable = getEditable();
		if (editable) {
			w.start("form", "class", "form-horizontal", "method", "post",
					"action", "/package-version/save");
			w.e("input", "type", "hidden", "name", "package", "value",
					this.packageName);
			w.start("div", "class", "btn-group");
			w.e("input", "class", "btn btn-default", "type", "submit", "title",
					"Saves the changes", "value", "Save", "id", "save");
			if (!new_) {
				w.e("input",
						"class",
						"btn btn-default",
						"type",
						"button",
						"value",
						"Copy",
						"title",
						"Create a copy of this package version",
						"onclick",
						"this.form.action='/package-version/copy'; this.form.submit()",
						"id", "copy");
				NWUtils.jsButton(w, "Edit as XML", "/rep/edit-as-xml?package=" +
						packageName + "&version=" + version,
						"Edits this package version as repository XML");
				w.e("input", "class", "btn btn-default", "type", "button",
						"title", "Delete this package version", "value",
						"Delete", "onclick",
						"this.form.action='/package-version/delete'; this.form.submit()");
				w.end("div");
				w.t(" ");

				w.start("div", "class", "btn-group");
				NWUtils.jsButton(w, "Compute SHA-1",
						"/package-version/compute-sha1?package=" + packageName +
								"&version=" + version,
						"Computes SHA1 for this package version");
				NWUtils.jsButton(w, "Compute SHA-256",
						"/package-version/compute-sha-256?package=" +
								packageName + "&version=" + version,
						"Computes SHA-256 for this package version (Npackd 1.19)");
				w.end("div");
				w.t(" ");

				w.start("div", "class", "btn-group");
				NWUtils.jsButton(
						w,
						"Recognize the installer",
						"/package-version/recognize?package=" + packageName +
								"&version=" + version,
						"downloads the binary and tries to recognize the used installer and create the necessary dependencies and scripts automatically");

				if (NWUtils.isAdminLoggedIn()) {
					NWUtils.jsButton(w, "Disable download check",
							"/package-version/dont-check-download?package=" +
									packageName + "&version=" + version,
							"Disables binary download check for this package version");
					NWUtils.jsButton(w, "Mark as reviewed",
							"/package-version/mark-reviewed?package=" +
									packageName + "&version=" + version,
							"Marks this package version as reviewed and safe");
				}
			}
			w.end("div");
		}

		w.start("table", "id", "fields");

		w.start("tr");
		w.e("td", "Full internal name:");
		w.e("td", p.name);
		w.end("tr");

		w.start("tr");
		w.e("td", "Project site:");
		w.start("td");
		w.e("a", "id", "packageURL", "href", p.url, p.url);
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "Description:");
		Markdown4jProcessor mp = new Markdown4jProcessor();
		w.start("td");
		try {
			w.unencoded(mp.process(p.description));
		} catch (IOException e) {
			w.t(p.description + " Failed to parse the Markdown syntax: " +
					e.getMessage());
		}
		w.end("td");
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
		w.start("td");
		if (new_) {
			w.e("input", "type", "text", "name", "version", "value",
					this.version, "size", "20");
			w.e("input", "type", "hidden", "name", "new", "value", "true");
		} else if (editable) {
			w.e("input", "type", "hidden", "name", "version", "value", version);
			w.t(version);
		} else {
			w.t(version);
		}
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "Download:");
		w.start("td");
		if (editable) {
			w.e("input", "style", "display: inline; width: 90%", "class",
					"form-control", "type", "text", "name", "url", "value",
					url, "size", "120", "id", "url", "title",
					"http: or https: address of the package binary");
			w.e("div", "class", "glyphicon glyphicon-link", "id", "url-link",
					"style",
					"cursor: pointer; font-size: 20px; font-weight: bold");
		} else {
			if (reviewed)
				w.e("a", "href", url, url);
			else
				w.t("Not yet reviewed");
		}
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.start("td");
		w.t("SHA-1 or SHA-256 ");
		w.e("small", "(optional):");
		w.end("td");
		w.start("td");
		if (editable) {
			w.e("input",
					"class",
					"form-control",
					"type",
					"text",
					"name",
					"sha1",
					"value",
					sha,
					"size",
					"50",
					"title",
					"SHA-1 or SHA-256 (since Npackd 1.19) check sum for the package binary. "
							+ "Leave this field empty if different binaries are "
							+ "distributed from the same address.");
		} else {
			w.t(sha);
		}
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.start("td");
		w.t("Detect MSI GUID ");
		w.e("small", "(optional):");
		w.end("td");
		w.start("td");
		if (editable) {
			w.e("input", "class", "form-control", "type", "text", "name",
					"detectMSI", "value", detectMSI, "size", "43", "title",
					"MSI package ID like "
							+ "{1ad147d0-be0e-3d6c-ac11-64f6dc4163f1}. "
							+ "Leave this field empty if the package does not "
							+ "install itself using the Microsoft installer.");
		} else {
			w.t(detectMSI);
		}
		w.end("td");
		w.end("tr");

		if (editable) {
			w.start("tr");
			w.start("td");
			w.t("Dependencies ");
			w.e("small", "(optional):");
			w.end("td");
			w.start("td");
			w.start("div", "class", "btn-group");
			w.e("button", "class", "btn btn-default", "type", "button", "id",
					"addDep", "title",
					"Adds a dependency entry on another package", "More");
			w.e("button", "class", "btn btn-default", "type", "button", "id",
					"removeDep", "title", "Removes the last dependency entry",
					"Less");
			w.end("div");
			w.start("table", "id", "deps");
			w.start("tbody");
			w.start("tr");
			w.e("td", "Full package name");
			w.e("td", "Range of versions");
			w.e("td", "Environment variable");
			w.end("tr");
			for (int i = 0; i < dependencyPackages.size(); i++) {
				String dp = dependencyPackages.get(i);
				String dvr = dependencyVersionRanges.get(i);
				String v = dependencyEnvVars.get(i);

				w.start("tr");
				w.start("td");
				w.e("input", "class", "form-control", "type", "text", "name",
						"depPackage." + i, "value", dp, "size", "80");
				w.end("td");
				w.start("td");
				w.e("input", "class", "form-control", "name", "depVersions." +
						i, "type", "text", "size", "20", "value", dvr);
				w.end("td");
				w.start("td");
				w.e("input", "class", "form-control", "name", "depEnvVar." + i,
						"type", "text", "size", "20", "value", v);
				w.end("td");
				w.end("tr");
			}
			w.end("tbody");
			w.end("table");
			w.end("td");
		} else {
			w.start("tr");
			w.e("td", "Dependencies:");
			w.start("td");
			w.start("ul");
			for (int i = 0; i < dependencyPackages.size(); i++) {
				Package dp =
						ofy.find(new com.googlecode.objectify.Key<Package>(
								Package.class, dependencyPackages.get(i)));

				w.start("li");
				w.e("a", "href", "/p/" + dependencyPackages.get(i),
						dp == null ? dependencyPackages.get(i) : dp.title);
				w.t(" ");
				w.t(dependencyVersionRanges.get(i));
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
			w.start("div", "class", "radio");
			w.start("label",
					"title",
					"the file will be downloaded and placed in the package directory under the name derived from the download URL");
			w.e("input", "type", "radio", "id", "oneFile", "name", "type",
					"value", "one-file", "checked", oneFile ? "checked" : null,
					"title",
					"The file may have any format and will be downloaded as-is.");
			w.t("one file");
			w.end("label");
			w.end("div");

			w.start("div", "class", "radio");
			w.start("label", "title",
					"the file will be downloaded and unzipped in the package directory");
			w.e("input", "type", "radio", "id", "zip", "name", "type", "value",
					"zip", "checked", !oneFile ? "checked" : null, "title",
					"The file must be in ZIP format and will be unpacked automatically.");
			w.t("zip");
			w.end("label");
			w.end("div");
		} else {
			w.t(oneFile ? "one file" : "zip");
		}
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.start("td");
		w.t("Tags ");
		w.e("small", "(optional):");
		w.end("td");
		w.start("td");
		if (editable) {
			w.start("input", "list", "allTags", "class", "form-control",
					"type", "text", "name", "tags", "id", "tags",
					"autocomplete", "off", "value", NWUtils.join(", ", tags),
					"size", "80", "title",
					"Comma separated list of tags associated with "
							+ "this package version. The default tags "
							+ "'stable', 'stable64', 'libs' and 'unstable' "
							+ "can be used to include this package "
							+ "version into one of the default repositories.");
			w.start("datalist", "id", "allTags");
			for (String s : new String[] { "stable", "stable64", "libs",
					"unstable" }) {
				w.e("option", "value", s);
			}
			w.end("datalist");
			w.end("input");
		} else {
			w.t(NWUtils.join(", ", tags));
		}
		w.end("td");
		w.end("tr");

		if (editable) {
			w.start("tr");
			w.start("td");
			w.t("Important files ");
			w.e("small", "(optional):");
			w.end("td");
			w.start("td");
			w.start("textarea",
					"class",
					"form-control",
					"rows",
					"5",
					"name",
					"importantFiles",
					"cols",
					"80",
					"title",
					"List of important files inside of the package. "
							+ "For each file mentioned here an entry in the Windows "
							+ "start menu will be created. Each line should contain "
							+ "one file name and the associated title separated by a "
							+ "space character.");
			for (int i = 0; i < importantFilePaths.size(); i++) {
				w.t(importantFilePaths.get(i) + " " +
						importantFileTitles.get(i) + "\n");
			}
			w.end("textarea");
			w.end("td");
			w.end("tr");
		}

		w.start("tr");
		w.start("td");
		w.t("Text files ");
		w.e("small", "(optional):");
		w.end("td");
		w.start("td");
		if (editable) {
			w.start("div", "class", "btn-group");
			w.e("button", "class", "btn btn-default", "type", "button", "id",
					"addFile", "title", "Adds a file entry", "More");
			w.e("button", "class", "btn btn-default", "type", "button", "id",
					"removeFile", "title", "Removes the last file entry",
					"Less");
			w.end("div");
			w.t(" ");

			w.start("div", "class", "btn-group");
			w.start("button", "class", "btn btn-default dropdown-toggle",
					"type", "button", "data-toggle", "dropdown");
			w.t("Add ");
			w.e("span", "class", "caret");
			w.end("button");
			w.start("ul", "class", "dropdown-menu", "role", "menu");
			w.start("li");
			w.e("a", "href", "#", "title",
					"Adds the files necessary to install and "
							+ "uninstall an installation package (.exe) "
							+ "created using NSIS", "id", "addNSISFiles",
					"Add NSIS files");
			w.end("li");
			w.start("li");
			w.e("a", "href", "#", "title",
					"Adds the files necessary to install and "
							+ "uninstall an installation package (.exe) "
							+ "created using Inno Setup", "id",
					"addInnoSetupFiles", "Add Inno Setup files");
			w.end("li");
			w.start("li");
			w.e("a", "href", "#", "title",
					"Adds the files necessary to install and "
							+ "uninstall an installation package (.msi) "
							+ "created for the Microsoft Installer", "id",
					"addMSIFiles", "Add MSI files");
			w.end("li");
			w.start("li");
			w.e("a", "href", "#", "title",
					"Adds the files necessary to install and "
							+ "uninstall a .7z archive", "id",
					"addSevenZIPFiles", "Add .7z files");
			w.end("li");
			w.start("li");
			w.e("a", "href", "#", "title",
					"Adds the files necessary to install and "
							+ "uninstall a Vim plugin", "id", "addVimFiles",
					"Add Vim plugin files");
			w.end("li");
			w.end("ul");
			w.end("div");
			w.end("td");
			w.end("tr");

			w.start("tr");
			w.e("td");
			w.start("td");
			w.start("div", "id", "files");
			for (int i = 0; i < filePaths.size(); i++) {
				String path = filePaths.get(i);
				String content = fileContents.get(i);

				w.start("div");
				w.e("div", "File path " + i + ":");
				w.e("input", "class", "form-control", "type", "text", "name",
						"path." + i, "value", path, "size", "80");

				w.e("div", "File content " + i + ":");
				w.e("textarea", "class", "form-control", "name",
						"content." + i, "rows", "20", "cols", "80", "wrap",
						"off", content);
				w.end("div");
			}
			w.end("div");
		} else {
			w.start("div", "class", "panel-group");
			w.start("div", "class", "panel panel-default");

			w.start("div", "class", "panel-heading");
			w.start("h4", "class", "panel-title");
			w.start("a", "data-toggle", "collapse", "data-parent",
					"#accordion", "href", "#collapseOne");
			w.t("Contents");
			w.end("a");
			w.end("h4");
			w.end("div");

			w.start("div", "id", "collapseOne", "class",
					"panel-collapse collapse");
			w.start("div", "class", "panel-body");
			for (int i = 0; i < filePaths.size(); i++) {
				String path = filePaths.get(i);
				String content = fileContents.get(i);

				w.start("div");
				w.e("div", path + ":");
				w.e("pre", content);
				w.end("div");
			}
			w.end("div");
			w.end("div");

			w.end("div");
			w.end("div");
		}
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "Last modified:");
		w.start("td");
		w.t(lastModifiedAt == null ? "" : lastModifiedAt.toString());
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "Last modified by:");
		w.start("td");
		w.unencoded(lastModifiedBy == null ? "" : NWUtils.obfuscateEmail(ofy,
				lastModifiedBy.getEmail()));
		w.end("td");
		w.end("tr");

		if (editable) {
			w.start("tr");
			w.e("td", "Download check:");
			w.start("td");
			if (downloadCheckAt != null) {
				if (PackageVersion.DONT_CHECK_THIS_DOWNLOAD
						.equals(downloadCheckError)) {
					w.t("Download check disabled");
				} else {
					String s = "Checked at " + downloadCheckAt + ". ";
					if (downloadCheckError == null)
						s += "The download completed successfully.";
					else
						s += "The download failed: " + downloadCheckError;
					w.t(s);
				}
			} else {
				w.t("Not yet checked");
			}
			w.end("td");
			w.end("tr");
		}

		w.end("table");

		if (editable) {
			w.end("form");
		}

		return w.toString();
	}

	@Override
	public String getTitle() {
		Package p = getPackage();
		return p.title + " " + version;
	}

	/**
	 * @return associated package
	 */
	public Package getPackage() {
		if (this.package_ == null) {
			Objectify objectify = DefaultServlet.getObjectify();
			this.package_ = objectify.find(Package.class, packageName);
			if (this.package_ == null)
				this.package_ = new Package("unknown");
		}
		return this.package_;
	}

	/**
	 * @return true if the data should be editable
	 */
	public boolean getEditable() {
		return UserServiceFactory.getUserService().getCurrentUser() != null;
	}

	/**
	 * @param ofy
	 *            Objectify
	 * @return associated license or null
	 */
	public License getLicense(Objectify ofy) {
		if (this.license == null) {
			Package p = getPackage();
			if (!p.license.isEmpty()) {
				this.license = ofy.find(License.class, p.license);
				if (this.license == null)
					NWUtils.LOG.log(Level.WARNING,
							"License {0} not found for {1}", new Object[] {
									p.license, p.name });
			}
		}
		return license;
	}

	public void fillForm(HttpServletRequest req) {
		this.new_ = "true".equals(req.getParameter("new"));
		packageName = req.getParameter("package");
		version = req.getParameter("version");

		url = req.getParameter("url");
		sha = req.getParameter("sha1");
		detectMSI = req.getParameter("detectMSI");
		oneFile = "one-file".equals(req.getParameter("type"));
		tags = NWUtils.split(req.getParameter("tags"), ',');
		List<String> lines =
				NWUtils.splitLines(req.getParameter("importantFiles"));
		importantFilePaths.clear();
		importantFileTitles.clear();
		for (String line : lines) {
			int pos = line.indexOf(" ");
			if (pos > 0) {
				String path = line.substring(0, pos);
				String title = line.substring(pos + 1);
				importantFilePaths.add(path);
				importantFileTitles.add(title);
			}
		}

		this.filePaths.clear();
		this.fileContents.clear();
		for (int i = 0;; i++) {
			String path = req.getParameter("path." + i);
			if (path == null)
				break;

			if (!path.trim().isEmpty()) {
				String content = req.getParameter("content." + i);
				this.filePaths.add(path);
				this.fileContents.add(content);
			}
		}

		dependencyPackages.clear();
		dependencyVersionRanges.clear();
		dependencyEnvVars.clear();
		for (int i = 0;; i++) {
			String package_ = req.getParameter("depPackage." + i);
			if (package_ == null)
				break;

			if (!package_.trim().isEmpty()) {
				String versions = req.getParameter("depVersions." + i);
				String envVar = req.getParameter("depEnvVar." + i);
				dependencyPackages.add(package_);
				dependencyVersionRanges.add(versions);
				dependencyEnvVars.add(envVar);
			}
		}

		if (!NWUtils.isAdminLoggedIn())
			this.reviewed = false;

		if (this.new_) {
			this.downloadCheckAt = null;
			this.downloadCheckError = null;
		} else {
			Objectify ofy = DefaultServlet.getObjectify();
			PackageVersion pv =
					PackageVersion.find(ofy, this.packageName, this.version);
			this.downloadCheckAt = pv.downloadCheckAt;
			this.downloadCheckError = pv.downloadCheckError;
		}

		UserService us = UserServiceFactory.getUserService();
		if (us.isUserLoggedIn())
			this.lastModifiedBy = us.getCurrentUser();
		else
			this.lastModifiedBy =
					new User("tim.lebedkov@gmail.com", "gmail.com");
	}

	@Override
	public String validate() {
		String r = null;
		if (packageName.trim().length() == 0)
			r = "Empty package name";

		if (r == null) {
			if (version.trim().length() == 0)
				r = "Empty version number";
		}

		if (r == null) {
			try {
				Version.parse(version);
			} catch (NumberFormatException e) {
				r = "Invalid version number: " + e.getMessage();
			}
		}

		if (r == null) {
			if (new_) {
				Version v = Version.parse(version);
				v.normalize();
				Objectify ofy = DefaultServlet.getObjectify();
				PackageVersion p =
						ofy.find(new Key<PackageVersion>(PackageVersion.class,
								packageName.trim() + "@" + v.toString()));
				if (p != null)
					r = "Package version " + v + " already exists";
			}
		}

		if (r == null) {
			if (!this.url.trim().isEmpty()) {
				r = NWUtils.validateURL(this.url);
			}
		}

		if (r == null) {
			if (!this.sha.trim().isEmpty()) {
				r = NWUtils.validateSHA1(this.sha);
				if (r != null)
					r = NWUtils.validateSHA256(this.sha);
			}
		}

		if (r == null) {
			if (!this.detectMSI.trim().isEmpty()) {
				r = NWUtils.validateGUID(this.detectMSI);
			}
		}

		if (r == null) {
			for (int i = 0; i < this.dependencyPackages.size(); i++) {
				r = Package.checkName(this.dependencyPackages.get(i));
				if (r != null)
					break;
			}
		}

		if (r == null) {
			for (int i = 0; i < this.dependencyVersionRanges.size(); i++) {
				Dependency d = new Dependency();
				r = d.setVersions(this.dependencyVersionRanges.get(i));
				if (r != null)
					break;
			}
		}

		if (r == null) {
			for (int i = 0; i < this.dependencyEnvVars.size(); i++) {
				r = NWUtils.validateEnvVarName(this.dependencyEnvVars.get(i));
				if (r != null)
					break;
			}
		}

		if (r == null) {
			for (int i = 0; i < this.filePaths.size(); i++) {
				String p = this.filePaths.get(i);

				// TODO: incomplete
				if (p.trim().isEmpty())
					r = "File path cannot be empty";

				if (r != null)
					break;
			}
		}

		return r;
	}

	/**
	 * @return full package name
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return entered version number
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param error
	 *            new error message or null
	 */
	public void setErrorMessage(String error) {
		this.error = error;
	}

	/**
	 * Transfers the data from this from into the specified object
	 * 
	 * @param pv
	 *            the object
	 */
	public void fillObject(PackageVersion pv) {
		pv.package_ = this.packageName;
		pv.version = this.version;
		pv.name = this.packageName + "@" + this.version;
		pv.url = this.url;
		pv.sha1 = this.sha;
		pv.detectMSI = this.detectMSI;
		pv.dependencyPackages = new ArrayList<String>();
		pv.dependencyPackages.addAll(this.dependencyPackages);
		pv.dependencyVersionRanges = new ArrayList<String>();
		pv.dependencyVersionRanges.addAll(this.dependencyVersionRanges);
		pv.dependencyEnvVars = new ArrayList<String>();
		pv.dependencyEnvVars.addAll(this.dependencyEnvVars);
		pv.oneFile = this.oneFile;
		pv.tags = new ArrayList<String>();
		pv.tags.addAll(this.tags);
		pv.importantFilePaths = new ArrayList<String>();
		pv.importantFilePaths.addAll(this.importantFilePaths);
		pv.importantFileTitles = new ArrayList<String>();
		pv.importantFileTitles.addAll(this.importantFileTitles);
		pv.filePaths = new ArrayList<String>();
		pv.filePaths.addAll(this.filePaths);
		pv.clearFiles();
		for (int i = 0; i < this.filePaths.size(); i++) {
			pv.addFile(this.filePaths.get(i), this.fileContents.get(i));
		}

		if (!this.reviewed)
			pv.reviewed = false;

		pv.lastModifiedBy = this.lastModifiedBy;
	}

	/**
	 * Normalizes the version.
	 */
	public void normalizeVersion() {
		Version v = Version.parse(this.version);
		v.normalize();
		this.version = v.toString();
	}

	@Override
	public String createBodyBottom(HttpServletRequest request)
			throws IOException {
		HTMLWriter w = new HTMLWriter();
		w.start("script");
		InputStream stream =
				DefaultServlet
						.getInstance(request)
						.getServletContext()
						.getResourceAsStream(
								"/WEB-INF/templates/PackageVersionDetail.js");
		w.unencoded(NWUtils.readUTF8Resource(stream));
		w.end("script");

		w.unencoded(NWUtils.tmpl("GooglePlus.html"));

		return w.toString();
	}
}
