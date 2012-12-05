package com.googlecode.npackdweb.pv;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Save or create a package.
 */
public class PackageVersionSaveAction extends Action {
	/**
	 * -
	 */
	public PackageVersionSaveAction() {
		super("^/package-version/save$", ActionSecurityType.EDITOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
	        throws IOException {
		String name = req.getParameter("name");
		NWUtils.LOG.severe(name);
		Objectify ofy = NWUtils.getObjectify();
		PackageVersion p;
		if (name == null || name.trim().length() == 0) {
			p = new PackageVersion();
			p.name = name;
			int pos = name.indexOf("@");
			p.package_ = name.substring(0, pos);
			p.version = name.substring(pos + 1);
		} else {
			p = ofy.find(new Key<PackageVersion>(PackageVersion.class, name));
			if (p == null) {
				p = new PackageVersion();
				p.name = name;
				int pos = name.indexOf("@");
				p.package_ = name.substring(0, pos);
				p.version = name.substring(pos + 1);
			}
		}
		p.url = req.getParameter("url");
		p.sha1 = req.getParameter("sha1");
		p.detectMSI = req.getParameter("detectMSI");
		p.oneFile = "one-file".equals(req.getParameter("type"));
		p.tags = NWUtils.split(req.getParameter("tags"), ',');
		List<String> lines = NWUtils.splitLines(req
		        .getParameter("importantFiles"));
		p.importantFilePaths.clear();
		p.importantFileTitles.clear();
		for (String line : lines) {
			int pos = line.indexOf(" ");
			if (pos > 0) {
				String path = line.substring(0, pos);
				String title = line.substring(pos + 1);
				p.importantFilePaths.add(path);
				p.importantFileTitles.add(title);
			}
		}

		p.clearFiles();
		for (int i = 0;; i++) {
			String path = req.getParameter("path." + i);
			if (path == null)
				break;

			if (!path.trim().isEmpty()) {
				String content = req.getParameter("content." + i);
				p.addFile(path, content);
			}
		}

		p.dependencyPackages.clear();
		p.dependencyVersionRanges.clear();
		p.dependencyEnvVars.clear();
		for (int i = 0;; i++) {
			String package_ = req.getParameter("depPackage." + i);
			if (package_ == null)
				break;

			if (!package_.trim().isEmpty()) {
				String versions = req.getParameter("depVersions." + i);
				String envVar = req.getParameter("depEnvVar." + i);
				p.dependencyPackages.add(package_);
				p.dependencyVersionRanges.add(versions);
				p.dependencyEnvVars.add(envVar);
			}
		}

		ofy.put(p);
		DefaultServlet.dataVersion.incrementAndGet();
		resp.sendRedirect("/p/" + p.package_);
		return null;
	}
}
