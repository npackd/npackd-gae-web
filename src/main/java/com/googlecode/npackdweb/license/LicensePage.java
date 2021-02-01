package com.googlecode.npackdweb.license;

import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import java.io.IOException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;

/**
 * A license.
 */
public class LicensePage extends MyPage {

    /**
     * full license id or ""
     */
    public String id;

    /**
     * license title
     */
    public String title;

    /**
     * license home page
     */
    public String url;

    /**
     * date and time of the last modifition
     */
    public Date modifiedAt;

    /**
     * -
     */
    public LicensePage() {
        id = "";
        title = "";
        url = "";
        modifiedAt = NWUtils.newDate();
    }

    @Override
    public String createBodyBottom(HttpServletRequest request)
            throws IOException {
        HTMLWriter w = new HTMLWriter();

        w.unencoded(NWUtils.tmpl("LicensePage.html"));

        return w.toString();
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        boolean editable;
        boolean exists = false;
        if (this.id == null || this.id.trim().length() == 0) {
            editable = true;
        } else {
            editable = NWUtils.isAdminLoggedIn();
            exists = true;
        }

        HTMLWriter w = new HTMLWriter();

        if (editable) {
            w.start("form", "class", "form-horizontal", "method", "post",
                    "action", "/license/save", "id", "package-form");
        }

        if (editable && exists) {
            w.e("input", "type", "hidden", "name", "name", "value", id);
        }

        if (editable && !exists) {
            w.e("input", "type", "hidden", "name", "new", "value", "true");
        }

        if (editable) {
            w.start("div", "class", "btn-group");
            w.e("input", "class", "btn btn-default", "type", "submit", "value",
                    "Save");
            if (exists) {
                NWUtils.jsButton(w, "Edit as XML",
                        "/rep/edit-as-xml?type=license&id=" + id,
                        "Edit this license as repository XML");
                NWUtils.jsButton(w, "Delete", "/license/delete?id=" + id,
                        "Deletes this license");
            }
            w.end("div");
        }

        w.start("table", "border", "0");

        // internal name
        w.start("tr");
        w.e("td", "ID:");
        if (exists) {
            w.e("td", id);
        } else {
            w.start("td");
            w.e("input", "class", "form-control", "type", "text", "name",
                    "name", "value", id, "size", "80", "title",
                    "Full license name including the reversed domain name");
            w.start("p", "class", "nw-help");
            w.t(" See ");
            w.e("a",
                    "href",
                    "https://github.com/npackd/npackd/wiki/RepositoryFormat#package-naming-rules",
                    "target", "_blank", "License naming rules");
            w.t(" for more details");
            w.end("p");
            w.end("td");
        }
        w.end("tr");

        // title
        if (editable) {
            w.start("tr");
            w.e("td", "Title:");
            w.start("td");
            w.e("input", "class", "form-control", "type", "text", "name",
                    "title", "value", title, "size", "80", "title",
                    "Name of the license");
            w.end("td");
            w.end("tr");
        }

        w.start("tr");
        w.start("td");
        w.t("License home page ");
        w.e("small", "(optional):");
        w.end("td");
        w.start("td");
        if (editable) {
            w.e("input", "id", "url", "style", "display: inline; width: 90%",
                    "class", "form-control", "type", "url", "name", "url",
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
        w.e("td", "Modified:");
        w.start("td");
        w.t(modifiedAt == null ? "" : modifiedAt.toString());
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
        return title.isEmpty() ? "License" : title;
    }

    @Override
    public String getTitleHTML() {
        HTMLWriter w = new HTMLWriter();
        w.e("img", "src", "/App.png");

        if (this.id != null && this.id.trim().length() > 0) {
            w.t(" " + title);

            w.unencoded(
                    " <div class='g-plusone' data-size='medium' data-annotation='inline' data-width='300' data-href='" +
                    NWUtils.WEB_SITE + "/p/" + id + "'></div>");
        } else {
            w.t(" New license");
        }

        return w.toString();
    }

    /**
     * Fills the values from an HTTP request.
     *
     * @param req HTTP request
     */
    @Override
    public void fill(HttpServletRequest req) {
        id = req.getParameter("name");
        title = req.getParameter("title");
        url = req.getParameter("url");

        if (this.id == null || this.id.trim().length() == 0) {
            this.modifiedAt = NWUtils.newDate();
        } else {
            License p = NWUtils.dsCache.getLicense(id, false);
            this.modifiedAt = p.lastModifiedAt;
        }
    }

    /**
     * Transfers the data from this form into the specified object.
     *
     * @param p the data will be stored here
     */
    public void fillObject(License p) {
        p.title = title.trim();
        p.url = url.trim();
    }

    @Override
    public String validate() {
        String msg = Package.checkName(this.id);

        if (msg == null) {
            if (this.id == null || this.id.trim().length() == 0) {
                License r = NWUtils.dsCache.getLicense(this.id, false);
                if (r != null) {
                    msg = "A license with this ID already exists";
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

        return msg;
    }

    public void fill(License r) {
        this.id = r.name;
        title = r.title.trim();
        url = r.url.trim();
        modifiedAt = r.lastModifiedAt;
    }
}
