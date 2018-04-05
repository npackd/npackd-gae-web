package com.googlecode.npackdweb;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ReCaptcha for a user email address
 */
public class ReCaptchaAction extends Action {

    /**
     * -
     */
    public ReCaptchaAction() {
        super("^/recaptcha$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Page result = null;
        final String id_ = req.getParameter("id");

        if (id_ == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Empty user id");
        } else {
            int id = Integer.parseInt(id_);
            result = new ReCaptchaPage(id);
        }

        return result;
    }
}
