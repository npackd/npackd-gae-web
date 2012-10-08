package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

/**
 * Packages.
 */
public class PackagePage extends FramePage {
	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		Objectify ofy = ObjectifyService.begin();
		User user = UserServiceFactory.getUserService().getCurrentUser();
		Query<Package> q = ofy.query(Package.class).filter("createdBy =", user);

		StringBuilder sb = new StringBuilder();
		for (Package r : q) {
			sb.append(NWUtils.tmpl("PackageLink.html", "title", r.name, "id",
					r.id.toString()));
		}

		return NWUtils.tmpl("Packages.html", NWUtils.newMap("packages", sb
				.toString()));
	}

	@Override
	public String getTitle() {
		return "Packages";
	}
}
