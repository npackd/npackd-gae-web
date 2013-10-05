package com.googlecode.npackdweb.db;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.search.Document.Builder;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.Version;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Entity;

/**
 * A package.
 */
@Entity
@Cached
public class Package {
    /**
     * Searches for a package with the given full package ID.
     * 
     * @param id
     *            full package ID
     * @return found package or null
     */
    public static Package findByName(Objectify ofy, String id) {
        return ofy.find(new Key<Package>(Package.class, id));
    }

    @Id
    /* internal name of the package like com.example.Test */
    public String name = "";

    public String title = "";
    public String url = "";
    public String description = "";
    public String icon = "";
    public String license = "";
    public String comment = "";

    /** last modification date */
    public Date lastModifiedAt = new Date();

    /** creation time */
    public Date createdAt = new Date();

    /**
     * URL of the HTML/plain text page where the current version number is
     * present
     */
    public String discoveryPage = "";

    /** regular expression to discover the version number */
    public String discoveryRE = "";

    /**
     * Pattern for the package binary download URL Available variables:
     * ${{version}} ${{version2Parts}} ${{version3Parts}}
     * ${{version2PartsWithoutDots}} ${{actualVersion}}
     * ${{actualVersionWithoutDots}} ${{actualVersionWithUnderscores}}
     * ${{match}}
     */
    public String discoveryURLPattern = "";

    public List<String> tags = new ArrayList<String>();

    /** this package was created by this user */
    public User createdBy;

    /** list of users allowed to edit this package and package versions */
    public List<User> permissions = new ArrayList<User>();

    /** last check performed which found no updates */
    public Date noUpdatesCheck;

    /**
     * For Objectify.
     */
    public Package() {
    }

    /**
     * @param name
     *            full internal name of the package
     */
    public Package(String name) {
        createdBy = UserServiceFactory.getUserService().getCurrentUser();
        if (createdBy == null)
            createdBy = new User("tim.lebedkov@gmail.com", "gmail.com");
        this.name = name;
        this.permissions.add(createdBy);
    }

