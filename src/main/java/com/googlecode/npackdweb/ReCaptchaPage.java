package com.googlecode.npackdweb;

import static com.googlecode.npackdweb.NWUtils.getSetting;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Objectify;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

/**
 * Shows a ReCaptcha page for an email address.
 */
public class ReCaptchaPage extends MyPage {

    private int id;

    /**
     * -
     *
     * @param id Editor.id
     */
    public ReCaptchaPage(int id) {
        this.id = id;
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter w = new HTMLWriter();
        w.start("form", "action", "/recaptcha-answer", "method", "post");
        Objectify ob = DefaultServlet.getObjectify();
        w.e("div", "class", "g-recaptcha", "data-sitekey", getSetting(ob,
                "ReCaptchaPublicKey", ""));
        w.end("div");
        w.e("input", "type", "hidden", "name", "id", "value",
                Integer.toString(id));
        w.e("input", "type", "submit", "value", "submit");
        w.end("form");
        return w.toString();
    }

    @Override
    public String getTitle() {
        return "ReCaptcha";
    }

    @Override
    public String getHeadPart() {
        return "<script src=\"https://www.google.com/recaptcha/api.js\" async defer></script>";
    }
}
