package com.googlecode.npackdweb.db;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.googlecode.npackdweb.NWUtils;
import java.util.Date;

/**
 * A repository.
 */
public class Repository {

    /**
     * name of the repository. This is the ID of the entity.
     */
    public String name;

    /**
     * last modification date
     */
    public Date lastModifiedAt;

    /**
     * path to the XML blob or null
     */
    public String blobFile;

    public Repository() {

    }

    Repository(com.google.appengine.api.datastore.Entity e) {
        this.name = e.getKey().getName();
        this.lastModifiedAt = (Date) e.getProperty("lastModifiedAt");
        this.blobFile = NWUtils.getString(e, "blobFile");

        if (this.lastModifiedAt == null) {
            this.lastModifiedAt = NWUtils.newDate();
        }
    }

    com.google.appengine.api.datastore.Entity createEntity() {
        this.lastModifiedAt = NWUtils.newDate();

        com.google.appengine.api.datastore.Entity e =
                new com.google.appengine.api.datastore.Entity("Repository",
                        this.name);

        e.setIndexedProperty("lastModifiedAt", this.lastModifiedAt);
        e.setIndexedProperty("blobFile", this.blobFile);

        return e;
    }

    /**
     * @return created Key for this object
     */
    public Key createKey() {
        return KeyFactory.createKey("Repository", name);
    }

    /**
     * @return copy of this object
     */
    public Repository copy() {
        Repository r = new Repository();
        r.name = this.name;
        r.lastModifiedAt = this.lastModifiedAt;
        r.blobFile = this.blobFile;

        return r;
    }
}
