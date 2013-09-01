package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Edit a package, a package version or a complete repository as XML.
 */
public class EditAsXMLAction extends Action {
	/**
	 * -
	 */
	public EditAsXMLAction() {
		super("^/rep/edit-as-xml$", ActionSecurityType.ANONYMOUS);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String package_ = req.getParameter("package");
		String version = req.getParameter("version");

		Document d = NWUtils.newXMLRepository(false);

		Element root = d.getDocumentElement();
		String tag = "";
		if (package_ == null) {
			// nothing. Editing an empty repository.
		} else if (version == null) {
			Objectify ofy = NWUtils.getObjectify();
			Package r = ofy.get(new Key<Package>(Package.class, package_));
			Element e = r.toXML(d);
			root.appendChild(e);
		} else {
			Objectify ofy = NWUtils.getObjectify();
			PackageVersion r = ofy.get(new Key<PackageVersion>(
					PackageVersion.class, package_ + "@" + version));
			Element e = r.toXML(d);
			if (r.tags.size() > 0)
				tag = r.tags.get(0);
			root.appendChild(e);
		}

		return new EditAsXMLPage(d, tag);
	}
}
