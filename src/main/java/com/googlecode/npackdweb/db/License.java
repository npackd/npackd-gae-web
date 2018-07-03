package com.googlecode.npackdweb.db;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.googlecode.npackdweb.NWUtils;
import java.util.Date;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A license definition.
 */
public class License {

    /**
     * This is the ID of the entity.
     */
    public String name;

    public String title;
    public String url;

    /**
     * last modification date
     */
    public Date lastModifiedAt;

    public License() {

    }

    /**
     * Creates a license from a Datastore entity.
     *
     * @param e an entity
     */
    public License(com.google.appengine.api.datastore.Entity e) {
        this.name = e.getKey().getName();
        this.title = NWUtils.getString(e, "title");
        this.url = NWUtils.getString(e, "url");

        if (this.lastModifiedAt == null) {
            this.lastModifiedAt = NWUtils.newDate();
        }
    }

    com.google.appengine.api.datastore.Entity createEntity() {
        this.lastModifiedAt = NWUtils.newDate();

        com.google.appengine.api.datastore.Entity e =
                new com.google.appengine.api.datastore.Entity("License",
                        this.name);

        e.setIndexedProperty("title", this.title);
        e.setIndexedProperty("url", this.url);

        return e;
    }

    // WARNING: update copy()!
    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    /**
     * @return created Key for this object
     */
    public Key createKey() {
        return KeyFactory.createKey("License", name);
    }

    /**
     * @param d XML document
     * @return XML representation of this license
     */
    public Element toXML(Document d) {
        Element license = d.createElement("license");
        license.setAttribute("name", name);
        if (!title.isEmpty()) {
            NWUtils.e(license, "title", title);
        }
        if (!url.isEmpty()) {
            NWUtils.e(license, "url", url);
        }
        return license;
    }

    /**
     * @return true if the current user allowed to modify the license
     */
    public boolean isCurrentUserPermittedToModify() {
        return NWUtils.isAdminLoggedIn();
    }

    /**
     * @return copy of this object
     */
    public License copy() {
        License r = new License();
        r.name = this.name;
        r.title = this.title;
        r.url = this.url;
        r.lastModifiedAt = this.lastModifiedAt;
        return r;
    }
}
