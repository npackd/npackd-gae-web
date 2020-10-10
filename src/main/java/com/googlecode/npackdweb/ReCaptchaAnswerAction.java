package com.googlecode.npackdweb;

import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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

        String secretParameter = NWUtils.dsCache.
                getSetting("ReCaptchaPrivateKey", "");
        String recap = req.getParameter("g-recaptcha-response");

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(
                "https://www.google.com/recaptcha/api/siteverify?secret=" +
                secretParameter + "&response=" + recap + "&remoteip=" + req.
                        getRemoteAddr());
        CloseableHttpResponse r = httpclient.execute(httpGet);

        // The underlying HTTP connection is still held by the response object
        // to allow the response content to be streamed directly from the network socket.
        // In order to ensure correct deallocation of system resources
        // the user MUST call CloseableHttpResponse#close() from a finally clause.
        // Please note that if response content is not fully consumed the underlying
        // connection cannot be safely re-used and will be shut down and discarded
        // by the connection manager.
        try {
            HttpEntity e = r.getEntity();

            byte[] content = EntityUtils.toByteArray(e);

            JSONObject json;
            try {
                json = new JSONObject(new String(content, Charset.
                        forName("UTF-8")));

                String s;
                if (json.getBoolean("success")) {
                    Editor ed = NWUtils.dsCache.findEditor(id);
                    s = "Email address: " + ed.name;
                } else {
                    s = "Answer is wrong";
                }

                return new MessagePage(s);
            } catch (JSONException ex) {
                throw new IOException(ex);
            }

        } finally {
            r.close();
        }

    }
}
