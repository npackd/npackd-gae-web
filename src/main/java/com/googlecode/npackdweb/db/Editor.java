package com.googlecode.npackdweb.db;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.OnSave;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An editor/user.
 */
@Entity
@Cache
@Index
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
     * this record was created by this user
     */
    public User createdBy;

    /**
     * list of package IDs starred by this user
     */
    public List<String> starredPackages = new ArrayList<>();

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

    @OnSave
    void onPersist() {
        NWUtils.dsCache.incDataVersion();
        this.lastModifiedAt = NWUtils.newDate();
    }

    /**
     * @return created Key for this object
     */
    public Key<Editor> createKey() {
        return Key.create(Editor.class, this.name);
    }

    /**
     * Creates an ID for this Editor.
     */
    public void createId() {
        ShardedCounter sc = ShardedCounter.getOrCreateCounter("EditorID", 20);
        sc.increment();
        this.id = sc.getCount();
    }

    @OnLoad
    public void postLoad() {
        if (this.starredPackages == null) {
            this.starredPackages = new ArrayList<>();
        }
    }
}
