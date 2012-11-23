package com.googlecode.npackdweb;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Exports default repositories to BLOBs.
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
		export("stable", true);
		export("stable64", true);
		export("libs", true);
		export("unstable", true);
		resp.setStatus(200);
		return null;
	}

	/**
	 * Exports a repository.
	 * 
	 * @param tag
	 *            tag for package versions. Cannot be null.
	 * @param recreate
	 *            true = recreate the repository if it already exists
	 * @return the repository with blobFile != null
	 */
	public static Repository export(String tag, boolean recreate)
	        throws IOException {
		Objectify ob = ObjectifyService.begin();
		Repository r = ob.find(new Key<Repository>(Repository.class, tag));
		if (r == null) {
			r = new Repository();
			r.name = tag;
			ob.put(r);
		}

		String blobToDelete = null;

		if (r.blobFile == null || recreate) {
			blobToDelete = r.blobFile;

			Document d = RepXMLPage.toXML(tag);

			// Get a file service
			FileService fileService = FileServiceFactory.getFileService();

			// Create a new Blob file with mime-type "text/plain"
			AppEngineFile file = fileService
			        .createNewBlobFile("application/xml");

			// Open a channel to write to it
			FileWriteChannel writeChannel = fileService.openWriteChannel(file,
			        true);

			OutputStream os = Channels.newOutputStream(writeChannel);
			NWUtils.serializeXML(d, os);
			os.close();

			// Now finalize
			writeChannel.closeFinally();

			BlobKey blobKey = fileService.getBlobKey(file);
			file = fileService.getBlobFile(blobKey);

			r.blobFile = file.getFullPath();
			NWUtils.LOG.warning(file.getNamePart());
			ob.put(r);
		}

		// delete the blob later after we recreated the repository
		if (blobToDelete != null) {
			FileService fileService = FileServiceFactory.getFileService();
			try {
				fileService.delete(new AppEngineFile(blobToDelete));
			} catch (Exception e) {
				NWUtils.LOG.log(Level.WARNING, "cannot delete a blob", e);
			}
		}

		return r;
	}
}
