package com.googlecode.npackdweb;

import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ZIP for a repository.
 */
public class RepZIPPage extends Page {

    private final String tag;
    private final boolean create;

    /**
     * @param tag only package versions with this tag will be exported.
     * @param create true = create the file, false = redirect to a Github
     * release asset
     */
    public RepZIPPage(String tag, boolean create) {
        this.tag = tag;
        this.create = create;
    }

    @Override
    public void create(HttpServletRequest request, HttpServletResponse resp)
            throws IOException {
        if (!create) {
            resp.sendRedirect(
                    "https://github.com/tim-lebedkov/npackd/releases/download/v1/" +
                    tag + ".zip");
        } else {
            final GcsFileMetadata md;
            try {
                md = NWUtils.getMetadata(tag + ".zip");
                NWUtils.serveFileFromGCS(md, request, resp,
                        "application/zip");
            } catch (ExecutionException ex) {
                throw new IOException(ex.getMessage());
            }
        }
    }
}
