package com.googlecode.npackdweb.package_;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.FormMode;
import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.QueryCache;
import com.googlecode.npackdweb.Version;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.http.HttpServletRequest;
import org.markdown4j.Markdown4jProcessor;

/**
 * A package.
 */
public class PackageDetailPage extends MyPage {

    /**
     * error message or null
     */
    public String error;

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
     * package description
     */
    public String description;

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
     * @param mode editing mode
     */
    public PackageDetailPage(FormMode mode) {
        this.mode = mode;

        id = "";
        title = "";
        url = "";
        changelog = "";
        icon = "";
        description = "";
        comment = "";
        discoveryURL = "";
        discoveryRE = "";
        discoveryURLPattern = "";
        license = "";
        this.tags = new ArrayList<String>();
        screenshots = "";
        permissions = "";
    }

    /*
     * @Override public String getHeadPart() { return
     * "<script type=\"text/javascript\" language=\"javascript\" src=\"/com.googlecode.npackdweb.Editor/com.googlecode.npackdweb.Editor.nocache.js\"></script>\n"
     * ; }
     */
    @Override
    public String createBodyBottom(HttpServletRequest request)
            throws IOException {
        HTMLWriter w = new HTMLWriter();
        w.start("script");
        w.unencoded(NWUtils.tmpl("PackageDetail.js"));
        w.end("script");

        w.unencoded(NWUtils.tmpl("GooglePlus.html"));

        return w.toString();
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        boolean editable = mode == FormMode.EDIT || mode == FormMode.CREATE;

        HTMLWriter w = new HTMLWriter();
        if (error != null) {
            w.e("p", "class", "bg-danger", this.error);
        }

        if (mode == FormMode.CREATE || mode == FormMode.EDIT) {
            w.start("form", "class", "form-horizontal", "method", "post",
                    "action", "/package/save", "id", "package-form");
        }

        if (mode == FormMode.EDIT) {
            w.e("input", "type", "hidden", "id", "name", "name", "name",
                    "value", id);
        }

        if (mode == FormMode.CREATE) {
            w.e("input", "type", "hidden", "name", "new", "value", "true");
        }

        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.start("div", "class", "btn-group");
            w.e("input", "class", "btn btn-default", "type", "submit", "value",
                    "Save");
            if (mode != FormMode.CREATE) {
                NWUtils.jsButton(w, "Edit as XML", "/rep/edit-as-xml?package=" +
                        id, "Edit this package as repository XML");
                NWUtils.jsButton_(w, "Delete", "deleteOnClick()",
                        "Deletes this package and all associated versions");
                NWUtils.jsButton(w, "New version", "/p/" + id + "/new",
                        "Creates new version");
                NWUtils.jsButton(
                        w,
                        "Detect new version",
                        "/p/" + id + "/detect",
                        "Uses the discovery page (URL) and discovery regular expression to identify a newer version of the package",
                        this.isDetectionPossible());
            }
            w.end("div");
        }

        w.start("table", "border", "0");

        if (mode != FormMode.VIEW) {
            w.start("tr");
            w.e("td", "Title:");
            w.start("td");
            w.e("input", "class", "form-control", "type", "text", "name",
                    "title", "value", title, "size", "80", "title",
                    "Name of the package", "id", "title");
            w.end("td");
            w.end("tr");
        }

        w.start("tr");
        w.e("td", "ID:");
        if (mode != FormMode.CREATE) {
            w.e("td", id);
        } else {
            w.start("td");
            w.e("input", "class", "form-control", "type", "text", "name",
                    "name", "value", id, "size", "80", "id", "id", "title",
                    "Package name");
            w.start("p", "class", "nw-help");
            w.t(" See ");
            w.e("a",
                    "href",
                    "https://github.com/tim-lebedkov/npackd/wiki/RepositoryFormat#package-naming-rules",
                    "target", "_blank", "Package naming rules");
            w.t(" for more details");
            w.end("p");
            w.end("td");
        }
        w.end("tr");

        Objectify ofy = DefaultServlet.getObjectify();

