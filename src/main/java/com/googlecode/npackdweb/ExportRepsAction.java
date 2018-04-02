package com.googlecode.npackdweb;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;

/**
 * Exports repositories to the GCS.
 */
public class ExportRepsAction extends Action {

    /**
     * -
     */
    public ExportRepsAction() {
        super("^/cron/export-reps$", ActionSecurityType.ANONYMOUS);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final GcsService gcsService =
                GcsServiceFactory.createGcsService(RetryParams
                        .getDefaultInstance());

        Objectify ofy = DefaultServlet.getObjectify();
        List<Repository> rs = Repository.findAll(ofy);
        for (Repository r : rs) {
            export(gcsService, ofy, r.name, true);
        }
        resp.setStatus(200);
        return null;
    }

    /**
     * Exports a repository.
     *
     * @param gcsService GCS
     * @param ob Objectify instance
     * @param tag tag for package versions. Cannot be null.
     * @param recreate true = recreate the repository if it already exists
     * @return the repository with blobFile != null
     * @throws java.io.IOException any error
     */
    public static Repository export(GcsService gcsService, Objectify ob,
            String tag, boolean recreate)
            throws IOException {
        Repository r = ob.load().key(Key.create(Repository.class, tag)).now();
        if (r == null) {
            r = new Repository();
            r.name = tag;
            NWUtils.saveRepository(ob, r);
        }

        GcsFilename fileName = new GcsFilename("npackd", tag + ".xml");
        GcsFilename fileNameZIP = new GcsFilename("npackd", tag + ".zip");

        if (r.blobFile == null || recreate ||
                gcsService.getMetadata(fileName) == null ||
                gcsService.getMetadata(fileNameZIP) == null) {
            Document d = RepXMLPage.toXML(ob, tag, true);

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

            NWUtils.saveRepository(ob, r);
        }

        return r;
    }
}
