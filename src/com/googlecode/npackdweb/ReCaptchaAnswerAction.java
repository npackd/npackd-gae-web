package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaResponse;

import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;

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
        Objectify ob = NWUtils.getObjectify();
        ReCaptcha reCaptcha = NWUtils.createReCaptcha(ob);

        String challenge = req.getParameter("recaptcha_challenge_field");
        String uresponse = req.getParameter("recaptcha_response_field");
        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr,
                challenge, uresponse);

        String s;
        if (reCaptchaResponse.isValid()) {
            Editor e = ob.query(Editor.class).filter("id =", id).get();
            s = "Email address: " + e.name;
        } else {
            s = "Answer is wrong";
        }

        return new MessagePage(s);
    }
}
