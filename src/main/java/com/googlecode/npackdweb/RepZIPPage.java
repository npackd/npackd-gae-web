package com.googlecode.npackdweb;

import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;
import static com.googlecode.objectify.ObjectifyService.ofy;
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
        Objectify ofy = ofy();
        final GcsService gcsService =
                GcsServiceFactory.createGcsService(RetryParams
                        .getDefaultInstance());

        GcsFilename fileName = new GcsFilename("npackd", tag + ".zip");
        GcsFileMetadata md = gcsService.getMetadata(fileName);

        if (md == null) {
            ExportRepsAction.export(gcsService, tag, false);
            md = gcsService.getMetadata(fileName);
        }

        NWUtils.serveFileFromGCS(gcsService, md, request, resp,
                "application/zip");
    }
}
