package com.googlecode.npackdweb.db;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Entity;

/**
 * A license definition.
 */
@Entity
@Cached
public class License {
	@Id
	public String name;
	public String title;
	public String url;

	/** last modification date */
	public Date lastModifiedAt;

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public String getName() {
		return name;
	}

	@PostLoad
	public void postLoad() {
		if (this.lastModifiedAt == null)
			this.lastModifiedAt = new Date();
	}

	@PrePersist
	void onPersist() {
		NWUtils.incDataVersion();
		this.lastModifiedAt = new Date();
	}

	/**
	 * @return created Key for this object
	 */
	public Key<License> createKey() {
		return new Key<License>(License.class, name);
	}
}
