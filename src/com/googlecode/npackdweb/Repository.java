package com.googlecode.npackdweb;

import javax.persistence.Id;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Entity;

/**
 * A repository.
 */
@Entity
public class Repository {
	@Id
	/** data store ID */
	Long id;

	/** name of the repository */
	String name;

	/** creator */
	User user;
}
