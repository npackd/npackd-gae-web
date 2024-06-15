package com.googlecode.npackdweb;

import com.google.appengine.tools.cloudstorage.*;
import com.googlecode.npackdweb.api.RepXMLPage;
import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.npackdweb.wlib.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
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

        GcsFilename fileName = new GcsFilename("npackd", tag + ".xml");
        GcsFilename fileNameExtra =
                new GcsFilename("npackd", tag + "_extra.xml");
        GcsFilename fileNameZIP = new GcsFilename("npackd", tag + ".zip");

        if (r.blobFile == null || recreate ||
                gcsService.getMetadata(fileName) == null ||
                gcsService.getMetadata(fileNameExtra) == null ||
                gcsService.getMetadata(fileNameZIP) == null) {
            HTMLWriter d;

            // XML
            if (tag.equals("unstable")) {
                d = RepXMLPage.toXML2(tag, true, false);
            } else {
                d = RepXMLPage.toXMLByPackageTag2(tag, true, false);
            }
            GcsOutputChannel outputChannel =
                    gcsService.createOrReplace(fileName,
                            GcsFileOptions.getDefaultInstance());
            OutputStream oout = Channels.newOutputStream(outputChannel);
            oout.write(
                    d.getContent().toString().getBytes(StandardCharsets.UTF_8));
            oout.close();

            // ZIP
            GcsOutputChannel outputChannelZIP =
                    gcsService.createOrReplace(fileNameZIP,
                            GcsFileOptions.getDefaultInstance());
            OutputStream ooutZIP = Channels.newOutputStream(outputChannelZIP);
            ZipOutputStream zos = new ZipOutputStream(ooutZIP);
            zos.setLevel(Deflater.BEST_COMPRESSION);
            ZipEntry e = new ZipEntry("Rep.xml");
            zos.putNextEntry(e);
            zos.write(
                    d.getContent().toString().getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.close();

            // XML with extra data
            if (tag.equals("unstable")) {
                d = RepXMLPage.toXML2(tag, true, true);
            } else {
                d = RepXMLPage.toXMLByPackageTag2(tag, true, true);
            }
            outputChannel =
                    gcsService.createOrReplace(fileNameExtra,
                            GcsFileOptions.getDefaultInstance());
            oout = Channels.newOutputStream(outputChannel);
            oout.write(
                    d.getContent().toString().getBytes(StandardCharsets.UTF_8));
            oout.close();

            r.blobFile =
                    "/gs/" + fileName.getBucketName() + "/" +
                            fileName.getObjectName();

            NWUtils.dsCache.saveRepository(r);
        }

        return r;
    }
}
