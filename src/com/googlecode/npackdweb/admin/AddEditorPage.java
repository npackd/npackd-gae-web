package com.googlecode.npackdweb.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Add a new editor
 */
public class AddEditorPage extends MyPage {
    /** error message or null */
    public String error;

    /** entered email address */
    public String email = "";

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter w = new HTMLWriter();
        if (error != null) {
            w.e("p", "class", "nw-error", this.error);
        }
        w.start("form", "method", "post", "action", "/add-editor-confirmed");
        w.t("Email: ");
        w.e("input", "type", "text", "name", "email", "value", email, "size",
                "40");
        w.e("br");
        w.e("input", "class", "input", "type", "submit", "value", "Add");
        w.end("form");
        return w.toString();
    }

    @Override
    public void fill(HttpServletRequest req) {
        this.email = req.getParameter("email");
    }

    @Override
    public String validate() {
        String err = null;

        err = NWUtils.validateEmail(this.email);

        if (err == null) {
            Objectify ofy = DefaultServlet.getObjectify();
            Editor existing = ofy.find(new Key<Editor>(Editor.class, email));
            if (existing != null)
                err = "An editor with the email " + email + " already exists";

        }
        return err;
    }

    @Override
    public String getTitle() {
        return "Add editor";
    }
}
