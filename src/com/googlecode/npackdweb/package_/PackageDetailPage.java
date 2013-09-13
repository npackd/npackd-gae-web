package com.googlecode.npackdweb.package_;

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

/**
 * A package.
 */
public class PackageDetailPage extends MyPage {
    /** error message or null */
    public String error;

    /** full package id */
    public String id;

    /** package title */
    public String title;

    /** package home page */
    public String url;

    /** package icon */
    public String icon;

    /** package description */
    public String description;

    /** package comment */
    public String comment;

    /** discovery URL */
    public String discoveryURL;

    /** discovery regular expression */
    public String discoveryRE;

    /** pattern for the download URL */
    public String discoveryURLPattern;

    /** mode for this form */
    public FormMode mode;

    /** date and time of the package creation or null for new packages */
    public Date createdAt;

    /** user that created the package or null */
    public User createdBy;

    /** license ID */
    public String license;

    /** list of tags separated by commas */
    public String tags;

    /**
     * @param p
     *            a package or null
     * @param editable
     *            true if the data should be editable
     */
    public PackageDetailPage(FormMode mode) {
        this.mode = mode;

        id = "";
        title = "";
        url = "";
        icon = "";
        description = "";
        comment = "";
        discoveryURL = "";
        discoveryRE = "";
        discoveryURLPattern = "";
        license = "";
        tags = "";
    }

