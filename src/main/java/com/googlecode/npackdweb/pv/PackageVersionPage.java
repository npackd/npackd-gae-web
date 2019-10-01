package com.googlecode.npackdweb.pv;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Dependency;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.db.Version;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import org.markdown4j.Markdown4jProcessor;

/**
 * Packages.
 */
public class PackageVersionPage extends MyPage {

    private Package package_;
    private License license;
    private boolean new_;

    private String packageName;
    private String version;
    private String url, sha;
    private List<String> dependencyPackages;
    private List<String> dependencyVersionRanges;
    private List<String> dependencyEnvVars;
    private boolean oneFile;
    private List<String> tags;
    private List<String> importantFilePaths;
    private List<String> importantFileTitles;
    private List<String> cmdFilePaths;
    private List<String> filePaths;
    private List<String> fileContents;
    private Date lastModifiedAt;
    private User lastModifiedBy;
    private Date createdAt;
    private User createdBy;
    private int installSucceeded, installFailed, uninstallSucceeded, uninstallFailed;
    private boolean currentUserIsAdmin;

    /**
     * -
     */
    public PackageVersionPage() {
        this.packageName = "";
        this.version = "";
        this.url = "";
        this.sha = "";
        this.dependencyPackages = new ArrayList<>();
        this.dependencyVersionRanges = new ArrayList<>();
        this.dependencyEnvVars = new ArrayList<>();
        this.oneFile = false;
        this.tags = new ArrayList<>();
        this.tags.add("not-reviewed");
        this.importantFilePaths = new ArrayList<>();
        this.importantFileTitles = new ArrayList<>();
        this.cmdFilePaths = new ArrayList<>();
        this.filePaths = new ArrayList<>();
        this.fileContents = new ArrayList<>();
        this.lastModifiedAt = null;

        UserService us = UserServiceFactory.getUserService();

        if (us.isUserLoggedIn()) {
            this.lastModifiedBy = us.getCurrentUser();
            this.currentUserIsAdmin = us.isUserAdmin();
        } else {
            this.lastModifiedBy =
                    new User(NWUtils.THE_EMAIL, "gmail.com");
        }
        this.createdBy = this.lastModifiedBy;
    }

    /**
     * @param pv a package version
     * @param new_ true = a new package version will be created, false = an an
     * existing package version will be edited
     */
    public PackageVersionPage(PackageVersion pv, boolean new_) {
        this();
        if (pv != null) {
            fillForm(pv);
        }
        this.new_ = new_;
    }

    private void fillForm(PackageVersion pv) {
        this.packageName = pv.package_;
        this.version = pv.version;
        this.url = pv.url;
        this.sha = pv.sha1;
        this.dependencyPackages = new ArrayList<>();
        this.dependencyPackages.addAll(pv.dependencyPackages);
        this.dependencyVersionRanges = new ArrayList<>();
        this.dependencyVersionRanges.addAll(pv.dependencyVersionRanges);
        this.dependencyEnvVars = new ArrayList<>();
        this.dependencyEnvVars.addAll(pv.dependencyEnvVars);
        this.oneFile = pv.oneFile;
        this.tags = new ArrayList<>();
        this.tags.addAll(pv.tags);
        this.importantFilePaths = new ArrayList<>();
        this.importantFilePaths.addAll(pv.importantFilePaths);
        this.importantFileTitles = new ArrayList<>();
        this.importantFileTitles.addAll(pv.importantFileTitles);
        this.cmdFilePaths = new ArrayList<>();
        this.cmdFilePaths.addAll(pv.cmdFilePaths);
        this.filePaths = new ArrayList<>();
        this.filePaths.addAll(pv.filePaths);
        this.fileContents = new ArrayList<>();
        for (int i = 0; i < this.filePaths.size(); i++) {
            this.fileContents.add(pv.getFileContents(i));
        }
        this.lastModifiedAt = pv.lastModifiedAt;
        this.lastModifiedBy = pv.lastModifiedBy;
        this.createdAt = pv.createdAt;
        this.createdBy = pv.createdBy;
        this.installSucceeded = pv.installSucceeded;
        this.installFailed = pv.installFailed;
        this.uninstallSucceeded = pv.uninstallSucceeded;
        this.uninstallFailed = pv.uninstallFailed;
    }

