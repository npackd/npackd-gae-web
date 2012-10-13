package com.googlecode.npackdweb;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Entity;

/**
 * A license definition.
 */
@Entity
public class License {
	@Id
	String name;
	String title;
	String url;
}
