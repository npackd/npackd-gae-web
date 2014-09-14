package com.googlecode.npackdweb;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;

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
		Objectify ofy = DefaultServlet.getObjectify();
		List<Repository> rs = Repository.findAll(ofy);
		for (Repository r : rs)
			export(ofy, r.name, true);
		resp.setStatus(200);
		return null;
	}

	/**
	 * Exports a repository.
	 * 
	 * @param ob
	 *            Objectify instance
	 * @param tag
	 *            tag for package versions. Cannot be null.
	 * @param recreate
	 *            true = recreate the repository if it already exists
	 * @return the repository with blobFile != null
	 */
	public static Repository export(Objectify ob, String tag, boolean recreate)
			throws IOException {
		Repository r = ob.find(new Key<Repository>(Repository.class, tag));
		if (r == null) {
			r = new Repository();
			r.name = tag;
			NWUtils.saveRepository(ob, r);
		}

		final GcsService gcsService =
				GcsServiceFactory.createGcsService(RetryParams
						.getDefaultInstance());

		GcsFilename fileName = new GcsFilename("npackd", tag + ".xml");

		boolean exists = gcsService.getMetadata(fileName) != null;

		if (r.blobFile == null || recreate || !exists) {
			GcsOutputChannel outputChannel =
					gcsService.createOrReplace(fileName,
							GcsFileOptions.getDefaultInstance());

			Document d = RepXMLPage.toXML(ob, tag, true);

			OutputStream oout = Channels.newOutputStream(outputChannel);
			NWUtils.serializeXML(d, oout);
			oout.close();

			r.blobFile =
					"/gs/" + fileName.getBucketName() + "/" +
							fileName.getObjectName();

			NWUtils.saveRepository(ob, r);
		}

		return r;
	}
}
