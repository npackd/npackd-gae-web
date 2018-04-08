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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A license definition.
 */
@Entity
@Cache
@Index
public class License {

    @Id
    public String name;
    public String title;
    public String url;

    /**
     * last modification date
     */
    public Date lastModifiedAt;

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
    public Key<License> createKey() {
        return Key.create(License.class, name);
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
