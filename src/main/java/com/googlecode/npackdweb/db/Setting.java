package com.googlecode.npackdweb.db;

import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Entity;

/**
 * A setting.
 */
@Entity
@Cached
public class Setting {
	/** name of the setting */
	@Id
	public String name = "";

	/** value of the setting */
	public String value = "";

	/**
	 * For Objectify.
	 */
	public Setting() {
	}

	@PostLoad
	public void postLoad() {
	}

	@PrePersist
	void onPersist() {
	}

	/**
	 * @return created Key for this object
	 */
	public Key<Setting> createKey() {
		return new Key<>(Setting.class, this.name);
	}
}
