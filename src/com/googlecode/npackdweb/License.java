package com.googlecode.npackdweb;

import javax.persistence.Id;
import javax.persistence.PrePersist;

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

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public String getName() {
		return name;
	}

	@PrePersist
	void onPersist() {
		DefaultServlet.dataVersion.incrementAndGet();
	}

	/**
	 * @return created Key for this object
	 */
	public Key<License> createKey() {
		return new Key<License>(License.class, name);
	}
}
