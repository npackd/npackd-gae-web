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

    /**
     * @param tag only package versions with this tag will be exported.
     */
    public RepZIPPage(String tag) {
        this.tag = tag;
    }

    @Override
    public void create(HttpServletRequest request, HttpServletResponse resp)
            throws IOException {
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
