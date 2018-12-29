package com.googlecode.npackdweb.db;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.search.Document.Builder;
import com.google.appengine.api.search.Facet;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.NWUtils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A package.
 */
public class Package {

    /**
     * Default tags.
     *
     * WARNING: also update PackageDetail.js and TAG_TOOLTIPS
     */
    public static final String[] TAGS = {"Communications", "Development",
        "Education", "Finance", "Games", "Music", "News", "Photo",
        "Productivity", "Security", "Text", "Tools", "Video",
        "same-url", "end-of-life"};

    /**
     * Help for the tags.
     *
     * WARNING: also update PackageDetail.js and TAGS
     */
    public static final String[] TAG_TOOLTIPS = {
        "tools for communication", "software development tools",
        "educational programs", "finance related programs", "games",
        "music related software", "news", "image processing",
        "productivity", "security related software",
        "text related software (text editors, etc.)",
        "other tools", "video",
        "different versions are distributed from the same address. " +
        "The download is always up-to-date. New versions will " +
        "automatically replace the newest available.",
        "the development was stopped. There will be no new versions of this software."
    };

    /**
     * internal name of the package like com.example.Test. This is the ID of the
     * entity
     */
    public String name = "";

    /**
     * package architecture != null
     */
    public Arch arch;

    public String title = "";
    public String url = "";

    /**
     * change log URL or null
     */
    public String changelog;

    /**
     * This is stored unindexed
     */
    public String description = "";

    public String icon = "";
    public String license = "";

    /**
     * This is stored unindexed
     */
    public String comment = "";

    /**
     * last modification date
     */
    public Date lastModifiedAt = NWUtils.newDate();

    /**
     * user for the last modification
     */
    public User lastModifiedBy;

    /**
     * creation time
     */
    public Date createdAt = NWUtils.newDate();

    /**
     * URL of the HTML/plain text page where the current version number is
     * present
     */
    public String discoveryPage = "";

    /**
     * regular expression to discover the version number
     */
    public String discoveryRE = "";

    /**
     * Pattern for the package binary download URL Available variables:
     * ${{version}} ${{version2Parts}} ${{version3Parts}}
     * ${{version2PartsWithoutDots}} ${{actualVersion}}
     * ${{actualVersionWithoutDots}} ${{actualVersionWithUnderscores}}
     * ${{match}}
     */
    public String discoveryURLPattern = "";

    /**
     * categories. Example: "Entertainment/Travel"
     */
    public List<String> tags = new ArrayList<>();

    /**
     * this package was created by this user
     */
    public User createdBy;

    /**
     * list of users allowed to edit this package and package versions
     */
    public List<User> permissions = new ArrayList<>();

    /**
     * list of screenshot URLs
     */
    public List<String> screenshots = new ArrayList<>();

    /**
     * last check performed which found no updates
     */
    public Date noUpdatesCheck;

    /**
     * How many users starred this package?
     */
    public int starred;

    // PLEASE ALSO UPDATE #copy() and #Package(Entity)
    /**
     * @param name full internal name of the package
     */
    public Package(String name) {
        this.arch = Arch.ANY;
        final UserService us = UserServiceFactory.getUserService();
        if (us.isUserLoggedIn()) {
            this.lastModifiedBy = us.getCurrentUser();
        } else {
            this.lastModifiedBy =
                    new User(NWUtils.THE_EMAIL, "gmail.com");
        }

        createdBy = lastModifiedBy;
        this.name = name;
        this.permissions.add(createdBy);
    }

