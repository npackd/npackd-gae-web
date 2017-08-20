package com.googlecode.npackdweb.db;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Entity;
import java.util.Date;
import javax.persistence.Id;
import javax.persistence.PrePersist;

/**
 * A package.
 */
@Entity
@Cached
public class Editor {

    @Id
    /* User.getEmail() */
    public String name = "";

    /**
     * last modification date
     */
    public Date lastModifiedAt = NWUtils.newDate();

    /**
     * creation time
     */
    public Date createdAt = NWUtils.newDate();

    /**
     * this package was created by this user
     */
    public User createdBy;

    /**
     * ID > 0
     */
    public int id;

    /**
     * For Objectify.
     */
    public Editor() {
    }

    /**
     * @param user editor
     */
    public Editor(User user) {
        createdBy = UserServiceFactory.getUserService().getCurrentUser();
        if (createdBy == null) {
            createdBy = new User(NWUtils.THE_EMAIL, "gmail.com");
        }
        this.name = user.getEmail();
    }

    public String getName() {
        return name;
    }

    @PrePersist
    void onPersist() {
        NWUtils.incDataVersion();
        this.lastModifiedAt = NWUtils.newDate();
    }

    /**
     * @return created Key for this object
     */
    public Key<Editor> createKey() {
        return new Key<Editor>(Editor.class, this.name);
    }

    /**
     * Creates an ID for this Editor.
     */
    public void createId() {
        ShardedCounter sc = ShardedCounter.getOrCreateCounter("EditorID", 20);
        sc.increment();
        this.id = sc.getCount();
    }
}
