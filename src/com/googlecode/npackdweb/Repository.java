package com.googlecode.npackdweb;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;

/**
 * A repository.
 */
@Entity
public class Repository {
	/** name of the repository */
	@Id
	public String name;

	/** last modification date */
	public Date lastModifiedAt;

	/** path to the XML blob or null */
	public String blobFile;

	@PostLoad
	public void postLoad() {
		if (this.lastModifiedAt == null)
			this.lastModifiedAt = new Date();
	}

	@PrePersist
	void onPersist() {
		DefaultServlet.dataVersion.incrementAndGet();
		this.lastModifiedAt = new Date();
	}

	/**
	 * @return created Key for this object
	 */
	public Key<Repository> createKey() {
		return new Key<Repository>(Repository.class, name);
	}
}
