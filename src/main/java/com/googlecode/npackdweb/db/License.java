package com.googlecode.npackdweb.db;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Entity;
import java.util.Date;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A license definition.
 */
@Entity
@Cached
public class License {

    /**
     * Searches for a license with the given full license ID.
     *
     * @param ofy Objectify instance
     * @param id full license ID
     * @return found license or null
     */
    public static License findByName(Objectify ofy, String id) {
        return ofy.find(new Key<>(License.class, id));
    }

    @Id
    public String name;
    public String title;
    public String url;

    /**
     * last modification date
     */
    public Date lastModifiedAt;

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

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
    public Key<License> createKey() {
        return new Key<>(License.class, name);
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
}
