package com.googlecode.npackdweb.license;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.License;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Save or create a license.
 */
public class LicenseSaveAction extends Action {
	/**
	 * -
	 */
	public LicenseSaveAction() {
		super("^/license/save$", ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String name = req.getParameter("name");
		Objectify ofy = DefaultServlet.getObjectify();
		LicensePage pdp = new LicensePage();
		pdp.fill(req);
		String msg = pdp.validate();
		if (msg == null) {
			License p = ofy.find(new Key<License>(License.class, name));
			if (p == null) {
				p = new License();
				p.name = name;
			} else if (!p.isCurrentUserPermittedToModify())
				return new MessagePage(
						"You do not have permission to modify this license");
			pdp.fillObject(p);
			NWUtils.saveLicense(ofy, p, true);
			pdp = null;
			resp.sendRedirect("/l/" + p.name);
		} else {
			pdp.error = msg;
		}
		return pdp;
	}
}
