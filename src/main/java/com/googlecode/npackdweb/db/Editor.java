package com.googlecode.npackdweb.db;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.NWUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An editor/user.
 */
public class Editor implements Cloneable {

    /* User.getEmail(). This is the ID of the Datastore entity. */
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
    public long id;

    /**
     * Last time this user was seen using the application or null.
     */
    public Date lastLogin;

    /**
     * -
     */
    public Editor() {
    }

    Editor(com.google.appengine.api.datastore.Entity p) {
        this.name = p.getKey().getName();
        this.lastModifiedAt = (Date) p.getProperty("lastModifiedAt");
        this.createdAt = (Date) p.getProperty("createdAt");
        this.createdBy = (User) p.getProperty("createdBy");
        this.starredPackages = NWUtils.getStringList(p, "starredPackages");
        this.id = (Long) p.getProperty("id");
        this.lastLogin = (Date) p.getProperty("lastLogin");

        if (this.starredPackages == null) {
            this.starredPackages = new ArrayList<>();
        }

        if (this.lastLogin == null) {
            this.lastLogin = NWUtils.newDay();
        }
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

    com.google.appengine.api.datastore.Entity createEntity() {
        this.lastModifiedAt = NWUtils.newDate();

        com.google.appengine.api.datastore.Entity e =
                new com.google.appengine.api.datastore.Entity("Editor",
                        this.name);

        e.setIndexedProperty("lastModifiedAt", this.lastModifiedAt);
        e.setIndexedProperty("createdAt", this.createdAt);
        e.setIndexedProperty("createdBy", this.createdBy);
        e.setIndexedProperty("starredPackages", this.starredPackages);
        e.setIndexedProperty("id", new Long(this.id));
        e.setIndexedProperty("lastLogin", this.lastLogin);

        return e;
    }

    public String getName() {
        return name;
    }

    /**
     * @return created Key for this object
     */
    public Key createKey() {
        return KeyFactory.createKey("Editor", this.name);
    }

    /**
     * Creates an ID for this Editor.
     */
    public void createId() {
        ShardedCounter sc = new ShardedCounter("EditorID");
        sc.increment();
        this.id = sc.getCount();
    }

    @Override
    protected Editor clone() {
        Editor r;
        try {
            r = (Editor) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new InternalError(ex);
        }
        r.starredPackages = new ArrayList<>();
        r.starredPackages.addAll(this.starredPackages);
        return r;
    }
}
