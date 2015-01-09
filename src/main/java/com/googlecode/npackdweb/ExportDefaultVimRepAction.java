package com.googlecode.npackdweb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Exports the default Vim repository to GCS.
 */
public class ExportDefaultVimRepAction extends Action {
	/**
	 * -
	 */
	public ExportDefaultVimRepAction() {
		super("^/cron/export-default-vim-rep$", ActionSecurityType.ANONYMOUS);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Objectify ofy = DefaultServlet.getObjectify();
		export(ofy, true);
		resp.setStatus(200);
		return null;
	}

	/**
	 * Exports a repository.
	 * 
	 * @param ob
	 *            Objectify instance
	 * @param recreate
	 *            true = recreate the repository if it already exists
	 * @return the repository with blobFile != null
	 */
	public static Repository export(Objectify ob, boolean recreate)
			throws IOException {
		final String tag = "vim-org";

		URLFetchService s = URLFetchServiceFactory.getURLFetchService();
		HTTPRequest request =
				new HTTPRequest(
						new URL(
								"https://bitbucket.org/vimcommunity/vim-pi/raw/51a23481851685dda95cae07e97185870cf67723/db/vimorgsources.json"));
		request.getFetchOptions().setDeadline(60.0);
		HTTPResponse response = s.fetch(request);
		byte[] content = response.getContent();
		byte[] content2 = Arrays.copyOf(content, content.length - 1);
		content2[content2.length - 2] = '}';

		ByteArrayInputStream bais = new ByteArrayInputStream(content2);
		InputStreamReader isr = new InputStreamReader(bais, "UTF-8");
		JsonReader jr = new JsonReader(isr);
		jr.setLenient(true);
		JsonElement e = new JsonParser().parse(jr);

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

			Document d = toXML(e);

			OutputStream oout = Channels.newOutputStream(outputChannel);
			NWUtils.serializeXML(d, oout);
			NWUtils.serializeXML(d, System.out);
			oout.close();

			r.blobFile =
					"/gs/" + fileName.getBucketName() + "/" +
							fileName.getObjectName();

			NWUtils.saveRepository(ob, r);
		}

		return r;
	}

	private static Document toXML(JsonElement e) {
		Document d = NWUtils.newXMLRepository(true);
		Element root = d.getDocumentElement();

		if (e.isJsonObject()) {
			JsonObject a = e.getAsJsonObject();

			for (Entry<String, JsonElement> entry : a.entrySet()) {
				JsonElement p_ = entry.getValue();
				if (p_.isJsonObject()) {
					JsonObject p2 = p_.getAsJsonObject();
					String version = p2.get("version").getAsString();
					try {
						Version.parse(version);
					} catch (NumberFormatException e1) {
						continue;
					}
					String category = p2.get("script-type").getAsString();
					String archiveName = p2.get("archive_name").getAsString();
					String url = p2.get("url").getAsString();

					String package_ = "org.vim." + entry.getKey();
					if (package_.endsWith(".zip"))
						package_ = package_.substring(0, package_.length() - 4);
					package_ = package_.replace('.', '_');

					int nr = p2.get("vim_script_nr").getAsInt();
					if (archiveName.toLowerCase().endsWith(".zip") &&
							Package.checkName(package_) == null) {
						Package p = new Package(package_);
						p.title = "Vim plugin " + entry.getKey();
						p.tags.add("Text/" + category);
						p.icon =
								"https://lh6.googleusercontent.com/-oPg5OrLBr74/UZ8rV_mHduI/AAAAAAAACE0/twSVwJ4sOTQ/s800/vim.png";
						p.description = p.title;
						p.url =
								"http://www.vim.org/scripts/script.php?script_id=" +
										nr;

						PackageVersion pv =
								new PackageVersion(package_, version);
						pv.url = url;
						pv.addDependency(
								"com.googlecode.windows-package-manager.NpackdInstallerHelper",
								"[1.4, 2)", "nih");
						pv.addDependency("vim-pathogen", "[2.2, 3)", "nih");
						pv.addFile(".Npackd\\Install.bat",
								"call \"%NIH%\\RegisterVimPlugin.bat\" || exit /b %errorlevel%");
						pv.addFile(".Npackd\\Uninstall.bat",
								"call \"%NIH%\\UnregisterVimPlugin.bat\" || exit /b %errorlevel%");

						root.appendChild(p.toXML(d));
						root.appendChild(pv.toXML(d));
					}
				}
			}
		}

		return d;
	}
}
