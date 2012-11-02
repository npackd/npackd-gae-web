package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Save <file> in a package version.
 */
public class PackageVersionFileSaveAction extends Action {
	/**
	 * -
	 */
	public PackageVersionFileSaveAction() {
		super("^/pv-file/save$");
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String name = req.getParameter("name");
		String path = req.getParameter("path");
		String content = req.getParameter("content");

		Objectify ofy = ObjectifyService.begin();
		PackageVersion r = ofy.get(new Key<PackageVersion>(
				PackageVersion.class, name));

		int index = r.filePaths.indexOf(path);
		if (index >= 0) {
			if (content.isEmpty()) {
				r.removeFile(index);
			} else {
				r.setFileContents(index, content);
			}
		} else {
			if (!content.isEmpty()) {
				r.addFile(path, content);
			}
		}
		ofy.put(r);

		int pos = name.indexOf("@");
		String package_ = name.substring(0, pos);
		String version = name.substring(pos + 1);
		resp.sendRedirect("/p/" + package_ + "/" + version);

		return null;
	}
}