    /**
     * @return true if the current user may modify the package
     */
    public boolean isCurrentUserPermittedToModify() {
        UserService us = UserServiceFactory.getUserService();
        User u = us.getCurrentUser();
        boolean r = false;
        if (u != null) {
            if (us.isUserAdmin()) {
                r = true;
            } else {
                for (int i = 0; i < this.permissions.size(); i++) {
                    User cu = this.permissions.get(i);
                    if (cu.getEmail().equals(u.getEmail())) {
                        r = true;
                        break;
                    }
                }
            }
        }
        return r;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public String getLicense() {
        return license;
    }

    public String getComment() {
        return comment;
    }

    @PostLoad
    public void postLoad() {
        if (this.comment == null)
            this.comment = "";
        if (this.lastModifiedAt == null)
            this.lastModifiedAt = new Date();
        if (this.createdAt == null)
            this.createdAt = new Date(1355048474); // December 9, 2012, 11:21:14
        if (this.discoveryPage == null)
            this.discoveryPage = "";
        if (this.discoveryRE == null)
            this.discoveryRE = "";
        if (this.discoveryURLPattern == null)
            this.discoveryURLPattern = "";
        if (this.createdBy == null)
            this.createdBy = new User("tim.lebedkov@gmail.com", "gmail.com");
        if (this.tags == null)
            this.tags = new ArrayList<String>();
        if (permissions == null) {
            this.permissions = new ArrayList<User>();
        }
        if (permissions.size() == 0)
            this.permissions.add(this.createdBy);
    }

    @PrePersist
    void onPersist() {
        NWUtils.incDataVersion();
        this.lastModifiedAt = new Date();
    }

    /**
     * <package>
     * 
     * @param d
     *            XML document
     * @return <package>
     */
    public Element toXML(Document d) {
        Package p = this;
        Element package_ = d.createElement("package");
        package_.setAttribute("name", p.name);
        if (!p.title.isEmpty())
            NWUtils.e(package_, "title", p.title);
        if (!p.url.isEmpty())
            NWUtils.e(package_, "url", p.url);
        if (!p.description.isEmpty())
            NWUtils.e(package_, "description", p.description);
        if (!p.icon.isEmpty())
            NWUtils.e(package_, "icon", p.icon);
        if (!p.license.isEmpty())
            NWUtils.e(package_, "license", p.license);
        if (tags.size() > 0)
            NWUtils.e(package_, "category", NWUtils.join("|", tags));

        return package_;
    }

    /**
     * @return created Key for this object
     */
    public Key<Package> createKey() {
        return new Key<Package>(Package.class, this.name);
    }

    /**
     * @return document for the search index
     */
    public com.google.appengine.api.search.Document createDocument() {
        Builder b = com.google.appengine.api.search.Document.newBuilder();
        b.setId(this.name)
                .addField(
                        Field.newBuilder().setName("title").setText(this.title))
                .addField(
                        Field.newBuilder().setName("description")
                                .setText(this.description))
                .addField(
                        Field.newBuilder().setName("createdAt")
                                .setDate(this.createdAt))
                .addField(Field.newBuilder().setName("name").setText(this.name))
                .addField(
                        Field.newBuilder().setName("category")
                                .setText(NWUtils.join(" ", tags)));
        com.google.appengine.api.search.Document d = b.build();
        return d;
    }

    /**
     * Checks a package name
     * 
     * @param n
     *            full package name
     * @return error message or null
     */
    public static String checkName(String n) {
        if (n.length() == 0) {
            return "Empty package name";
        } else {
            int pos = n.indexOf("..");
            if (pos >= 0)
                return MessageFormat.format(
                        "Empty segment at position {0} in {1}", pos + 1, n);

            pos = n.indexOf("--");
            if (pos >= 0)
                return MessageFormat.format("-- at position {0} in {1}",
                        pos + 1, n);

            String[] parts = n.split("\\.", -1);
            for (int j = 0; j < parts.length; j++) {
                String part = parts[j];

                if (!part.isEmpty()) {
                    char c = part.charAt(0);
                    if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z')
                            || (c == '_') || (c >= 'a' && c <= 'z') || Character
                                .isLetter(c)))
                        return MessageFormat.format(
                                "Wrong character at position 1 in {0}", part);
                }

                for (int i = 1; i < part.length() - 1; i++) {
                    char c = part.charAt(i);
                    if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z')
                            || (c == '_') || (c == '-')
                            || (c >= 'a' && c <= 'z') || Character.isLetter(c)))
                        return MessageFormat.format(
                                "Wrong character at position {0} in {1}",
                                i + 1, part);
                }

                if (!part.isEmpty()) {
                    char c = part.charAt(part.length() - 1);
                    if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z')
                            || (c == '_') || (c >= 'a' && c <= 'z') || Character
                                .isLetter(c)))
                        return MessageFormat.format(
                                "Wrong character at position {0} in {1}",
                                part.length(), part);
                }
            }
        }

        return null;
    }

    /**
     * Determines the newest available version of the package by downloading a
     * package and scanning it for a version number.
     * 
     * @return found version number
     * @throws IOException
     *             if something goes wrong
     */
    public Version findNewestVersion() throws IOException {
        if (discoveryPage == null || discoveryPage.trim().length() == 0)
            throw new IOException("No discovery page is defined");

        if (discoveryRE == null || discoveryRE.trim().length() == 0)
            throw new IOException("No discovery regular expression is defined");

        String version = null;

        URLFetchService s = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse r;
        try {
            r = s.fetch(new URL(discoveryPage));
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(r.getContent()), "UTF-8"));
            String line;
            Pattern vp = Pattern.compile(discoveryRE);
            while ((line = br.readLine()) != null) {
                Matcher vm = vp.matcher(line);
                if (vm.find()) {
                    version = vm.group(1);
                    break;
                }
            }
        } catch (MalformedURLException e) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        } catch (IOException e) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }

        if (version == null)
            throw new IOException(
                    "Error detecting new version: the version number pattern was not found.");

        version = version.replace('-', '.');

        // process version numbers like 2.0.6b
        if (version.length() > 0) {
            char c = Character
                    .toLowerCase(version.charAt(version.length() - 1));
            if (c >= 'a' && c <= 'z') {
                version = version.substring(0, version.length() - 1) + "."
                        + (c - 'a' + 1);
            }
        }

        Version v = null;
        try {
            v = Version.parse(version);
        } catch (NumberFormatException e) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
        v.normalize();

        return v;
    }
}
