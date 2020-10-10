package com.googlecode.npackdweb.db;

import com.googlecode.npackdweb.AuthService;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    Editor(ResultSet p) throws SQLException {
        this.name = p.getString("name");
        this.lastModifiedAt = p.getDate("lastModifiedAt");
        this.createdAt = p.getDate("createdAt");
        this.createdBy = new User(p.getString("createdBy"), "server");
        this.starredPackages = NWUtils.getStringList(p, "starredPackages");
        this.id = p.getLong("id");
        this.lastLogin = p.getDate("lastLogin");

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
        createdBy = AuthService.getInstance().getCurrentUser();
        if (createdBy == null) {
            createdBy = new User(NWUtils.THE_EMAIL, "gmail.com");
        }
        this.name = user.email;
    }

    void createEntity(PreparedStatement ps) throws SQLException {
        this.lastModifiedAt = NWUtils.newDate();

        ps.setString(0, name);
        ps.setDate(1, new java.sql.Date(this.lastModifiedAt.getTime()));
        ps.setDate(2, new java.sql.Date(this.createdAt.getTime()));
        ps.setString(3, this.createdBy.email);
        // TODO ps.setInt(4, this.starredPackages);
        ps.setLong(4, this.id);
        ps.setDate(5, new java.sql.Date(this.lastLogin.getTime()));
    }

    public String getName() {
        return name;
    }

    /**
     * Creates an ID for this Editor.
     */
    public void createId() {
        this.id = NWUtils.dsCache.getNextEditorID();
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
