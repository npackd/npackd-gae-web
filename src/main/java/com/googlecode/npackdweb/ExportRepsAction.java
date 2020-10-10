package com.googlecode.npackdweb;

import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        List<Repository> rs = NWUtils.dsCache.findAllRepositories();
        for (Repository r : rs) {
            export(r.name, true);
        }
        resp.setStatus(200);
        return null;
    }

    /**
     * Exports a repository.
     *
     * @param tag tag for package versions. Cannot be null.
     * @param recreate true = recreate the repository if it already exists
     * @return the repository with blobFile != null
     * @throws java.io.IOException any error
     */
    public static Repository export(
            String tag, boolean recreate)
            throws IOException {
        Repository r = NWUtils.dsCache.findRepository(tag, true);
        if (r == null) {
            r = new Repository();
            r.name = tag;
            NWUtils.dsCache.saveRepository(r);
        }

        /* TODO
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
         */
        return r;
    }
}
