package com.googlecode.npackdweb.wlib;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Always returns the specified HTTP status code.
 */
public class SendStatusAction extends Action {

    private final int status;

    /**
     * @param urlRegEx regular expression for URLs. Example: "^/def$"
     * @param status HTTP status code
     */
    public SendStatusAction(String urlRegEx, int status) {
        super(urlRegEx, ActionSecurityType.ANONYMOUS);
        this.status = status;
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (this.status / 100 == 2) {
            resp.setStatus(this.status);
        } else {
            resp.sendError(status);
        }
        return null;
    }
}