        w.start("tr");
        w.e("td", "Versions:");
        w.start("td");
        List<PackageVersion> pvs = this.getVersions(ofy);
        Collections.sort(pvs, new Comparator<PackageVersion>() {
            @Override
            public int compare(PackageVersion a, PackageVersion b) {
                Version va = Version.parse(a.version);
                Version vb = Version.parse(b.version);
                return va.compare(vb);
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
        w.end("td");
        w.end("tr");

        w.start("tr");
        w.start("td");
        w.t("Product home page");
        if (editable) {
            w.e("small", " (optional)");
        }
        w.t(":");
        w.end("td");
        w.start("td");
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.e("input", "id", "url", "style", "display: inline; width: 90%",
                    "class", "form-control", "type", "text", "name", "url",
                    "value", url, "size", "120", "title",
                    "http: or https: address of the product home page");
            w.e("div", "class", "glyphicon glyphicon-link", "id", "url-link",
                    "style",
                    "cursor: pointer; font-size: 20px; font-weight: bold");
            /*
             * w.start("a", "id", "url-link", "href", "#", "target", "_blank");
             * w.e("img", "src", "/Link.png"); w.end("a");
             */
        } else {
            w.e("a", "href", url, url);
        }
        w.end("td");
        w.end("tr");

        w.start("tr");
        w.start("td");
        w.t("Change log");
        if (editable) {
            w.e("small", " (optional)");
        }
        w.t(":");
        w.end("td");
        w.start("td");
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.e("input", "id", "changelog", "style",
                    "display: inline; width: 90%", "class", "form-control",
                    "type", "text", "name", "changelog", "value", changelog,
                    "size", "120", "title",
                    "http: or https: address of the package change log");
            w.e("div", "class", "glyphicon glyphicon-link", "id",
                    "changelog-link", "style",
                    "cursor: pointer; font-size: 20px; font-weight: bold");
        } else {
            w.e("a", "href", changelog, changelog);
        }
        w.end("td");
        w.end("tr");

        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.start("tr");
            w.start("td");
            w.t("Icon");
            w.e("small", " (optional)");
            w.t(":");
            w.end("td");
            w.start("td");
            w.e("input", "class", "form-control", "type", "text", "name",
                    "icon", "value", icon, "size", "120", "title",
                    "http: or https: address of a 32x32 PNG icon representing this package");
            w.end("td");
            w.end("tr");
        }

