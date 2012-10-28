package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Edit a package, a package version or a complete repository as XML.
 */
public class EditAsXMLAction extends Action {
	/**
	 * -
	 */
	public EditAsXMLAction() {
		super("^/rep/edit-as-xml$");
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String package_ = req.getParameter("package");
		String version = req.getParameter("version");

		Document d = NWUtils.newXMLRepository(false);

		Element root = d.getDocumentElement();
		if (package_ == null) {
			// nothing. Editing an empty repository.
		} else if (version == null) {
			Objectify ofy = ObjectifyService.begin();
			Package r = ofy.get(new Key<Package>(Package.class, package_));
			Element e = r.toXML(d);
			root.appendChild(e);
		} else {
			Objectify ofy = ObjectifyService.begin();
			PackageVersion r = ofy.get(new Key<PackageVersion>(
					PackageVersion.class, package_ + "@" + version));
			Element e = r.toXML(d);
			root.appendChild(e);
		}

		return new EditAsXMLPage(d);
	}
}
