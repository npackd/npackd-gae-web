package com.googlecode.npackdweb;

import javax.persistence.Id;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;

/**
 * A package.
 */
@Entity
public class Package {
	@Id
	Long id;
	String name;
	String title;
	String url;
	String description;
	String icon;
	User createdBy;
	Key<Repository> repository;
}
