package com.googlecode.npackdweb;

import javax.persistence.Id;
import javax.persistence.PostLoad;

import com.googlecode.objectify.annotation.Entity;

/**
 * A package.
 */
@Entity
public class Package {
	@Id
	String name = "";
	String title = "";
	String url = "";
	String description = "";
	String icon = "";
	String license = "";
	String comment = "";

	/**
	 * For Objectify.
	 */
	public Package() {
	}

	/**
	 * @param name
	 *            full internal name of the package
	 */
	public Package(String name) {
		this.name = name;
	}

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

	public String getLicense() {
		return license;
	}

	public String getComment() {
		return comment;
	}

	@PostLoad
	public void postLoad() {
		if (this.comment == null)
			this.comment = "";
	}
}
