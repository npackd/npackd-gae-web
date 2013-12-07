package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Objectify;

/**
 * Adds an editor.
 */
public class AddEditorConfirmedAction extends Action {
	/**
	 * -
	 */
	public AddEditorConfirmedAction() {
		super("^/add-editor-confirmed$", ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Page res = null;

		AddEditorPage p = new AddEditorPage();
		p.fill(req);
		String err = p.validate();
		if (err == null) {
			Editor e = new Editor(new User(p.email, p.email.substring(p.email
					.indexOf('@'))));
			Objectify ofy = DefaultServlet.getObjectify();
			e.createId();
			NWUtils.saveEditor(ofy, e);
			res = new MessagePage("Editor " + p.email
					+ " was added successfully");
		} else {
			p.error = err;
			res = p;
		}

		return res;
	}
}
