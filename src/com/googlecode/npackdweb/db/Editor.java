package com.googlecode.npackdweb.db;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.PrePersist;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Entity;

/**
 * A package.
 */
@Entity
@Cached
public class Editor {
    @Id
    /* User.getEmail() */
    public String name = "";

    /** last modification date */
    public Date lastModifiedAt = new Date();

    /** creation time */
    public Date createdAt = new Date();

    /** this package was created by this user */
    public User createdBy;

    /**
     * For Objectify.
     */
    public Editor() {
    }

    /**
     * @param user
     *            editor
     */
    public Editor(User user) {
        createdBy = UserServiceFactory.getUserService().getCurrentUser();
        if (createdBy == null)
            createdBy = new User("tim.lebedkov@gmail.com", "gmail.com");
        this.name = user.getEmail();
    }

    public String getName() {
        return name;
    }

    @PrePersist
    void onPersist() {
        NWUtils.incDataVersion();
        this.lastModifiedAt = new Date();
    }

    /**
     * @return created Key for this object
     */
    public Key<Editor> createKey() {
        return new Key<Editor>(Editor.class, this.name);
    }
}
