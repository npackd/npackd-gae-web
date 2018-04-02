package com.googlecode.npackdweb.db;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * A setting.
 */
@Entity
@Cache
@Index
public class Setting {

    /**
     * name of the setting
     */
    @Id
    public String name = "";

    /**
     * value of the setting
     */
    public String value = "";

    /**
     * For Objectify.
     */
    public Setting() {
    }

    /**
     * @return created Key for this object
     */
    public Key<Setting> createKey() {
        return Key.create(Setting.class, this.name);
    }
}
