package com.googlecode.npackdweb;

import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ZIP for a repository.
 */
public class RepZIPPage extends Page {

    private final String tag;

    /**
     * @param tag only package versions with this tag will be exported.
     */
    public RepZIPPage(String tag) {
        this.tag = tag;
    }

    @Override
    public void create(HttpServletRequest request, HttpServletResponse resp)
            throws IOException {
        NWUtils.serveFileFromGCS(NWUtils.BASE_PATH + "/rep/" + tag + ".zip",
                request, resp,
                "application/zip");
    }
}