    private void createScripts(HTMLWriter w) {
        w.start("button", "class", "btn btn-default dropdown-toggle",
                "type", "button", "data-toggle", "dropdown");
        w.t("Create scripts ");
        w.e("span", "class", "caret");
        w.end("button");
        w.start("ul", "class", "dropdown-menu", "role", "menu");
        w.start("li");
        w.e("a", "href", "#", "title",
                "Adds the files necessary to install and " +
                "uninstall an installation package (.exe) " +
                "created using NSIS", "id", "addNSISFiles",
                "NSIS");
        w.end("li");
        w.start("li");
        w.e("a", "href", "#", "title",
                "Adds the files and dependencies necessary to install and " +
                "uninstall an installation package (.exe) " +
                "created using Inno Setup", "id",
                "addInnoSetupFiles", "Inno Setup");
        w.end("li");
        w.start("li");
        w.e("a", "href", "#", "title",
                "Adds the files and dependencies necessary to install and " +
                "uninstall an installation package (.msi) " +
                "created for the Microsoft Installer", "id",
                "addMSIFiles", "MSI");
        w.end("li");
        w.start("li");
        w.e("a", "href", "#", "title",
                "Adds the files and dependencies necessary to install and " +
                "uninstall a .zip archive with an additional top-level directory",
                "id",
                "addZIPDirFiles", ".zip with a top-level directory");
        w.end("li");
        w.start("li");
        w.e("a", "href", "#", "title",
                "Adds the files and dependencies necessary to install and " +
                "uninstall a .7z archive", "id",
                "addSevenZIPFiles", ".7z");
        w.end("li");
        w.start("li");
        w.e("a", "href", "#", "title",
                "Adds the files and dependencies necessary to uninstall a " +
                "program via its title in the Software Control Panel", "id",
                "addRemoveSCPFiles", "Uninstall via Software Control Panel");
        w.end("li");
        w.end("ul");
    }

    @Override
    public String createHead() throws IOException {
        if (!new_) {
            return "<link rel='canonical' href='" + NWUtils.WEB_SITE + "/p/" +
                    package_.name + "/" + version +
                    "'>";
        } else {
            return "";
        }
    }

