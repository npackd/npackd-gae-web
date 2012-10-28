package com.googlecode.npackdweb;

import javax.persistence.Id;
import javax.persistence.PostLoad;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

	/**
	 * <package>
	 * 
	 * @param d
	 *            XML document
	 * @return <package>
	 */
	public Element toXML(Document d) {
		Package p = this;
		Element package_ = d.createElement("package");
		package_.setAttribute("name", p.name);
		if (!p.title.isEmpty())
			NWUtils.e(package_, "title", p.title);
		if (!p.url.isEmpty())
			NWUtils.e(package_, "url", p.url);
		if (!p.description.isEmpty())
			NWUtils.e(package_, "description", p.description);
		if (!p.icon.isEmpty())
			NWUtils.e(package_, "icon", p.icon);
		if (!p.license.isEmpty())
			NWUtils.e(package_, "license", p.license);

		return package_;
	}
}
