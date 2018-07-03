package com.googlecode.npackdweb.db;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.googlecode.npackdweb.NWUtils;

/**
 * A setting.
 */
public class Setting {

    /**
     * name of the setting. This is the ID of the entity.
     */
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
    public Key createKey() {
        return KeyFactory.createKey("Setting", this.name);
    }
}