    @Override
    public String getTitleHTML() {
        HTMLWriter w = new HTMLWriter();
        if (package_.icon.isEmpty()) {
            w.e("img", "src", "/App.png");
        } else {
            w.e("img", "src", package_.icon, "style",
                    "width: 32px; max-height: 32px");
        }
        w.t(" ");
        w.e("a", "href", "/p/" + package_.name, package_.title);
        w.t(" " + version);
        w.unencoded(
                " <div class='g-plusone' data-size='medium' data-annotation='inline' data-width='300' data-href='" +
                NWUtils.WEB_SITE + "/p/" + package_.name + "'></div>");
        return w.toString();
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter w = new HTMLWriter();

        Package p = getPackage();
        License lic = getLicense();

        if (tags.contains("not-reviewed")) {
            w.e("p", "class", "bg-danger",
                    "This package version is not yet reviewed and may be unsafe");
        }

        if (error != null) {
            w.e("p", "class", "bg-danger", this.error);
        }

        boolean editable = getEditable();
        if (editable) {
            w.start("form", "class", "form-horizontal", "method", "post",
                    "action", "/package-version/save");
            w.e("input", "type", "hidden", "name", "package",
                    "id", "package",
                    "value",
                    this.packageName);
            if (!new_) {
                w.start("div", "class", "btn-group");
                w.e("input", "class", "btn btn-default", "type", "submit",
                        "title",
                        "Saves the changes", "value", "Save", "id", "save");
                NWUtils.jsButton_(w, "Copy", "copyOnClick()",
                        "Create a copy of this package version");
                NWUtils.jsButton(w, "Edit as XML", "/rep/edit-as-xml?package=" +
                        packageName + "&version=" + version,
                        "Edits this package version as repository XML");
                NWUtils.jsButton_(w, "Delete", "deleteOnClick()",
                        "Deletes this package version");
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
                createScripts(w);
                w.end("div");

                if (currentUserIsAdmin) {
                    w.t(" ");
                    w.start("div", "class", "btn-group");
                    NWUtils.jsButton(
                            w,
                            "Mark as reviewed",
                            "/package-version/mark-reviewed?package=" +
                            packageName +
                            "&version=" + version,
                            "Marks this package version as reviewed");

                    NWUtils.jsButton(
                            w,
                            "Archive",
                            "/package-version/archive?package=" +
                            packageName +
                            "&version=" + version,
                            "Uploads the binary to archive.org");
                    w.end("div");
                }
            } else {
                w.start("div", "class", "btn-group");
                w.e("input", "class", "btn btn-default", "type", "submit",
                        "title",
                        "Saves the changes", "value", "Save", "id", "save");
                createScripts(w);
                w.end("div");
            }
        }

        w.start("table", "id", "fields", "itemscope", "itemscope", "itemtype",
                "http://schema.org/SoftwareApplication");

        // full internal name
        w.start("tr");
        w.e("td", "Full internal name:");
        w.start("td", "itemprop", "name");
        w.t(p.name);
        for (String s : p.screenshots) {
            w.e("meta", "itemprop", "screenshot", "content", s);
        }
        if (!p.icon.isEmpty()) {
            w.e("meta", "itemprop", "image", "content", p.icon);
        }
        w.e("meta", "itemprop", "operatingSystem", "content",
                "Microsoft Windows");
        w.e("meta", "itemprop", "applicationCategory", "content",
                p.tags.size() > 0 ? NWUtils.join(", ", p.tags) :
                "Uncategorized");
        w.end("td");
        w.end("tr");

        // project site
        w.start("tr");
        w.e("td", "Project site:");
        w.start("td");
        w.e("a", "itemprop", "sameAs", "id", "packageURL", "href", p.url, p.url);
        w.end("td");
        w.end("tr");

        // screenshots
        w.start("tr");
        w.e("td", "Screen shots:");
        w.start("td");
        for (String s : p.screenshots) {
            if (!s.trim().isEmpty()) {
                w.start("div", "class", "col-xs-6 col-md-3");
                w.start("a", "target", "_blank", "href", s, "class",
                        "thumbnail");
                w.e("img", "src", s, "alt", "Screen shot");
                w.end("a");
                w.end("div");
            }
        }
        w.end("td");
        w.end("tr");

        // download
        w.start("tr");
        w.e("td", "Download:");
        w.start("td");
        if (editable) {
            NWUtils.inputURL(w, "url", url,
                    "http: or https: address of the package binary");
            w.e("div",
                    "Example: https://ayera.dl.sourceforge.net/project/x64dbg/snapshots/snapshot_2018-03-04_22-52.zip");
        } else {
            if (!tags.contains("not-reviewed")) {
                w.start("a", "href",
                        url,
                        "class", "btn btn-primary btn-md",
                        "role", "button");
                w.e("span", "class", "glyphicon glyphicon-download");
                w.t(" Download " + p.title + " " + version);
                w.end("a");
                w.unencoded("<br><br>");
                w.e("a", "itemprop", "downloadUrl", "href", url, url);
            } else {
                w.t("Not yet reviewed");
            }
            if (!url.trim().isEmpty()) {
                w.unencoded("<br><br>");
                w.e("a", "href", "https://www.virustotal.com/#/url/" + NWUtils.
                        byteArrayToHexString(NWUtils.
                                stringSHA256(url)) + "/detection",
                        "VirusTotal results");
            }
        }
        w.end("td");
        w.end("tr");

        // change log
        w.start("tr");
        w.e("td", "Change log:");
        w.start("td");
        if (p.changelog != null && p.changelog.trim().length() > 0) {
            w.e("a", "itemprop", "releaseNotes", "id", "changelog", "href",
                    p.changelog, p.changelog);
        } else {
            w.t("n/a");
        }
        w.end("td");
        w.end("tr");

        // description
        w.start("tr");
        w.e("td", "Description:");
        Markdown4jProcessor mp = new Markdown4jProcessor();
        w.start("td", "itemprop", "description");
        try {
            w.unencoded(mp.process(p.description));
        } catch (IOException e) {
            w.t(p.description + " Failed to parse the Markdown syntax: " +
                    e.getMessage());
        }
        w.end("td");
        w.end("tr");

        // license
        w.start("tr");
        w.e("td", "License:");
        w.start("td");
        if (lic == null) {
            w.t("unknown");
        } else {
            w.e("a", "href", lic.url, lic.title);
        }
        w.end("td");
        w.end("tr");

        // version
        w.start("tr");
        w.e("td", "Version:");
        w.start("td", "itemprop", "softwareVersion");
        if (new_) {
            w.e("input", "type", "text", "name", "version", "value",
                    this.version, "size", "20");
            w.e("input", "type", "hidden", "name", "new", "value", "true");
        } else if (editable) {
            w.e("input", "type", "hidden", "name", "version",
                    "id", "version",
                    "value", version);
            w.t(version);
        } else {
            w.t(version);
        }
        w.end("td");
        w.end("tr");

        // SHA1
        w.start("tr");
        w.start("td");
        w.t("SHA-1 or SHA-256");
        if (editable) {
            w.e("small", " (optional)");
        }
        w.t(":");
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
                    "SHA-1 or SHA-256 (since Npackd 1.19) check sum for the package binary. " +
                    "Leave this field empty if different binaries are " +
                    "distributed from the same address.");
        } else {
            w.t(sha);
        }
        w.end("td");
        w.end("tr");

        // type
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

        if (editable) {
            w.start("tr");
            w.start("td");
            w.t("Dependencies");
            w.e("small", " (optional)");
            w.t(":");
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
            w.start("ul", "itemprop", "requirements");
            for (int i = 0; i < dependencyPackages.size(); i++) {
                Package dp = NWUtils.dsCache.getPackage(
                        dependencyPackages.get(i), true);

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
        w.start("td");
        w.t("Tags");
        if (editable) {
            w.e("small", " (optional)");
        }
        w.t(":");
        w.end("td");
        w.start("td", "itemprop", "keywords");
        if (editable) {
            for (int i = 0; i < PackageVersion.TAGS.length; i++) {
                String st = PackageVersion.TAGS[i];
                String title = PackageVersion.TAG_TOOLTIPS[i];
                w.start("label", "class", "checkbox-inline", "title", title);
                w.e("input", "type", "checkbox", "class", "nw-tag-checkbox",
                        "id", "tag-" +
                        st, "value", st);
                w.t(" " + st);
                w.end("label");
            }
            w.start("input", "class", "form-control", "type", "text", "name",
                    "tags", "id", "tags", "autocomplete", "off", "value",
                    NWUtils.join(", ", tags), "size", "80", "title",
                    "Comma separated list of tags associated with " +
                    "this package version. The repository " +
                    "names can be used to include this package " +
                    "version into them.");
            w.end("input");
        } else {
            for (int i = 0; i < tags.size(); i++) {
                String tag = tags.get(i);
                String title = PackageVersion.TAG_2_TOOLTIP.get(tag);
                if (i != 0) {
                    w.t(", ");
                }
                w.e("span", "title", title, tag);
            }
        }
        w.end("td");
        w.end("tr");

        // <important-file>
        if (editable) {
            w.start("tr");
            w.start("td");
            w.t("Important files");
            w.e("small", " (optional)");
            w.t(":");
            w.end("td");
            w.start("td");
            w.start("textarea",
                    "class",
                    "form-control nw-autosize",
                    "rows",
                    "5",
                    "name",
                    "importantFiles",
                    "cols",
                    "80",
                    "title",
                    "List of important files inside of the package. " +
                    "For each file mentioned here an entry in the Windows " +
                    "start menu will be created. Each line should contain " +
                    "one file name and the associated title separated by a " +
                    "space character.");
            for (int i = 0; i < importantFilePaths.size(); i++) {
                w.t(importantFilePaths.get(i) + " " +
                        importantFileTitles.get(i) + "\n");
            }
            w.end("textarea");
            w.end("td");
            w.end("tr");
        }

        // <cmd-file>
        if (editable) {
            w.start("tr");
            w.start("td");
            w.t("Command line tools");
            w.e("small", " (optional)");
            w.t(":");
            w.end("td");
            w.start("td");
            w.start("textarea",
                    "class",
                    "form-control nw-autosize",
                    "rows",
                    "5",
                    "name",
                    "cmdFiles",
                    "cols",
                    "80",
                    "title",
                    "List of command line tools inside of the package. " +
                    "For each file mentioned here a link in the directory " +
                    "%allusersprofile%\\Npackd\\Commands will be created. " +
                    "Each line should contain " +
                    "one path relative to the package root.");
            for (int i = 0; i < cmdFilePaths.size(); i++) {
                w.t(cmdFilePaths.get(i) + "\n");
            }
            w.end("textarea");
            w.end("td");
            w.end("tr");
        }

        // text files
        w.start("tr");
        w.start("td");
        w.t("Text files");
        if (editable) {
            w.e("small", " (optional)");
        }
        w.t(":");
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

                // don't forget to update PackageVersionDetail.js!
                w.e("div", "File content " + i + ":");
                w.e("textarea", "class", "form-control nw-autosize", "name",
                        "content." + i, "rows", "5", "cols", "80", "wrap",
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
        w.unencoded(lastModifiedBy == null ? "" : NWUtils.obfuscateEmail(
                lastModifiedBy.getEmail(), request.getServerName()));
        w.end("td");
        w.end("tr");

        w.start("tr");
        w.e("td", "Created:");
        w.start("td");
        w.t(createdAt == null ? "" : createdAt.toString());
        w.end("td");
        w.end("tr");

        w.start("tr");
        w.e("td", "Created by:");
        w.start("td");
        w.unencoded(createdBy == null ? "" : NWUtils.obfuscateEmail(createdBy.
                getEmail(), request.getServerName()));
        w.end("td");
        w.end("tr");

        // automated tests
        w.start("tr");
        w.e("td", "Automated tests:");
        w.start("td");
        w.t(installSucceeded + " of " + (installSucceeded + installFailed) +
                " installations succeeded, ");
        w.t(uninstallSucceeded + " of " + (uninstallSucceeded +
                uninstallFailed) + " removals succeeded");
        w.end("td");
        w.end("tr");

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
            this.package_ = NWUtils.dsCache.getPackage(
                    packageName, true);
            if (this.package_ == null) {
                this.package_ = new Package("unknown");
            }
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
     * @return associated license or null
     */
    public License getLicense() {
        if (this.license == null) {
            Package p = getPackage();
            if (!p.license.isEmpty()) {
                this.license = NWUtils.dsCache.getLicense(p.license, true);
                if (this.license == null) {
                    NWUtils.LOG.log(Level.WARNING,
                            "License {0} not found for {1}", new Object[]{
                                p.license, p.name});
                }
            }
        }
        return license;
    }

    @Override
    public void fill(HttpServletRequest req) {
        super.fill(req);

        this.new_ = "true".equals(req.getParameter("new"));
        packageName = req.getParameter("package");
        version = req.getParameter("version");

        url = req.getParameter("url");
        sha = req.getParameter("sha1");
        oneFile = "one-file".equals(req.getParameter("type"));
        tags = NWUtils.split(req.getParameter("tags"), ',');

        // <important-file>
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

        // <cmd-file>
        lines =
                NWUtils.splitLines(req.getParameter("cmdFiles").trim());
        cmdFilePaths.clear();
        cmdFilePaths.addAll(lines);

        this.filePaths.clear();
        this.fileContents.clear();
        for (int i = 0;; i++) {
            String path = req.getParameter("path." + i);
            if (path == null) {
                break;
            }

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
            String pp = req.getParameter("depPackage." + i);
            if (pp == null) {
                break;
            }

            if (!pp.trim().isEmpty()) {
                String versions = req.getParameter("depVersions." + i);
                String envVar = req.getParameter("depEnvVar." + i);
                dependencyPackages.add(pp);
                dependencyVersionRanges.add(versions);
                dependencyEnvVars.add(envVar);
            }
        }

        UserService us = UserServiceFactory.getUserService();
        if (us.isUserLoggedIn()) {
            this.lastModifiedBy = us.getCurrentUser();
        } else {
            this.lastModifiedBy =
                    new User(NWUtils.THE_EMAIL, "gmail.com");
        }
    }

    @Override
    public String validate() {
        String r = null;
        if (packageName.trim().length() == 0) {
            r = "Empty package name";
        }

        if (r == null) {
            if (version.trim().length() == 0) {
                r = "Empty version number";
            }
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
                PackageVersion p = NWUtils.dsCache.getPackageVersion(
                        packageName.trim() + "@" + v.toString());
                if (p != null) {
                    r = "Package version " + v + " already exists";
                }
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
                if (r != null) {
                    r = NWUtils.validateSHA256(this.sha);
                }
            }
        }

        if (r == null) {
            for (int i = 0; i < this.dependencyPackages.size(); i++) {
                r = Package.checkName(this.dependencyPackages.get(i));
                if (r != null) {
                    break;
                }
            }
        }

        if (r == null) {
            for (int i = 0; i < this.dependencyVersionRanges.size(); i++) {
                Dependency d = new Dependency();
                r = d.setVersions(this.dependencyVersionRanges.get(i));
                if (r != null) {
                    break;
                }
            }
        }

        if (r == null) {
            for (int i = 0; i < this.dependencyEnvVars.size(); i++) {
                r = NWUtils.validateEnvVarName(this.dependencyEnvVars.get(i));
                if (r != null) {
                    break;
                }
            }
        }

        if (r == null) {
            for (int i = 0; i < this.filePaths.size(); i++) {
                String p = this.filePaths.get(i);

                // TODO: incomplete
                if (p.trim().isEmpty()) {
                    r = "File path cannot be empty";
                }

                if (r != null) {
                    break;
                }
            }
        }

        if (r == null) {
            String detect = params.get("detect");
            if (detect == null) {
                detect = "";
            }
            List<String> lines = NWUtils.splitLines(detect);
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] parts = NWUtils.partition(line, " ");

                    // if there are many spaces between the package name and
                    // the version
                    parts[1] = parts[1].trim();

                    if (parts[1].isEmpty()) {
                        r = "Missing package version for detection";
                    } else {
                        r = Package.checkName(parts[0]);
                        if (r == null) {
                            try {
                                Version.parse(parts[1]);
                            } catch (NumberFormatException e) {
                                r = "Invalid version number for detection: " +
                                        e.getMessage();
                            }
                        }
                    }
                }
                if (r != null) {
                    break;
                }
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
     * @param error new error message or null
     */
    public void setErrorMessage(String error) {
        this.error = error;
    }

    /**
     * Transfers the data from this from into the specified object
     *
     * @param pv the object
     */
    public void fillObject(PackageVersion pv) {
        pv.package_ = this.packageName;
        pv.version = this.version;
        pv.name = this.packageName + "@" + this.version;
        pv.url = this.url;
        pv.sha1 = this.sha;
        pv.dependencyPackages = new ArrayList<>();
        pv.dependencyPackages.addAll(this.dependencyPackages);
        pv.dependencyVersionRanges = new ArrayList<>();
        pv.dependencyVersionRanges.addAll(this.dependencyVersionRanges);
        pv.dependencyEnvVars = new ArrayList<>();
        pv.dependencyEnvVars.addAll(this.dependencyEnvVars);
        pv.oneFile = this.oneFile;
        pv.tags = new ArrayList<>();
        pv.tags.addAll(this.tags);
        pv.importantFilePaths = new ArrayList<>();
        pv.importantFilePaths.addAll(this.importantFilePaths);
        pv.importantFileTitles = new ArrayList<>();
        pv.importantFileTitles.addAll(this.importantFileTitles);
        pv.cmdFilePaths = new ArrayList<>();
        pv.cmdFilePaths.addAll(this.cmdFilePaths);
        pv.filePaths = new ArrayList<>();
        pv.filePaths.addAll(this.filePaths);
        pv.clearFiles();
        for (int i = 0; i < this.filePaths.size(); i++) {
            pv.addFile(this.filePaths.get(i), this.fileContents.get(i));
        }

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
        w.unencoded(NWUtils.tmpl("PackageVersionDetail.js"));
        w.unencoded(NWUtils.tmpl("Common.js"));
        w.end("script");

        NWUtils.linkScript(w, "/autosize.min.js");

        return w.toString();
    }
}