    /**
     * Creates an object from a Datastore entity.
     *
     * @param p an entity
     */
    public Package(com.google.appengine.api.datastore.Entity p) {
        this.name = p.getKey().getName();
        this.arch = Arch.fromDatastore(NWUtils.getLong(p, "arch"));
        this.title = NWUtils.getString(p, "title");
        this.url = NWUtils.getString(p, "url");
        this.changelog = NWUtils.getString(p, "changelog");
        this.description = NWUtils.getString(p, "description");
        this.icon = NWUtils.getString(p, "icon");
        this.license = NWUtils.getString(p, "license");
        this.comment = NWUtils.getString(p, "comment");
        this.lastModifiedAt = (Date) p.getProperty("lastModifiedAt");
        this.lastModifiedBy = (User) p.getProperty("lastModifiedBy");
        this.createdAt = (Date) p.getProperty("createdAt");
        this.discoveryPage = NWUtils.getString(p, "discoveryPage");
        this.discoveryRE = NWUtils.getString(p, "discoveryRE");
        this.discoveryURLPattern = NWUtils.getString(p, "discoveryURLPattern");
        this.tags = NWUtils.getStringList(p, "tags");
        this.createdBy = (User) p.getProperty("createdBy");
        this.permissions = NWUtils.getUserList(p, "permissions");
        this.screenshots = NWUtils.getStringList(p, "screenshots");
        this.noUpdatesCheck = (Date) p.getProperty("noUpdatesCheck");

        Long s = (Long) p.getProperty("starred");
        if (s != null) {
            this.starred = s.intValue();
        }

        if (this.comment == null) {
            this.comment = "";
        }
        if (this.lastModifiedAt == null) {
            this.lastModifiedAt = NWUtils.newDate();
        }
        if (this.createdAt == null) {
            this.createdAt = new Date(1355048474); // December 9, 2012, 11:21:14
        }
        if (this.discoveryPage == null) {
            this.discoveryPage = "";
        }
        if (this.discoveryRE == null) {
            this.discoveryRE = "";
        }
        if (this.discoveryURLPattern == null) {
            this.discoveryURLPattern = "";
        }
        if (this.createdBy == null) {
            this.createdBy = new User(NWUtils.THE_EMAIL, "gmail.com");
        }
        if (lastModifiedBy == null) {
            this.lastModifiedBy = this.createdBy;
        }
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        if (permissions == null) {
            this.permissions = new ArrayList<>();
        }
        if (permissions.size() == 0) {
            this.permissions.add(this.createdBy);
        }
        if (this.screenshots == null) {
            this.screenshots = new ArrayList<>();
        }
    }

