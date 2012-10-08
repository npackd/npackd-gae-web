package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

/**
 * List of repositories.
 */
public class RepPage extends FramePage {
	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		Objectify ofy = ObjectifyService.begin();
		User user = UserServiceFactory.getUserService().getCurrentUser();
		Query<Repository> q = ofy.query(Repository.class)
				.filter("user =", user);

		StringBuilder sb = new StringBuilder();
		for (Repository r : q) {
			sb.append(NWUtils.tmpl("rep/RepositoryLink.html", "title", r.name,
					"id", r.id.toString()));
		}

		return NWUtils.tmpl("rep/Content.html", NWUtils.newMap("repositories",
				sb.toString()));
	}

	@Override
	public String getTitle() {
		return "Repositories";
	}
}
