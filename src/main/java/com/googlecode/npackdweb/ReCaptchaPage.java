package com.googlecode.npackdweb;

import com.googlecode.npackdweb.wlib.HTMLWriter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Shows a ReCaptcha page for an email address.
 */
public class ReCaptchaPage extends MyPage {

    private final int id;

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
        w.e("div", "class", "g-recaptcha", "data-sitekey", NWUtils.dsCache.
                getSetting(
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
    public String getScriptsPart() {
        return "<script src=\"https://www.google.com/recaptcha/api.js\" async defer></script>";
    }
}
