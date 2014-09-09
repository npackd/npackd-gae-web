package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;

import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Objectify;

/**
 * Shows a ReCaptcha page for an email address.
 */
public class ReCaptchaPage extends MyPage {
    private int id;

    /**
     * -
     * 
     * @param id
     *            Editor.id
     */
    public ReCaptchaPage(int id) {
        this.id = id;
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter w = new HTMLWriter();
        w.start("form", "action", "/recaptcha-answer", "method", "post");
        Objectify ob = DefaultServlet.getObjectify();
        ReCaptcha c = NWUtils.createReCaptcha(ob);
        w.unencoded(c.createRecaptchaHtml(null, null));
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
}
