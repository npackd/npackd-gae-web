package com.googlecode.npackdweb;

import com.googlecode.npackdweb.wlib.HTMLWriter;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Shows XML for a repository.
 */
public class EditAsXMLPage extends MyPage {
    private final Document xml;
    private final String tag;

    /**
     * @param xml XML for a repository
     * @param tag tag that should be assigned to all package versions
     */
    public EditAsXMLPage(Document xml, String tag) {
        this.xml = xml;
        this.tag = tag;
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter w = new HTMLWriter();
        w.start("form", "action", "/rep/upload", "method", "POST");
        w.start("table", "style", "width: 100%");
        w.start("tr");
        w.e("td", "Repository:");
        w.start("td");
        w.e("textarea", "class", "form-control", "rows", "20", "cols", "120",
                "name", "repository", "wrap", "off", "style", "width: 100%",
                NWUtils.toString(this.xml));
        w.end("td");
        w.end("tr");

        w.start("tr");
        w.e("td", "Tag for package versions:");
        w.start("td");
        w.e("input", "class", "form-control", "type", "text", "name", "tag",
                "value", tag);
        w.t("Please use one of the repository names to place the package versions in the right repository");
        w.end("td");
        w.end("tr");

        w.start("tr");
        w.e("td", "Overwrite:");
        w.start("td");
        w.e("input", "type", "checkbox", "name", "overwrite");
        w.t("If this checkbox is not selected, only new packages, package versions and licenses will be created");
        w.end("td");
        w.end("tr");

        w.end("table");
        w.e("input", "type", "submit", "class", "btn btn-default", "value",
                "submit");
        w.end("form");

        return w.toString();
    }

    @Override
    public String getTitle() {
        return "Upload repository";
    }
}