    @Override
    public String getHeadPart() {
        return "<script type=\"text/javascript\" language=\"javascript\" src=\"/com.googlecode.npackdweb.Editor/com.googlecode.npackdweb.Editor.nocache.js\"></script>\n";
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter w = new HTMLWriter();
        if (error != null) {
            w.e("p", "class", "nw-error", this.error);
        }
        w.start("h3");
        if (icon.isEmpty()) {
            w.e("img", "src", "/App.png");
        } else {
            w.e("img", "src", icon, "style",
                    "max-width: 32px; max-height: 32px");
        }
        if (mode != FormMode.CREATE)
            w.t(" " + title);
        else
            w.t(" New package");
        w.end("h3");

        if (mode == FormMode.CREATE || mode == FormMode.EDIT) {
            w.start("form", "method", "post", "action", "/package/save", "id",
                    "package-form");
        }

        if (mode == FormMode.EDIT)
            w.e("input", "type", "hidden", "name", "name", "value", id);

        if (mode == FormMode.CREATE)
            w.e("input", "type", "hidden", "name", "new", "value", "true");

        w.start("table", "border", "0");
        w.start("tr");
        w.e("td", "ID:");
        if (mode != FormMode.CREATE)
            w.e("td", id);
        else {
            w.start("td");
            w.e("input", "type", "text", "name", "name", "value", id, "size",
                    "80", "title",
                    "Full package name including the reversed domain name");
            w.start("p", "class", "nw-help");
            w.t(" See ");
            w.e("a",
                    "href",
                    "http://code.google.com/p/windows-package-manager/wiki/RepositoryFormat#Package_naming_rules",
                    "target", "_blank", "Package naming rules");
            w.t(" for more details");
            w.end("p");
            w.end("td");
        }
        w.end("tr");

        if (mode != FormMode.VIEW) {
            w.start("tr");
            w.e("td", "Title:");
            w.start("td");
            w.e("input", "type", "text", "name", "title", "value", title,
                    "size", "80", "title", "Name of the package");
            w.end("td");
            w.end("tr");
        }

        w.start("tr");
        w.e("td", "Product home page:");
        w.start("td");
        if (mode.isEditable()) {
            w.e("input", "type", "text", "name", "url", "value", url, "size",
                    "120", "title",
                    "http: or https: address of the product home page");
            w.start("a", "href", url, "target", "_blank");
            w.e("img", "src", "/Link.png");
            w.end("a");
        } else {
            w.e("a", "href", url, url);
        }
        w.end("td");
        w.end("tr");

        if (mode.isEditable()) {
            w.start("tr");
            w.e("td", "Icon:");
            w.start("td");
            w.e("input", "type", "text", "name", "icon", "value", icon, "size",
                    "120", "title",
                    "http: or https: address of a 32x32 PNG icon representing this package");
            w.end("td");
            w.end("tr");
        }

        w.start("tr");
        w.e("td", "Description:");
        w.start("td");
        if (mode.isEditable()) {
            w.start("p", "class", "nw-help");
            w.e("a", "href",
                    "http://daringfireball.net/projects/markdown/syntax",
                    "target", "_blank", "Markdown syntax");
            w.t(" can be used in the following text area");
            w.end("p");
            w.e("textarea", "rows", "10", "name", "description", "cols", "80",
                    "title", "Possibly long description of the package. "
                            + "Try to not repeat the package name here and "
                            + "keep it simple and informative.", description);
        } else {
            Markdown4jProcessor mp = new Markdown4jProcessor();
            try {
                w.unencoded(mp.process(description));
            } catch (IOException e) {
                w.t(description + " Failed to parse the Markdown syntax: "
                        + e.getMessage());
            }
        }
        w.end("td");
        w.end("tr");

        Objectify ofy = DefaultServlet.getObjectify();

        w.start("tr");
        w.e("td", "License:");
        w.start("td");
        if (mode.isEditable()) {
            w.start("select", "name", "license", "title",
                    "Package licensing terms");
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

            if (license_ == null)
                w.t("unknown");
            else
                w.e("a", "href", license_.url, license_.title);
        }
        w.end("td");
        w.end("tr");

        w.start("tr");
        w.e("td", "Tags:");
        w.start("td");
        if (mode.isEditable()) {
            w.e("input", "type", "text", "name", "tags", "value", tags, "size",
                    "40", "id", "tags", "autocomplete", "off", "title",
                    "list of tags associated with this package separated by commas");
        } else {
            w.t(tags);
        }
        w.end("td");
        w.end("tr");

        if (mode.isEditable()) {
            w.start("tr");
            w.e("td", "Comment:");
            w.start("td");
            w.e("textarea",
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

        if (mode.isEditable()) {
            w.start("tr");
            w.e("td", "Permissions:");
            w.start("td");
            Package p = Package.findByName(ofy, this.id);
            for (int i = 0; i < p.permissions.size(); i++) {
                if (i != 0)
                    w.e("br");
                w.t(p.permissions.get(i).getEmail());
            }
            w.end("td");
            w.end("tr");
        }

        w.start("tr");
        w.e("td", "Versions:");
        w.start("td");
        List<PackageVersion> pvs = this.getVersions(ofy);
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
            w.e("a", "href", "/p/" + pv.package_ + "/" + pv.version, pv.version);
        }
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
        w.unencoded(createdBy != null ? NWUtils.obfuscateEmail(ofy,
                createdBy.getEmail()) : "");
        w.end("td");
        w.end("tr");

        if (mode.isEditable()) {
            w.start("tr");
            w.e("td", "Discovery page (URL):");
            w.start("td");
            w.e("input",
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
            w.start("td", "colspan", "2");
            w.e("p", "class", "nw-help", "");
            w.end("td");
            w.end("tr");

            w.start("tr");
            w.e("td", "Discovery regular expression:");
            w.start("td");
            w.e("input",
                    "type",
                    "text",
                    "name",
                    "discoveryRE",
                    "value",
                    discoveryRE,
                    "size",
                    "80",
                    "title",
                    "Regular expression to match the newest version number. This regular expression should contain a match group for the version number.\nExample: <h1>the newest version is ([\\d\\.]+)</h1>");
            w.end("td");
            w.end("tr");
            /*
             * w.start("tr"); w.start("td", "colspan", "2"); w.e("p", "class",
             * "nw-help",
             * "The following field will help to define the download URL for a newly detected version in the future and is currently unused"
             * ); w.end("td"); w.end("tr");
             * 
             * w.start("tr"); w.e("td", "Discovery URL pattern:");
             * w.start("td"); w.e("input", "type", "text", "name",
             * "discoveryURLPattern", "value", discoveryURLPattern, "size",
             * "120", "title",
             * "Special URL pattern where ${{actualVersion}} and similar variables will be used to identify the binary of the new package version"
             * ); w.end("td"); w.end("tr");
             */
        }

        w.end("table");

        if (mode.isEditable()) {
            w.e("input", "class", "input", "type", "submit", "value", "Save");
            if (mode != FormMode.CREATE) {
                NWUtils.jsButton(w, "Edit as XML", "/rep/edit-as-xml?package="
                        + id, "Edit this package as repository XML");
                NWUtils.jsButton(w, "Delete", "/package/delete?id=" + id,
                        "Deletes this package and all associated versions");
                NWUtils.jsButton(w, "New version", "/p/" + id + "/new",
                        "Creates new version");
                NWUtils.jsButton(
                        w,
                        "Detect new version",
                        "/p/" + id + "/detect",
                        "Uses the discovery page (URL) and discovery regular expression to identify a newer version of the package");
            }
            w.end("form");
        }

        return w.toString();
    }

    @Override
    public String getTitle() {
        return title.isEmpty() ? "Package" : title;
    }

    /**
     * @param ofy
     *            Objectify
     * @return versions of this package
     */
    public List<PackageVersion> getVersions(Objectify ofy) {
        ArrayList<PackageVersion> versions = new ArrayList<PackageVersion>();
        if (!id.isEmpty()) {
            for (PackageVersion pv : ofy.query(PackageVersion.class)
                    .filter("package_ =", id).fetch())
                versions.add(pv);

        }
        return versions;
    }

    /**
     * @param ofy
     *            Objectify
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
     * @param req
     *            HTTP request
     */
    public void fill(HttpServletRequest req) {
        if ("true".equals(req.getParameter("new")))
            this.mode = FormMode.CREATE;
        else
            this.mode = FormMode.EDIT;
        id = req.getParameter("name");
        title = req.getParameter("title");
        url = req.getParameter("url");
        icon = req.getParameter("icon");
        description = req.getParameter("description");
        comment = req.getParameter("comment");
        discoveryURL = req.getParameter("discoveryPage");
        discoveryRE = req.getParameter("discoveryRE");
        license = req.getParameter("license");
        tags = req.getParameter("tags");

        if (this.mode == FormMode.CREATE) {
            this.createdAt = new Date();
            this.createdBy = UserServiceFactory.getUserService()
                    .getCurrentUser();
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
     * @param p
     *            the data will be stored here
     */
    public void fillObject(Package p) {
        p.description = description.trim();
        p.icon = icon.trim();
        p.title = title.trim();
        p.url = url.trim();
        p.license = license.trim();
        p.comment = comment.trim();
        p.discoveryPage = discoveryURL.trim();
        p.discoveryRE = discoveryRE.trim();
        p.discoveryURLPattern = discoveryURLPattern.trim();
        p.tags = NWUtils.split(tags, ',');
    }

    public String validate() {
        String msg = null;
        msg = Package.checkName(this.id);

        if (msg == null) {
            if (mode == FormMode.CREATE) {
                Objectify ofy = DefaultServlet.getObjectify();
                Package r = ofy.find(new Key<Package>(Package.class, this.id));
                if (r != null)
                    msg = "A package with this ID already exists";
            }
        }

        if (msg == null) {
            if (title.trim().isEmpty())
                msg = "Title cannot be empty";
        }
        if (msg == null) {
            if (!this.url.trim().isEmpty()) {
                msg = NWUtils.validateURL(this.url);
            }
        }
        if (msg == null) {
            if (!this.icon.trim().isEmpty()) {
                msg = NWUtils.validateURL(this.icon);
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
                    msg = "Cannot parse the regular expression: "
                            + e.getMessage();
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
        license = r.license.trim();
        comment = r.comment.trim();
        discoveryURL = r.discoveryPage.trim();
        discoveryRE = r.discoveryRE.trim();
        discoveryURLPattern = r.discoveryURLPattern.trim();
        createdAt = r.createdAt;
        createdBy = r.createdBy;
        tags = NWUtils.join(", ", r.tags);
    }
}
