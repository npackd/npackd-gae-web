package com.googlecode.npackdweb.db;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.OnSave;
import java.util.Date;

/**
 * A repository.
 */
@Entity
@Cache
@Index
public class Repository {

    /**
     * name of the repository
     */
    @Id
    public String name;

    /**
     * last modification date
     */
    public Date lastModifiedAt;

    /**
     * path to the XML blob or null
     */
    public String blobFile;

    @OnLoad
    public void postLoad() {
        if (this.lastModifiedAt == null) {
            this.lastModifiedAt = NWUtils.newDate();
        }
    }

    @OnSave
    void onPersist() {
        NWUtils.dsCache.incDataVersion();
        this.lastModifiedAt = NWUtils.newDate();
    }

    /**
     * @return created Key for this object
     */
    public Key<Repository> createKey() {
        return Key.create(Repository.class, name);
    }
}
