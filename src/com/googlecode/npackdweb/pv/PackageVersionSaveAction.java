package com.googlecode.npackdweb.pv;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.PackageVersion;
import com.googlecode.npackdweb.Version;
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
		String package2 = req.getParameter("package");
		String version = req.getParameter("version");

		if (package2 == null || package2.trim().length() == 0
		        || version == null || version.trim().length() == 0)
			throw new InternalError("Wrong parameters");

		Version version_ = Version.parse(version);
		version_.normalize();
		version = version_.toString();

		Objectify ofy = NWUtils.getObjectify();
		PackageVersion p = ofy.find(new Key<PackageVersion>(
		        PackageVersion.class, package2 + "@" + version));
		if (p == null) {
			p = new PackageVersion();
			p.name = package2 + "@" + version;
			p.package_ = package2;
			p.version = version;
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

		NWUtils.savePackageVersion(ofy, p);

		resp.sendRedirect("/p/" + p.package_);
		return null;
	}
}
