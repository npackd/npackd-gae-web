package com.googlecode.npackdweb;

import com.google.appengine.tools.cloudstorage.*;
import com.googlecode.npackdweb.api.RepXMLPage;
import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Exports repositories to the GCS.
 */
public class ExportRepAction extends Action {

    /**
     * -
     */
    public ExportRepAction() {
        super("^/cron/export-rep$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final GcsService gcsService =
                GcsServiceFactory.createGcsService(RetryParams
                        .getDefaultInstance());

        export(gcsService, req.getParameter("tag"), true);
        resp.setStatus(200);
        return null;
    }

    /**
     * Exports a repository.
     *
     * @param gcsService GCS
     * @param tag tag for package versions. Cannot be null.
     * @param recreate true = recreate the repository if it already exists
     * @return the repository with blobFile != null
     * @throws IOException any error
     */
    public static Repository export(GcsService gcsService,
                                    String tag, boolean recreate)
            throws IOException {
        Repository r = NWUtils.dsCache.findRepository(tag, true);
        if (r == null) {
            r = new Repository();
            r.name = tag;
            NWUtils.dsCache.saveRepository(r);
        }

        GcsFilename fileName = new GcsFilename("npackd", tag + ".xml");
        GcsFilename fileNameZIP = new GcsFilename("npackd", tag + ".zip");

        if (r.blobFile == null || recreate ||
                gcsService.getMetadata(fileName) == null ||
                gcsService.getMetadata(fileNameZIP) == null) {
            Document d;

            if (tag.equals("unstable")) {
                d = RepXMLPage.toXML(tag, true);
            } else {
                d = RepXMLPage.toXMLByPackageTag(tag, true);
            }

            GcsOutputChannel outputChannel =
                    gcsService.createOrReplace(fileName,
                            GcsFileOptions.getDefaultInstance());
            OutputStream oout = Channels.newOutputStream(outputChannel);
            NWUtils.serializeXML(d, oout);
            oout.close();

            GcsOutputChannel outputChannelZIP =
                    gcsService.createOrReplace(fileNameZIP,
                            GcsFileOptions.getDefaultInstance());
            OutputStream ooutZIP = Channels.newOutputStream(outputChannelZIP);
            ZipOutputStream zos = new ZipOutputStream(ooutZIP);
            zos.setLevel(Deflater.BEST_COMPRESSION);
            ZipEntry e = new ZipEntry("Rep.xml");
            zos.putNextEntry(e);
            NWUtils.serializeXML(d, zos);
            zos.closeEntry();
            zos.close();

            r.blobFile =
                    "/gs/" + fileName.getBucketName() + "/" +
                            fileName.getObjectName();

            NWUtils.dsCache.saveRepository(r);
        }

        return r;
    }
}
