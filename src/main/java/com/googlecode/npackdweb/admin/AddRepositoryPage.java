package com.googlecode.npackdweb.admin;

import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

/**
 * Add a new repository.
 */
public class AddRepositoryPage extends MyPage {

    /**
     * entered tag name
     */
    public String tag = "";

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter w = new HTMLWriter();
        if (error != null) {
            w.e("p", "class", "bg-danger", this.error);
        }
        w.start("form", "method", "post", "action", "/add-repository-confirmed");
        w.t("Email: ");
        w.e("input", "type", "text", "name", "tag", "value", tag, "size",
                "40");
        w.e("br");
        w.e("input", "class", "input", "type", "submit", "value", "Add");
        w.end("form");
        return w.toString();
    }

    @Override
    public void fill(HttpServletRequest req) {
        this.tag = req.getParameter("tag");
    }

    @Override
    public String validate() {
        String err = null;

        if (this.tag.trim().isEmpty()) {
            err = "An empty tag name is not allowed";
        }

        if (err == null) {
            Repository existing = NWUtils.dsCache.findRepository(tag, true);
            if (existing != null) {
                err = "A repository with the tag " + tag + " already exists";
            }
        }

        return err;
    }

    @Override
    public String getTitle() {
        return "Add repository";
    }
}
