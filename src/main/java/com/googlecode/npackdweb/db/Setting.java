package com.googlecode.npackdweb.db;

import java.sql.ResultSet;

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
     * -
     */
    public Setting() {
    }

    Setting(ResultSet e) {
        /* TODO this.name = e.getKey().getName();
        this.value = NWUtils.getString(e, "value");
         */
    }

    ResultSet createEntity() {
        /* TODO: // onPersist();

        com.google.appengine.api.datastore.Entity e =
                new com.google.appengine.api.datastore.Entity("Setting",
                        this.name);

        e.setIndexedProperty("value", this.value);

        return e;
         */
        return null;
    }
}
