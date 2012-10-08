package com.googlecode.npackdweb;

import javax.persistence.Id;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.AlsoLoad;
import com.googlecode.objectify.annotation.Entity;

/**
 * A package version.
 */
@Entity
public class PackageVersion {
	@Id
	Long id;
	String name;
	User createdBy;
	@AlsoLoad("package")
	Key<Package> package_;
	Key<Repository> repository;
}
