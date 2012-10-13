package com.googlecode.npackdweb;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Entity;

/**
 * A package.
 */
@Entity
public class Package {
	@Id
	String name;
	String title;
	String url;
	String description;
	String icon;

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public String getDescription() {
		return description;
	}

	public String getIcon() {
		return icon;
	}
}
