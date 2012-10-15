package com.googlecode.npackdweb;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Entity;

/**
 * A repository.
 */
@Entity
public class Repository {
	/** name of the repository */
	@Id
	String name;
}