        // screen shots
        w.start("tr");
        w.start("td");
        w.t("Screen shots");
        if (editable) {
            w.e("small", " (optional)");
        }
        w.t(":");
        w.end("td");
        w.start("td");
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
            w.e("textarea", "class", "form-control", "rows", "4", "name",
                    "screenshots", "cols", "80", "title",
                    "List of screen shot URLs. " +
                    "Each URL must be on a separate line. " +
                    "Only https: and http: protocols are allowed. " +
                    "Only PNG images are allowed.", screenshots);
        }
        w.end("td");
        w.end("tr");

        // description
        w.start("tr");
        w.start("td");
        w.t("Description");
        if (editable) {
            w.e("small", " (optional)");
        }
        w.t(":");
        w.end("td");
        w.start("td");
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.start("p", "class", "nw-help");
            w.e("a", "href",
                    "http://daringfireball.net/projects/markdown/syntax",
                    "target", "_blank", "Markdown syntax");
            w.t(" can be used in the following text area");
            w.end("p");
            w.e("textarea", "class", "form-control", "rows", "10", "name",
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
        w.end("td");
        w.end("tr");

        // license
        w.start("tr");
        w.start("td");
        w.t("License");
        if (editable) {
            w.e("small", " (optional)");
        }
        w.t(":");
        w.end("td");
        w.start("td");
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.start("select", "class", "form-control", "name", "license",
                    "title", "Package licensing terms");
            w.e("option", "value", "");
            for (License lic : this.getLicenses(ofy)) {
                w.e("option", "value", lic.name, "selected",
                        lic.name.equals(license) ? "selected" : null, lic.title);
            }
            w.end("select");
        } else {
            License license_ = null;
            if (!license.isEmpty()) {
                license_ = ofy.find(License.class, license);
            }

            if (license_ == null) {
                w.t("unknown");
            } else {
                w.e("a", "href", license_.url, license_.title);
            }
        }
        w.end("td");
        w.end("tr");

        // categories/tags
        w.start("tr");
        w.start("td");
        w.t("Categories/tags");
        if (editable) {
            w.e("small", " (optional)");
        }
        w.t(":");
        w.end("td");
        w.start("td");
        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            // PackageDetail.js should also be updated if the list of
            // default categories/tags changes
            for (String s : new String[]{"Communications", "Development",
                "Education", "Finance", "Games", "Music", "News", "Photo",
                "Productivity", "Security", "Text", "Tools", "Video"}) {
                w.start("label", "class", "checkbox-inline");
                w.e("input", "type", "checkbox", "id", "tag-" + s, "value", s);
                w.t(" " + s);
                w.end("label");
            }
            w.start("input", "class", "form-control", "type", "text", "name",
                    "tags", "id", "tags", "autocomplete", "off", "value",
                    NWUtils.join(", ", tags), "size", "80", "title",
                    "Comma separated list of tags/categories associated with " +
                    "this package version. " +
                    "Sub-categories can be defined using slashes as in Video/Encoders. " +
                    "Please note that only the first category and sub-category will be used in Npackd."
            );
            w.end("input");
        } else {
            w.t(NWUtils.join(", ", tags));
        }
        w.end("td");
        w.end("tr");

        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.start("tr");
            w.start("td");
            w.t("Comment");
            w.e("small", " (optional)");
            w.t(":");
            w.end("td");
            w.start("td");
            w.e("textarea",
                    "class",
                    "form-control",
                    "rows",
                    "5",
                    "name",
                    "comment",
                    "cols",
                    "80",
                    "title",
                    "Internal comments normally only visible to the package editors",
                    comment);
            w.end("td");
            w.end("tr");
        }

        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.start("tr");
            w.start("td");
            w.t("Discovery page (URL)");
            w.e("small", " (optional)");
            w.t(":");
            w.end("td");
            w.start("td");
            w.e("input",
                    "class",
                    "form-control",
                    "type",
                    "text",
                    "name",
                    "discoveryPage",
                    "value",
                    discoveryURL,
                    "size",
                    "120",
                    "title",
                    "http: or https: URL of an HTML or text page that contains the newest version number as text");
            w.end("td");
            w.end("tr");

            w.start("tr");
            w.start("td");
            w.t("Discovery regular expression");
            w.e("small", " (optional)");
            w.t(":");
            w.end("td");
            w.start("td");
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
                    "Regular expression to match the newest version number. This regular expression should contain a match group for the version number. A single letter at the end of the version number is allowed (2.0.6b will be interpreted as 2.0.6.2). Minus sign will be interpreted as a dot.\n" +

                    "This regular expression will be applied to all lines in the file one-by-one until a match is found.\n" +
                    "Example: <h1>the newest version is ([\\d\\.]+)</h1>");
            w.start("datalist", "id", "discovery-res");
            w.e("option", "value", "The current version is ([\\d\\.]+)");
            w.e("option", "value", ">v([\\d\\.]+)<");
            w.end("datalist");
            w.end("input");
            w.end("td");
            w.end("tr");

            w.start("tr");
            w.start("td");
            w.t("Discovery package download URL pattern");
            w.e("small", " (optional)");
            w.t(":");
            w.end("td");
            w.start("td");
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
                    "pattern for the download URL for the newly discovered package");
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
            w.end("td");
            w.end("tr");
        }

        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.start("tr");
            w.e("td", "Permissions:");
            w.start("td");
            User u = UserServiceFactory.getUserService().getCurrentUser();
            if (mode != FormMode.CREATE) {
                Package p = Package.findByName(ofy, this.id);
                if (NWUtils.isAdminLoggedIn()) {
                    w.e("textarea",
                            "class",
                            "form-control",
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
                    for (int i = 0; i < p.permissions.size(); i++) {
                        if (i != 0) {
                            w.unencoded("<br>");
                        }
                        if (NWUtils.isEqual(u, p.permissions.get(i))) {
                            w.t(u.getEmail());
                        } else {
                            w.unencoded(NWUtils.obfuscateEmail(ofy,
                                    p.permissions.get(i).getEmail()));
                        }
                    }
                }
            } else {
                w.t(u.getEmail());
            }
            w.end("td");
            w.end("tr");
        }

        w.start("tr");
        w.e("td", "Created:");
        w.start("td");
        w.t(createdAt == null ? "" : createdAt.toString());
        w.end("td");
        w.end("tr");

        w.start("tr");
        w.e("td", "Created by:");
        w.start("td");
        w.unencoded(createdBy != null ? NWUtils.obfuscateEmail(ofy,
                createdBy.getEmail()) : "");
        w.end("td");
        w.end("tr");

        w.end("table");

        if (mode == FormMode.EDIT || mode == FormMode.CREATE) {
            w.end("form");
        }

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
                    "max-width: 32px; max-height: 32px");
        }
        if (mode != FormMode.CREATE) {
            w.t(" " + title);

            w.unencoded(
                    " <div class='g-plusone' data-size='medium' data-annotation='inline' data-width='300' data-href='" +
                    "https://npackd.appspot.com/p/" + id + "'></div>");
        } else {
            w.t(" New package");
        }
        return w.toString();
    }

    /**
     * @param ofy Objectify
     * @return versions of this package
     */
    public List<PackageVersion> getVersions(Objectify ofy) {
        ArrayList<PackageVersion> versions = new ArrayList<PackageVersion>();
        if (!id.isEmpty()) {
            for (PackageVersion pv : ofy.query(PackageVersion.class)
                    .filter("package_ =", id).fetch()) {
                versions.add(pv);
            }

        }
        return versions;
    }

    /**
     * @param ofy Objectify
     * @return list of all licenses
     */
    private List<License> getLicenses(Objectify ofy) {
        List<License> licenses = new ArrayList<License>();
        String cacheSuffix = "@" + NWUtils.getDataVersion();
        Query<License> q = ofy.query(License.class).order("title");
        List<Key<License>> keys = QueryCache.getKeys(ofy, q, cacheSuffix);
        Map<Key<License>, License> k2v = ofy.get(keys);
        licenses.addAll(k2v.values());
        return licenses;
    }

    /**
     * Fills the values from an HTTP request.
     *
     * @param req HTTP request
     */
    @Override
    public void fill(HttpServletRequest req) {
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
        description = req.getParameter("description");
        comment = req.getParameter("comment");
        discoveryURL = req.getParameter("discoveryPage");
        discoveryRE = req.getParameter("discoveryRE");
        discoveryURLPattern = req.getParameter("discoveryURLPattern");
        license = req.getParameter("license");
        tags = NWUtils.split(req.getParameter("tags"), ',');
        screenshots = req.getParameter("screenshots");
        permissions = req.getParameter("permissions");

        if (this.mode == FormMode.CREATE) {
            this.createdAt = new Date();
            this.createdBy =
                    UserServiceFactory.getUserService().getCurrentUser();
        } else {
            Objectify ofy = DefaultServlet.getObjectify();
            Package p = Package.findByName(ofy, this.id);
            this.createdAt = p.createdAt;
            this.createdBy = p.createdBy;
        }
    }

    /**
     * Transfers the data from this form into the specified object.
     *
     * @param p the data will be stored here
     */
    public void fillObject(Package p) {
        p.description = description.trim();
        p.icon = icon.trim();
        p.title = title.trim();
        p.url = url.trim();
        p.changelog = changelog.trim();
        p.license = license.trim();
        p.comment = comment.trim();
        p.discoveryPage = discoveryURL.trim();
        p.discoveryRE = discoveryRE.trim();
        p.discoveryURLPattern = discoveryURLPattern.trim();
        p.tags = new ArrayList<String>();
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
    }

    @Override
    public String validate() {
        String msg = null;
        msg = Package.checkName(this.id);

        if (msg == null) {
            if (mode == FormMode.CREATE) {
                Objectify ofy = DefaultServlet.getObjectify();
                Package r = ofy.find(new Key<Package>(Package.class, this.id));
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
            if (!this.url.trim().isEmpty()) {
                msg = NWUtils.validateURL(this.url);
            }
        }
        if (msg == null) {
            if (!this.changelog.trim().isEmpty()) {
                msg = NWUtils.validateURL(this.changelog);
            }
        }
        if (msg == null) {
            if (!this.icon.trim().isEmpty()) {
                msg = NWUtils.validateURL(this.icon);
            }
        }
        if (msg == null) {
            List<String> lines = NWUtils.splitLines(this.screenshots);
            for (String s : lines) {
                if (!s.trim().isEmpty()) {
                    msg = NWUtils.validateURL(s);
                    if (msg != null) {
                        break;
                    }
                }
            }
        }
        if (msg == null) {
            Markdown4jProcessor mp = new Markdown4jProcessor();
            try {
                mp.process(description);
            } catch (IOException e) {
                msg = " Failed to parse the Markdown syntax: " + e.getMessage();
            }
        }

        if (msg == null) {
            if (!this.discoveryURL.trim().isEmpty()) {
                msg = NWUtils.validateURL(this.discoveryURL);
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
        description = r.description.trim();
        icon = r.icon.trim();
        title = r.title.trim();
        url = r.url.trim();
        changelog = r.changelog == null ? "" : r.changelog;
        license = r.license.trim();
        comment = r.comment.trim();
        discoveryURL = r.discoveryPage.trim();
        discoveryRE = r.discoveryRE.trim();
        discoveryURLPattern = r.discoveryURLPattern.trim();
        createdAt = r.createdAt;
        createdBy = r.createdBy;
        this.tags = new ArrayList<String>();
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
    }
}
