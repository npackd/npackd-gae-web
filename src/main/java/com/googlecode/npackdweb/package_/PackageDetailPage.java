package com.googlecode.npackdweb.package_;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.FormMode;
import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.db.Version;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.http.HttpServletRequest;
import org.markdown4j.Markdown4jProcessor;

/**
 * A package.
 */
public class PackageDetailPage extends MyPage {

    /**
     * full package id
     */
    public String id;

    /**
     * package title
     */
    public String title;

    /**
     * package home page
     */
    public String url;

    /**
     * package change log URL
     */
    public String changelog;

    /**
     * package icon
     */
    public String icon;

    /**
     * package comment
     */
    public String comment;

    /**
     * discovery URL
     */
    public String discoveryURL;

    /**
     * discovery regular expression
     */
    public String discoveryRE;

    /**
     * package category
     */
    public String category;

    /**
     * pattern for the download URL
     */
    public String discoveryURLPattern;

    /**
     * mode for this form
     */
    public FormMode mode;

    /**
     * date and time of the package creation or null for new packages
     */
    public Date createdAt;

    /**
     * user that created the package or null
     */
    public User createdBy;

    /**
     * license ID
     */
    public String license;

    /**
     * list of tags
     */
    private List<String> tags;

    /**
     * list of screenshot URLs one per line
     */
    public String screenshots;

    /**
     * multi-line list emails
     */
    public String permissions;

    /**
     * see Package.noUpdatesCheck
     */
    private Date noUpdatesCheck;

    private boolean starFilled;
    private int starred;

    /**
     * @param mode editing mode
     */
    public PackageDetailPage(FormMode mode) {
        this.mode = mode;

        id = "";
        title = "";
        url = "";
        changelog = "";
        icon = "";
        comment = "";
        discoveryURL = "";
        discoveryRE = "";
        discoveryURLPattern = "";
        license = "";
        this.category = "";
        this.tags = new ArrayList<>();
        screenshots = "";
        permissions = "";

        User u = UserServiceFactory.getUserService().getCurrentUser();
        this.params.put("modified", new Date().toString());
        this.params.put("modifiedBy", u == null ? "" : u.getEmail());
    }

    @Override
    public String createHead() throws IOException {
        if (this.mode != FormMode.CREATE) {
            return "<link rel='canonical' href='" + NWUtils.WEB_SITE + "/p/" +
                    id +
                    "'>";
        } else {
            return "";
        }
    }

    @Override
    public String createBodyBottom(HttpServletRequest request)
            throws IOException {
        HTMLWriter w = new HTMLWriter();
        w.start("script");
        w.unencoded(NWUtils.tmpl("PackageDetail.js"));
        w.end("script");

        NWUtils.linkScript(w, "/autosize.min.js");

        return w.toString();
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter w = new HTMLWriter();
        w.start("form", "class", "form-horizontal", "method", "post",
                "action", "/package/save", "id", "package-form");

        if (mode == FormMode.EDIT) {
            w.e("input", "type", "hidden", "id", "name", "name", "name",
                    "value", id);
        }

        if (mode == FormMode.CREATE) {
            w.e("input", "type", "hidden", "name", "new", "value", "true");
        }

        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.start("div", "class", "btn-group", "style", "margin-bottom: 12px");
            w.e("input", "class", "btn btn-default", "type", "submit", "value",
                    "Save");
        }

