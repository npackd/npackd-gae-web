package com.googlecode.npackdweb.db;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Entity;
import java.util.Date;
import java.util.List;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

/**
 * A repository.
 */
@Entity
@Cached
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

    @PostLoad
    public void postLoad() {
        if (this.lastModifiedAt == null) {
            this.lastModifiedAt = NWUtils.newDate();
        }
    }

    @PrePersist
    void onPersist() {
        NWUtils.incDataVersion();
        this.lastModifiedAt = NWUtils.newDate();
    }

    /**
     * @return created Key for this object
     */
    public Key<Repository> createKey() {
        return new Key<Repository>(Repository.class, name);
    }

    /**
     * Searches for the repository with the given tag.
     *
     * @param ofy Objectify
     * @param tag tag name
     * @return found repository or null
     */
    public static Repository findByTag(Objectify ofy, String tag) {
        return ofy.find(new Key<Repository>(Repository.class, tag));
    }

    /**
     * @param ofy Objectify instance
     * @return all defined repositories
     */
    public static List<Repository> findAll(Objectify ofy) {
        return ofy.query(Repository.class).list();
    }
}
