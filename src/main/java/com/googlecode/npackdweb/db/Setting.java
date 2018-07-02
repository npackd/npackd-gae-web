package com.googlecode.npackdweb.db;

import com.googlecode.npackdweb.NWUtils;
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

    Setting(com.google.appengine.api.datastore.Entity e) {
        this.name = e.getKey().getName();
        this.value = NWUtils.getString(e, "value");
    }

    com.google.appengine.api.datastore.Entity createEntity() {
        // onPersist();

        com.google.appengine.api.datastore.Entity e =
                new com.google.appengine.api.datastore.Entity("Setting",
                        this.name);

        e.setIndexedProperty("value", this.value);

        return e;
    }

    /**
     * @return created Key for this object
     */
    public Key<Setting> createKey() {
        return Key.create(Setting.class, this.name);
    }
}
