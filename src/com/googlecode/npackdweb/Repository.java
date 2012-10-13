package com.googlecode.npackdweb;

import javax.persistence.Id;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import com.googlecode.objectify.annotation.Entity;

/**
 * A repository.
 */
@Entity
public class Repository {
	/** name of the repository */
	@Id
	String name;

	/**
	 * @return XML for the whole repository definition
	 */
	public Document toXML() {
		Document d;
		try {
			d = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument();
		} catch (ParserConfigurationException e) {
			throw (InternalError) new InternalError(e.getMessage())
					.initCause(e);
		}
		d.appendChild(d.createElement("root"));
		return d;
	}
}