        if (mode == FormMode.EDIT) {
            NWUtils.jsButton(w, "Edit as XML", "/rep/edit-as-xml?package=" +
                    id, "Edit this package as repository XML");
            NWUtils.jsButton_(w, "Delete", "deleteOnClick()",
                    "Deletes this package and all associated versions");
            NWUtils.jsButton_(w, "Rename", "renameOnClick()",
                    "Rename this package");
            NWUtils.jsButton(w, "New version", "/p/" + id + "/new",
                    "Creates new version");
            NWUtils.jsButton(
                    w,
                    "Detect new version",
                    "/p/" + id + "/detect",
                    "Uses the discovery page (URL) and discovery regular expression to identify a newer version of the package",
                    this.isDetectionPossible());
            Package p = NWUtils.dsCache.getPackage(id, false);
            if (p != null) {
                NWUtils.jsButton(
                        w,
                        "Request access",
                        "/request-permissions?package=" + id,
                        "Request write access to this package",
                        !p.isCurrentUserPermittedToModify());
            }
            NWUtils.jsButton(w, "Next package", "/package/next?name=" + id,
                    "Shows the next package ordered by title");
        }

        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.end("div");
        }

        // title
        if (mode != FormMode.VIEW) {
            startRow(w, "Title", false);
            w.e("input", "class", "form-control", "type", "text", "name",
                    "title", "value", title, "size", "80", "title",
                    "Name of the package", "id", "title");
            endRow(w);
        }

        // package name
        startRow(w, "ID", false);
        if (mode != FormMode.CREATE) {
            w.start("p", "class", "form-control-static");
            w.t(id);
            w.
                    e("span", "class", "glyphicon glyphicon-search", "id",
                            "name-link",
                            "style",
                            "cursor: pointer; font-size: 20px; font-weight: bold",
                            "title", "Search on Repology");
            w.end("p");
        } else {
            w.e("input", "class", "form-control", "type", "text", "name",
                    "name", "value", id, "size", "80", "id", "name",
                    "style", "display: inline; width: 50%",
                    "title",
                    "Package name");
            w.e("div", "class", "glyphicon glyphicon-search", "id", "name-link",
                    "style",
                    "cursor: pointer; font-size: 20px; font-weight: bold",
                    "title", "Search on Repology");
            w.start("p", "class", "nw-help");
            w.t(" See ");
            w.e("a",
                    "href",
                    "https://github.com/tim-lebedkov/npackd/wiki/RepositoryFormat#package-naming-rules",
                    "target", "_blank", "Package naming rules");
            w.t(" for more details");
            w.end("p");
        }
        endRow(w);

        // versions
        startRow(w, "Versions", false);
        w.start("p", "class", "form-control-static");
        List<PackageVersion> pvs = this.getVersions();
        Collections.sort(pvs, new Comparator<PackageVersion>() {
            @Override
            public int compare(PackageVersion a, PackageVersion b) {
                Version va  = Version.parse(a.version);
                Version vb = Version.parse(b.version);
                return -va.compare(vb);
            }
        });
        for (int i = 0; i < pvs.size(); i++) {
            PackageVersion pv = pvs.get(i);
            if (i != 0) {
                w.t(" | ");
            }
            w.e("a", "href", "/p/" + pv.package_ + "/" + pv.version, pv.version);
        }
        if ((mode == FormMode.EDIT || mode == FormMode.CREATE) &&
                error == null && id != null && !id.isEmpty() && pvs.size() == 0) {
            info =
                    "Click on \"New version\" to create a new version of this package";
        } else {
            info = null;
        }
        w.end("p");
        endRow(w);

        // home page
        startRow(w, "Product home page", false);
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            NWUtils.inputURL(w, "url", url,
                    "http: or https: address of the product home page");
        } else {
            w.start("p", "class", "form-control-static");
            w.e("a", "href", url, url);
            w.end("p");
        }
        endRow(w);

        // change log
        startRow(w, "Change log", mode == FormMode.EDIT ||
                mode == FormMode.CREATE);
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            NWUtils.inputURL(w, "changelog", changelog,
                    "http: or https: address of the package change log");
        } else {
            w.start("p", "class", "form-control-static");
            w.e("a", "href", changelog, changelog);
            w.end("p");
        }
        endRow(w);

        // icon
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            startRow(w, "Icon", true);
            NWUtils.inputURL(w, "icon", icon,
                    "http: or https: address of a 32x32 PNG icon representing this package");
            endRow(w);
        }

        // screen shots
        startRow(w, "Screen shots", mode == FormMode.EDIT ||
                mode == FormMode.CREATE);
        w.start("div", "class", "row");
        for (String s : NWUtils.splitLines(this.screenshots)) {
            if (!s.trim().isEmpty()) {
                w.start("div", "class", "col-xs-6 col-md-3");
                w.start("a", "target", "_blank", "href", s, "class",
                        "thumbnail");
                w.e("img", "src", s, "alt", "Screen shot");
                w.end("a");
                w.end("div");
            }
        }
        w.end("div");
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.e("textarea", "class", "form-control nw-autosize", "rows", "4",
                    "name",
                    "screenshots", "cols", "80", "title",
                    "List of screen shot URLs. " +
                    "Each URL must be on a separate line. " +
                    "Only https: and http: protocols are allowed. " +
                    "Only PNG images are allowed.", screenshots);
        }
        endRow(w);

        // issues
        startRow(w, "Issue tracker", mode == FormMode.EDIT ||
                mode == FormMode.CREATE);
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            NWUtils.inputURL(w, "issues", params.get("issues"),
                    "http: or https: address of the package issue tracker");
        } else {
            w.start("p", "class", "form-control-static");
            w.e("a", "href", params.get("issues"), params.get("issues"));
            w.end("p");
        }
        endRow(w);

        // description
        startRow(w, "Description", false);
        String description = params.get("description");
        if (description == null) {
            description = "";
        }
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.start("p", "class", "nw-help");
            w.e("a", "href",
                    "http://daringfireball.net/projects/markdown/syntax",
                    "target", "_blank", "Markdown syntax");
            w.t(" can be used in the following text area");
            w.end("p");
            w.e("textarea", "class", "form-control nw-autosize", "rows", "5",
                    "name",
                    "description", "cols", "80", "title",
                    "Possibly long description of the package. " +
                    "Try to not repeat the package name here and " +
                    "keep it simple and informative.", description);
        } else {
            Markdown4jProcessor mp = new Markdown4jProcessor();
            try {
                w.unencoded(mp.process(description));
            } catch (IOException e) {
                w.t(description + " Failed to parse the Markdown syntax: " +
                        e.getMessage());
            }
        }
        endRow(w);

        // license
        startRow(w, "License", mode == FormMode.EDIT ||
                mode == FormMode.CREATE);
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.start("select", "class", "form-control", "name", "license",
                    "title", "Package licensing terms");
            w.e("option", "value", "");
            for (License lic : this.getLicenses()) {
                w.e("option", "value", lic.name, "selected",
                        lic.name.equals(license) ? "selected" : null, lic.title);
            }
            w.end("select");
        } else {
            w.start("p", "class", "form-control-static");
            License license_ = null;
            if (!license.isEmpty()) {
                license_ = NWUtils.dsCache.getLicense(license, false);
            }

            if (license_ == null) {
                w.t("unknown");
            } else {
                w.e("a", "href", license_.url, license_.title);
            }
            w.end("p");
        }
        endRow(w);

        // category
        startRow(w, "Category", mode == FormMode.EDIT ||
                mode == FormMode.CREATE);
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.start("input",
                    "list",
                    "category-datalist",
                    "class",
                    "form-control",
                    "type",
                    "text",
                    "name",
                    "category",
                    "value",
                    category,
                    "size",
                    "40",
                    "title",
                    "package category. Sub-categories can be defined using slashes as in Video/Encoders.");
            w.start("datalist", "id", "category-datalist");
            for (int i = 0; i < Package.CATEGORIES.length; i++) {
                String s = Package.CATEGORIES[i];
                String title_ = Package.CATEGORIES_TOOLTIPS[i];
                w.e("option", "value", s, "title", title_);
            }
            w.end("datalist");
            w.end("input");
        } else {
            w.start("p", "class", "form-control-static");
            w.t(category);
            w.end("p");
        }
        endRow(w);

        // tags
        startRow(w, "Tags", mode == FormMode.EDIT ||
                mode == FormMode.CREATE);
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            // PackageDetail.js should also be updated if the list of
            // default categories/tags changes
            for (int i = 0; i < Package.TAGS.length; i++) {
                String s = Package.TAGS[i];
                String title_ = Package.TAG_TOOLTIPS[i];
                w.start("label", "class", "checkbox-inline", "title", title_);
                w.e("input", "type", "checkbox", "id", "tag-" + s, "value", s);
                w.t(" " + s);
                w.end("label");
            }
            w.start("input", "class", "form-control",
                    "style", "margin-top: 24px",
                    "type", "text", "name",
                    "tags", "id", "tags", "autocomplete", "off", "value",
                    NWUtils.join(", ", tags), "size", "80", "title",
                    "Comma separated list of tags/categories associated with " +
                    "this package version. " +
                    "Sub-categories can be defined using slashes as in Video/Encoders. " +

                    "Please note that only the first category and sub-category will be used in Npackd."
            );
            w.end("input");
        } else {
            w.start("p", "class", "form-control-static");
            w.t(NWUtils.join(", ", tags));
            w.end("p");
        }
        endRow(w);

        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            startRow(w, "Comment", true);
            w.e("textarea",
                    "class",
                    "form-control nw-autosize",
                    "rows",
                    "5",
                    "name",
                    "comment",
                    "cols",
                    "80",
                    "title",
                    "Internal comments normally only visible to the package editors",
                    comment);
            endRow(w);
        }

        // discovery page
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            startRow(w, "Discovery page (URL)", true);
            NWUtils.inputURL(w, "discoveryPage", discoveryURL,
                    "http: or https: URL of an HTML or text page that contains the newest version number as text");
            endRow(w);

            startRow(w, "Discovery regular expression", true);
            w.start("input",
                    "list",
                    "discovery-res",
                    "class",
                    "form-control",
                    "type",
                    "text",
                    "name",
                    "discoveryRE",
                    "value",
                    discoveryRE,
                    "size",
                    "80",
                    "title",
                    "Regular expression to match the newest version number. This regular expression should contain a match group for the version number. A single letter at the end of the version number is allowed (2.0.6b will be interpreted as 2.0.6.2). Minus characters and underscores will be interpreted as dots.\n" +

                    "This regular expression will be applied to all lines in the file one-by-one until a match is found.\n" +
                    "Example: <h1>the newest version is ([\\d\\.]+)</h1>");
            w.start("datalist", "id", "discovery-res");
            w.e("option", "value", "The current version is ([\\d\\.]+)");
            w.e("option", "value", ">v([\\d\\.]+)<");
            w.end("datalist");
            w.end("input");
            endRow(w);

            startRow(w, "Discovery package download URL pattern", true);
            w.start("input",
                    "list",
                    "discovery-url",
                    "class",
                    "form-control",
                    "type",
                    "text",
                    "name",
                    "discoveryURLPattern",
                    "value",
                    discoveryURLPattern,
                    "size",
                    "80",
                    "title",
                    "pattern for the download URL for the newly discovered " +
                    "package. Use ${match} for the matched string as-is, " +
                    "${version} for the whole version " +
                    "number (please note that it will be normalized). " +
                    "Use ${v0}, ${v1}, etc. to access one " +
                    "number from the version.");
            w.start("datalist", "id", "discovery-url");
            w.e("option", "value",
                    "http://www.example.com/downloads/example-${version}.zip");
            /*
             w.e("option", "value", "${{version2Parts}}");
             w.e("option", "value", "${{version3Parts}}");
             w.e("option", "value", "${{version2PartsWithoutDots}}");
             w.e("option", "value", "${{actualVersion}}");
             w.e("option", "value", "${{actualVersionWithoutDots}}");
             w.e("option", "value", "${{actualVersionWithUnderscores}}");
             w.e("option", "value", "${{match}}");
             */
            w.end("datalist");
            w.end("input");
            endRow(w);
        }

        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            startRow(w, "Permissions", false);
            User u = UserServiceFactory.getUserService().getCurrentUser();
            if (mode != FormMode.CREATE) {
                if (NWUtils.isAdminLoggedIn()) {
                    w.e("textarea",
                            "class",
                            "form-control nw-autosize",
                            "rows",
                            "4",
                            "name",
                            "permissions",
                            "cols",
                            "80",
                            "title",
                            "list of email addresses for people that are allowed to change this package and its versions",
                            permissions);
                } else {
                    w.start("p", "class", "form-control-static");
                    List<String> pm = NWUtils.split(permissions, '\n');
                    for (int i = 0; i < pm.size(); i++) {
                        if (i != 0) {
                            w.unencoded("<br>");
                        }
                        if (NWUtils.isEmailEqual(u.getEmail(), pm.get(i))) {
                            w.t(pm.get(i));
                        } else {
                            w.unencoded(NWUtils.obfuscateEmail(pm.
                                    get(i), request.
                                    getServerName()));
                        }
                    }
                    w.end("p");
                }
            } else {
                w.start("p", "class", "form-control-static");
                w.t(u.getEmail());
                w.end("p");
            }
            endRow(w);
        }

        startRow(w, "Last modified", false);
        w.start("p", "class", "form-control-static");
        w.t(params.get("modified"));
        w.end("p");
        endRow(w);

        startRow(w, "Last modified by", false);
        w.start("p", "class", "form-control-static");
        w.unencoded(NWUtils.obfuscateEmail(params.get("modifiedBy"),
                request.getServerName()));
        w.end("p");
        endRow(w);

        startRow(w, "Created", false);
        w.start("p", "class", "form-control-static");
        w.t(createdAt == null ? "" : createdAt.toString());
        w.end("p");
        endRow(w);

        startRow(w, "Created by", false);
        w.start("p", "class", "form-control-static");
        w.unencoded(createdBy != null ? NWUtils.obfuscateEmail(createdBy.
                getEmail(), request.getServerName()) : "");
        w.end("p");
        endRow(w);

        w.end("form");

        return w.toString();
    }

    private boolean isDetectionPossible() {
        String msg = null;
        if (msg == null) {
            if (!this.discoveryURL.trim().isEmpty()) {
                msg = NWUtils.validateURL(this.discoveryURL);
            } else {
                msg = "No discovery URL defined";
            }
        }

        if (msg == null) {
            if (!this.discoveryRE.trim().isEmpty()) {
                try {
                    Pattern.compile(this.discoveryRE);
                } catch (PatternSyntaxException e) {
                    msg =
                            "Cannot parse the regular expression: " +
                            e.getMessage();
                }
            } else {
                msg = "No discovery regular expression defined";
            }
        }
        return msg == null;
    }

    @Override
    public String getTitle() {
        return title.isEmpty() ? "Package" : title;
    }

    @Override
    public String getTitleHTML() {
        HTMLWriter w = new HTMLWriter();
        if (icon.isEmpty()) {
            w.e("img", "src", "/App.png");
        } else {
            w.e("img", "src", icon, "style",
                    "width: 32px; max-height: 32px");
        }
        if (mode != FormMode.CREATE) {
            w.t(" " + title);

            PackagesPage.createTags(w, noUpdatesCheck, this.tags.indexOf(
                    "end-of-life") >= 0);

            w.t(" ");
            NWUtils.star(w, this.id, starFilled, starred);
        } else {
            w.t(" New package");
        }

        return w.toString();
    }

    /**
     * @return versions of this package
     */
    public List<PackageVersion> getVersions() {
        ArrayList<PackageVersion> versions = new ArrayList<>();
        if (!id.isEmpty()) {
            for (PackageVersion pv : NWUtils.dsCache.getPackageVersions(id)) {
                versions.add(pv);
            }

        }
        return versions;
    }

    /**
     * @return list of all licenses
     */
    private List<License> getLicenses() {
        return NWUtils.dsCache.getAllLicenses();
    }

    /**
     * Fills the values from an HTTP request.
     *
     * @param req HTTP request
     */
    @Override
    public void fill(HttpServletRequest req) {
        super.fill(req);
        if ("true".equals(req.getParameter("new"))) {
            this.mode = FormMode.CREATE;
        } else {
            this.mode = FormMode.EDIT;
        }
        id = req.getParameter("name");
        title = req.getParameter("title");
        url = req.getParameter("url");
        changelog = req.getParameter("changelog");
        icon = req.getParameter("icon");
        comment = req.getParameter("comment");
        discoveryURL = req.getParameter("discoveryPage");
        discoveryRE = req.getParameter("discoveryRE");
        discoveryURLPattern = req.getParameter("discoveryURLPattern");
        license = req.getParameter("license");
        category = req.getParameter("category");
        tags = NWUtils.split(req.getParameter("tags"), ',');
        screenshots = getTrimmedParam("screenshots");
        permissions = getTrimmedParam("permissions");

        if (this.mode == FormMode.CREATE) {
            this.createdAt = NWUtils.newDate();
            this.createdBy =
                    UserServiceFactory.getUserService().getCurrentUser();
            this.noUpdatesCheck = null;
        } else {
            Package p = NWUtils.dsCache.getPackage(this.id, false);
            this.createdAt = p.createdAt;
            this.createdBy = p.createdBy;
            this.noUpdatesCheck = p.noUpdatesCheck;

            this.starFilled = false;
            User u = UserServiceFactory.getUserService().getCurrentUser();
            if (u != null) {
                Editor e = NWUtils.dsCache.findEditor(u);
                if (e != null && e.starredPackages.contains(id)) {
                    starFilled = true;
                }
            }

            this.starred = p.starred;
        }
    }

    /**
     * Transfers the data from this form into the specified object.
     *
     * @param p the data will be stored here
     */
    public void fillObject(Package p) {
        p.description = params.get("description");
        if (p.description == null) {
            p.description = "";
        } else {
            p.description = p.description.trim();
        }
        p.icon = icon.trim();
        p.title = title.trim();
        p.url = url.trim();
        p.changelog = changelog.trim();
        p.license = license.trim();
        p.comment = comment.trim();
        p.discoveryPage = discoveryURL.trim();
        p.discoveryRE = discoveryRE.trim();
        p.discoveryURLPattern = discoveryURLPattern.trim();
        p.category = category.trim();
        p.tags = new ArrayList<>();
        p.tags.addAll(this.tags);

        if (NWUtils.isAdminLoggedIn()) {
            p.permissions.clear();
            if (permissions != null) {
                List<String> ps = NWUtils.splitLines(permissions);
                for (String permission : ps) {
                    permission = permission.trim();
                    if (!permission.isEmpty()) {
                        p.permissions.add(NWUtils.email2user(permission));
                    }
                }
            }
        }

        List<String> lines = NWUtils.splitLines(screenshots);
        p.screenshots.clear();
        for (String s : lines) {
            if (!s.trim().isEmpty()) {
                p.screenshots.add(s);
            }
        }

        p.issues = params.get("issues");
    }

    @Override
    public String validate() {
        String msg = Package.checkName(this.id);

        if (msg == null) {
            if (mode == FormMode.CREATE) {
                Package r = NWUtils.dsCache.getPackage(this.id, false);
                if (r != null) {
                    msg = "A package with this ID already exists";
                }
            }
        }

        if (msg == null) {
            if (title.trim().isEmpty()) {
                msg = "Title cannot be empty";
            }
        }
        if (msg == null) {
            msg = NWUtils.validateURL(this.url);
            if (msg != null) {
                msg = "Error in product home page: " + msg;
            }
        }
        if (msg == null) {
            if (!this.changelog.trim().isEmpty()) {
                msg = NWUtils.validateURL(this.changelog);
                if (msg != null) {
                    msg = "Error in change log: " + msg;
                }
            }
        }
        if (msg == null) {
            if (!this.icon.trim().isEmpty()) {
                msg = NWUtils.validateURL(this.icon);
                if (msg != null) {
                    msg = "Error in icon: " + msg;
                }
            }
        }
        if (msg == null) {
            List<String> lines = NWUtils.splitLines(this.screenshots);
            for (String s : lines) {
                if (!s.trim().isEmpty()) {
                    msg = NWUtils.validateURL(s);
                    if (msg != null) {
                        msg = "Error in screenshots: " + msg;
                        break;
                    }
                }
            }
        }
        if (msg == null) {
            if (params.get("issues") != null && !params.get("issues").trim().
                    isEmpty()) {
                msg = NWUtils.validateURL(params.get("issues"));
                if (msg != null) {
                    msg = "Error in issue tracker: " + msg;
                }
            }
        }
        if (msg == null) {
            String description = params.get("description");
            if (description == null) {
                description = "";
            } else {
                description = description.trim();
            }

            if (description.isEmpty()) {
                msg = "Empty description";
            } else {
                Markdown4jProcessor mp = new Markdown4jProcessor();
                try {
                    mp.process(description);
                } catch (IOException e) {
                    msg = " Failed to parse the Markdown syntax: " + e.
                            getMessage();
                }
            }
        }

        if (msg == null) {
            if (!this.discoveryURL.trim().isEmpty()) {
                msg = NWUtils.validateURL(this.discoveryURL);
                if (msg != null) {
                    msg = "Error in discovery page (URL): " + msg;
                }
            }
        }

        if (msg == null) {
            if (!this.discoveryRE.trim().isEmpty()) {
                try {
                    Pattern.compile(this.discoveryRE);
                } catch (PatternSyntaxException e) {
                    msg = "Cannot parse the regular expression: " +
                            e.getMessage();
                }
            }
        }

        if (msg == null) {
            if (NWUtils.isAdminLoggedIn()) {
                if (permissions == null || permissions.trim().length() == 0) {
                    if (mode != FormMode.CREATE) {
                        msg = "The list of permissions cannot be empty";
                    }
                } else {
                    List<String> ps = NWUtils.splitLines(permissions.trim());
                    for (String s : ps) {
                        s = s.trim();
                        if (s.length() != 0) {
                            msg = NWUtils.validateEmail(s);
                            if (msg != null) {
                                msg = "Error in permissions: " + msg;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return msg;
    }

    public void fill(Package r) {
        this.id = r.name;
        params.put("description", r.description.trim());
        icon = r.icon.trim();
        title = r.title.trim();
        url = r.url.trim();
        changelog = r.changelog == null ? "" : r.changelog;
        license = r.license.trim();
        params.put("issues", r.issues);
        comment = r.comment.trim();
        discoveryURL = r.discoveryPage.trim();
        discoveryRE = r.discoveryRE.trim();
        discoveryURLPattern = r.discoveryURLPattern.trim();
        createdAt = r.createdAt;
        createdBy = r.createdBy;
        category = r.category;
        this.tags = new ArrayList<>();
        this.tags.addAll(r.tags);
        screenshots = NWUtils.join("\n", r.screenshots);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < r.permissions.size(); i++) {
            if (i != 0) {
                sb.append("\n");
            }
            sb.append(r.permissions.get(i).getEmail());
        }
        this.permissions = sb.toString();
        this.noUpdatesCheck = r.noUpdatesCheck;

        params.put("modified", r.lastModifiedAt.toString());
        params.put("modifiedBy", r.lastModifiedBy.getEmail());

        this.starFilled = false;
        User u = UserServiceFactory.getUserService().getCurrentUser();
        if (u != null) {
            Editor e = NWUtils.dsCache.findEditor(u);
            if (e != null && e.starredPackages.contains(id)) {
                starFilled = true;
            }
        }

        this.starred = r.starred;
    }

    private void startRow(HTMLWriter w, String title, boolean optional) {
        w.start("div", "class", "form-group");
        w.start("label", "class", "col-sm-2 control-label");
        w.t(title);
        if (optional) {
            w.e("small", " (optional)");
        }
        w.t(":");
        w.end("label");
        w.start("div", "class", "col-sm-10");
    }

    private void endRow(HTMLWriter w) {
        w.end("div");
        w.end("div");
    }
}