    com.google.appengine.api.datastore.Entity createEntity() {
        // onPersist();

        com.google.appengine.api.datastore.Entity e =
                new com.google.appengine.api.datastore.Entity("Package",
                        this.name);

        e.setIndexedProperty("title", this.title);
        e.setIndexedProperty("arch", this.arch.toDatastore());
        e.setIndexedProperty("url", this.url);
        e.setIndexedProperty("changelog", this.changelog);
        e.setUnindexedProperty("description", this.description == null ? null :
                new Text(this.description));
        e.setIndexedProperty("icon", this.icon);
        e.setIndexedProperty("license", this.license);
        e.setUnindexedProperty("comment", this.comment == null ? null :
                new Text(this.comment));
        e.setIndexedProperty("lastModifiedAt", this.lastModifiedAt);
        e.setIndexedProperty("lastModifiedBy", this.lastModifiedBy);
        e.setIndexedProperty("createdAt", this.createdAt);
        e.setIndexedProperty("discoveryPage", this.discoveryPage);
        e.setIndexedProperty("discoveryRE", this.discoveryRE);
        e.setIndexedProperty("discoveryURLPattern", this.discoveryURLPattern);
        e.setIndexedProperty("tags", this.tags);
        e.setIndexedProperty("createdBy", this.createdBy);
        e.setIndexedProperty("permissions", this.permissions);
        e.setIndexedProperty("screenshots", this.screenshots);
        e.setIndexedProperty("noUpdatesCheck", this.noUpdatesCheck);
        e.setIndexedProperty("starred", new Long(this.starred));

        return e;
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
                r = isUserPermittedToModify(u);
            }
        }
        return r;
    }

    /**
     * Checks whether the supplied user is permitted to modify this package.
     * WARNING: special administrator rights are not considered
     *
     * @param u a user
     * @return true if the user may modify the package
     */
    public boolean isUserPermittedToModify(User u) {
        boolean r = false;
        for (User cu : this.permissions) {
            if (NWUtils.isEqual(cu, u)) {
                r = true;
                break;
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

    /**
     * &lt;package&gt;
     *
     * @param d XML document
     * @return &lt;package&gt;
     */
    public Element toXML(Document d) {
        Package p = this;
        Element package_ = d.createElement("package");
        package_.setAttribute("name", p.name);
        if (!p.title.isEmpty()) {
            NWUtils.e(package_, "title", p.title);
        }
        if (p.arch != Arch.ANY) {
            NWUtils.e(package_, "arch", this.arch.toString());
        }
        if (!p.url.isEmpty()) {
            NWUtils.e(package_, "url", p.url);
        }
        if (!p.description.isEmpty()) {
            NWUtils.e(package_, "description", p.description);
        }
        if (!p.icon.isEmpty()) {
            NWUtils.e(package_, "icon", p.icon);
        }
        if (!p.license.isEmpty()) {
            NWUtils.e(package_, "license", p.license);
        }
        for (String tag : tags) {
            NWUtils.e(package_, "category", tag);
        }
        if (p.changelog != null && !p.changelog.trim().isEmpty()) {
            NWUtils.e(package_, "link", "rel", "changelog", "href",
                    p.changelog, "");
        }
        for (String s : screenshots) {
            NWUtils.e(package_, "link", "rel", "screenshot", "href", s, "");
        }

        return package_;
    }

    /**
     * @return created Key for this object
     */
    public Key createKey() {
        return KeyFactory.createKey("Package", this.name);
    }

    /**
     * @return document for the search index
     */
    public com.google.appengine.api.search.Document createDocument() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.permissions.size(); i++) {
            if (i != 0) {
                sb.append(' ');
            }
            sb.append(this.permissions.get(i).getEmail());
        }

        Builder b = com.google.appengine.api.search.Document.newBuilder();
        b.setId(this.name)
                .addField(
                        Field.newBuilder().setName("title").setText(this.title))
                .addField(
                        Field.newBuilder().setName("arch").setText(this.arch.
                                toString()))
                .addField(
                        Field.newBuilder().setName("description")
                                .setText(this.description))
                .addField(
                        Field.newBuilder().setName("createdAt")
                                .setDate(this.createdAt))
                .addField(Field.newBuilder().setName("name").setText(this.name))
                .addField(
                        Field.newBuilder().setName("category")
                                .setText(NWUtils.join(" ", tags)))
                .addField(
                        Field.newBuilder().setName("permission")
                                .setText(sb.toString()));

        String category0 = null, category1 = null;
        if (tags.size() > 0) {
            String c = tags.get(0);
            List<String> parts = NWUtils.split(c, '/');
            if (parts.size() > 0) {
                category0 = parts.get(0);
            }
            if (parts.size() > 1) {
                category1 = parts.get(1);
            }
        }
        b.addFacet(Facet.withAtom("category0", category0 != null ? category0 :
                "Uncategorized"));
        b.addFacet(Facet.withAtom("category1", category1 != null ? category1 :
                "Uncategorized"));

        com.google.appengine.api.search.Document d = b.build();
        return d;
    }

    /**
     * Checks a package name
     *
     * @param n full package name
     * @return error message or null
     */
    public static String checkName(String n) {
        if (n.length() == 0) {
            return "Empty package name";
        }

        int pos = n.indexOf("..");
        if (pos >= 0) {
            return MessageFormat.format(
                    "Empty segment at position {0} in {1}", pos + 1, n);
        }

        pos = n.indexOf("--");
        if (pos >= 0) {
            return MessageFormat.format("-- at position {0} in {1}",
                    pos + 1, n);
        }

        String[] parts = n.split("\\.", -1);
        for (int j = 0; j < parts.length; j++) {
            String part = parts[j];

            if (!part.isEmpty()) {
                char c = part.charAt(0);
                if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') ||
                        (c == '_') || (c >= 'a' && c <= 'z') || Character
                                .isLetter(c))) {
                    return MessageFormat.format(
                            "Wrong character at position 1 in {0}", part);
                }
            }

            for (int i = 1; i < part.length() - 1; i++) {
                char c = part.charAt(i);
                if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') ||
                        (c == '_') || (c == '-') || (c >= 'a' && c <= 'z') ||
                        Character
                                .isLetter(c))) {
                    return MessageFormat.format(
                            "Wrong character at position {0} in {1}",
                            i + 1, part);
                }
            }

            if (!part.isEmpty()) {
                char c = part.charAt(part.length() - 1);
                if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') ||
                        (c == '_') || (c >= 'a' && c <= 'z') || Character
                                .isLetter(c))) {
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
     * @throws IOException if something goes wrong
     */
    public Version findNewestVersion() throws IOException {
        if (discoveryPage == null || discoveryPage.trim().length() == 0) {
            throw new IOException("No discovery page is defined");
        }

        if (discoveryRE == null || discoveryRE.trim().length() == 0) {
            throw new IOException("No discovery regular expression is defined");
        }

        String version = null;

        URLFetchService s = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse r;
        try {
            r = s.fetch(new URL(discoveryPage));
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(
                            new ByteArrayInputStream(r.getContent()),
                            "UTF-8"));
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
            throw new IOException(e);
        } catch (IOException
                | com.google.appengine.api.urlfetch.ResponseTooLargeException e) {
            throw new IOException(e);
        }

        if (version == null) {
            throw new IOException(
                    "Error detecting new version: the version number pattern was not found.");
        }

        version = version.replace('-', '.');
        version = version.replace('_', '.');

        // process version numbers like 2.0.6b
        if (version.length() > 0) {
            char c =
                    Character.toLowerCase(version.charAt(version.length() - 1));
            if (c >= 'a' && c <= 'z') {
                version =
                        version.substring(0, version.length() - 1) + "." +
                        (c - 'a' + 1);
            }
        }

        Version v = null;
        try {
            v = Version.parse(version);
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
        v.normalize();

        return v;
    }

    /**
     * Creates an instance of Package from XML.
     *
     * @param e &lt;package&gt;
     * @return created object
     */
    public static Package parse(Element e) {
        Package p = new Package(e.getAttribute("name"));
        p.title = NWUtils.getSubTagContent(e, "title", "");
        p.arch = Arch.fromString(NWUtils.getSubTagContent(e, "arch", "any"));
        p.url = NWUtils.getSubTagContent(e, "url", "");
        p.description = NWUtils.getSubTagContent(e, "description", "");
        p.license = NWUtils.getSubTagContent(e, "license", "");
        String category = NWUtils.getSubTagContent(e, "category", null);
        if (category != null) {
            p.tags.add(category);
        }

        NodeList children = e.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node ch = children.item(i);
            if (ch.getNodeType() == Element.ELEMENT_NODE &&
                    ch.getNodeName().equals("link")) {
                Element link = (Element) ch;
                String rel = link.getAttribute("rel");
                String href = link.getAttribute("href");
                if (rel.equals("changelog") &&
                        (p.changelog == null || p.changelog.isEmpty())) {
                    p.changelog = href;
                } else if (rel.equals("screenshot")) {
                    p.screenshots.add(href);
                } else if (rel.equals("icon") && p.icon.isEmpty()) {
                    p.icon = href;
                }
            }
        }

        if (p.icon.isEmpty()) {
            p.icon = NWUtils.getSubTagContent(e, "icon", "");
        }

        return p;
    }

    /**
     * @return copy of this object
     */
    public Package copy() {
        Package p = new Package(this.name);
        p.title = this.title;
        p.arch = this.arch;
        p.url = this.url;
        p.changelog = this.changelog;
        p.description = this.description;
        p.icon = this.icon;
        p.license = this.license;
        p.comment = this.comment;
        p.lastModifiedAt = this.lastModifiedAt;
        p.lastModifiedBy = this.lastModifiedBy;
        p.createdAt = this.createdAt;
        p.discoveryPage = this.discoveryPage;
        p.discoveryRE = this.discoveryRE;
        p.discoveryURLPattern = this.discoveryURLPattern;
        p.tags.clear();
        p.tags.addAll(this.tags);
        p.createdBy = this.createdBy;
        p.permissions.clear();
        p.permissions.addAll(this.permissions);
        p.screenshots.clear();
        p.screenshots.addAll(this.screenshots);
        p.noUpdatesCheck = this.noUpdatesCheck;
        p.starred = this.starred;
        return p;
    }

    /**
     * Checks whether this package has the specified tag.
     *
     * @param tag tag name
     * @return true if this package has the specified tag
     */
    public boolean hasTag(String tag) {
        return this.tags != null && this.tags.indexOf(tag) >= 0;
    }

    /**
     * Adds a tag if it is not already available.
     *
     * @param tag tag
     */
    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    /**
     * Creates a new version of this package using the newest available version
     * as a template and saves it.
     *
     * @param version new version number
     * @param maxSize maximum size of the file or 0 for "unlimited". If the file
     * is bigger than the specified size, the download will be cancelled and an
     * IOException will be thrown
     * @return created package version or null if the creation is not possible
     */
    public PackageVersion createDetectedVersion(Version version, long maxSize) {
        List<PackageVersion> versions = NWUtils.dsCache.getSortedVersions(name);

        PackageVersion copy;
        PackageVersion pv = null;
        if (versions.size() > 0) {
            pv = versions.get(versions.size() - 1);
            copy = pv.copy();
            copy.name = copy.package_ + "@" + version.toString();
            copy.version = version.toString();
            copy.createdAt = NWUtils.newDate();
        } else {
            copy = new PackageVersion(this.name, version.toString());
        }

        if (!hasTag("same-url") &&
                this.discoveryURLPattern.trim().length() > 0) {
            Map<String, String> map = new HashMap<>();
            map.put("${version}", version.toString());
            map.put("${v0}", Integer.toString(version.getPart(0)));
            map.put("${v1}", Integer.toString(version.getPart(1)));
            map.put("${v2}", Integer.toString(version.getPart(2)));
            map.put("${v3}", Integer.toString(version.getPart(3)));
            map.put("${v4}", Integer.toString(version.getPart(4)));
            map.put("${v5}", Integer.toString(version.getPart(5)));
            /*map.put("${{match}}", v.toString());*/
            copy.url = NWUtils.tmplString(this.discoveryURLPattern, map);
            if (!copy.sha1.isEmpty()) {
                try {
                    final NWUtils.Info info =
                            NWUtils.download(copy.url, "SHA-256",
                                    maxSize);
                    copy.sha1 = NWUtils.byteArrayToHexString(info.sha1);
                } catch (IOException | NoSuchAlgorithmException ex) {
                    NWUtils.LOG.
                            log(Level.SEVERE, null, ex);
                }
            }
        }

        if (hasTag("same-url") && pv != null) {
            NWUtils.dsCache.deletePackageVersion(pv);

            // the next call to savePackageVersion will do this
            // NWUtils.incDataVersion();
        } else {
            copy.addTag("untested");
        }

        boolean changeNotReviewed = true;
        if (hasTag("same-url")) {
            changeNotReviewed = false;
        } else {
            // full trust to THE_EMAIL
            User theUser = new User(NWUtils.THE_EMAIL, "gmail.com");
            if (pv != null && NWUtils.isEqual(pv.lastModifiedBy, theUser) &&
                    NWUtils.isEqual(this.lastModifiedBy, theUser)) {
                changeNotReviewed = false;
            }
        }

        NWUtils.dsCache.savePackageVersion(null, copy, true, changeNotReviewed);

        return copy;
    }
}
