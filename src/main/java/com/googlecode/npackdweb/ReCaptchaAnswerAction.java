package com.googlecode.npackdweb;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import static com.googlecode.npackdweb.NWUtils.getSetting;
import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * User's answer to a ReCaptcha.
 */
public class ReCaptchaAnswerAction extends Action {

    /**
     * -
     */
    public ReCaptchaAnswerAction() {
        super("^/recaptcha-answer$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int id = Integer.parseInt(req.getParameter("id"));

        String remoteAddr = req.getRemoteAddr();
        Objectify ob = DefaultServlet.getObjectify();

        String secretParameter = getSetting(ob, "ReCaptchaPrivateKey", "");
        String recap = req.getParameter("g-recaptcha-response");
        URLFetchService us = URLFetchServiceFactory.getURLFetchService();
        URL url = new URL(
                "https://www.google.com/recaptcha/api/siteverify?secret=" +
                secretParameter + "&response=" + recap + "&remoteip=" + req.
                getRemoteAddr());
        HTTPResponse r = us.fetch(url);
        JSONObject json = null;
        try {
            json = new JSONObject(new String(r.getContent(), Charset.
                    forName("UTF-8")));

            String s;
            if (json.getBoolean("success")) {
                Editor e = ob.query(Editor.class).filter("id =", id).get();
                s = "Email address: " + e.name;
            } else {
                s = "Answer is wrong";
            }

            return new MessagePage(s);
        } catch (JSONException ex) {
            throw new IOException(ex);
        }
    }
}
