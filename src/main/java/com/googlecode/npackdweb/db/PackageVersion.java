package com.googlecode.npackdweb.db;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * A package version.
 */
public class PackageVersion {

    /**
     * Default tags.
     * <p>
     * WARNING: update TAG_TOOLTIPS!
     */
    public static final String[] TAGS = {
            "unstable", "untested", "non-admin",
            "not-reviewed", "phishing", "malware", "unwanted"};

    /**
     * Help for the tags.
     */
    public static final String[] TAG_TOOLTIPS = {
            "this package version should be included in the default repository for unstable software",
            "the installation and removal of this package version was not yet tested. This package will not be included in the default repositories.",
            "can be installed without administrative privileges",
            "this package version is not yet reviewed and may be unsafe. Only an administrator can change this tag.",
            "Warning—Suspected phishing page. This page may be a forgery or imitation of another website, designed to trick users into sharing personal or financial information. Entering any personal information on this page may result in identity theft or other abuse. You can find out more about phishing from www.antiphishing.org.",
            "Warning—Visiting this web site may harm your computer. This page appears to contain malicious code that could be downloaded to your computer without your consent. You can learn more about harmful web content including viruses and other malicious code and how to protect your computer at StopBadware.org.",
            "Warning—The site ahead may contain harmful programs. Attackers might attempt to trick you into installing programs that harm your browsing experience (for example, by changing your homepage or showing extra ads on sites you visit). You can learn more about unwanted software at https://www.google.com/about/company/unwanted-software-policy.html.",};

    /**
     * TAG -&gt; TOOLTIP
     */
    public static final Map<String, String> TAG_2_TOOLTIP = new HashMap<>();

    static {
        for (int i = 0; i < TAGS.length; i++) {
            TAG_2_TOOLTIP.put(TAGS[i], TAG_TOOLTIPS[i]);
        }
    }

    /**
     * this is unindexed
     */
    private List<Object> fileContents = new ArrayList<>();

    /**
     * abc@2.4. This is the ID of the entity.
     */
    public String name = "";

    /**
     * old versions used the name of the property "package"
     */
    public String package_ = "";

    /**
     * only the version number
     */
    public String version = "";

    public boolean oneFile;
    public String url = "";

    /**
     * SHA-1 or SHA-256 or ""
     */
    public String sha1 = "";

    public List<String> importantFileTitles = new ArrayList<>();
    public List<String> importantFilePaths = new ArrayList<>();

    /**
     * <cmd-file>
     */
    public List<String> cmdFilePaths = new ArrayList<>();

    public List<String> filePaths = new ArrayList<>();
    public List<String> dependencyPackages = new ArrayList<>();
    public List<String> dependencyVersionRanges = new ArrayList<>();
    public List<String> dependencyEnvVars = new ArrayList<>();
    public List<String> detectFilePaths = new ArrayList<>();
    public List<String> detectFileSHA1s = new ArrayList<>();

    public List<String> tags;

    /**
     * last modification date
     */
    public Date lastModifiedAt = NWUtils.newDate();

    /**
     * user for the last modification
     */
    public User lastModifiedBy;

    /**
     * creation date
     */
    public Date createdAt;

    /**
     * user for the creation
     */
    public User createdBy;

    /**
     * number of installations that succeeded
     */
    public int installSucceeded;

    /**
     * number of installations that failed
     */
    public int installFailed;

    /**
     * number of un-installations that succeeded
     */
    public int uninstallSucceeded;

    /**
     * number of un-installations that failed
     */
    public int uninstallFailed;

    /**
     * -.
     */
    public PackageVersion() {
    }

    /**
     * tags = "not-reviewed"
     *
     * @param package_ full internal package name
     * @param version version number
     */
    public PackageVersion(String package_, String version) {
        this.name = package_ + "@" + version;
        this.package_ = package_;
        this.version = version;
        UserService us = UserServiceFactory.getUserService();
        if (us.isUserLoggedIn()) {
            this.lastModifiedBy = us.getCurrentUser();
        } else {
            this.lastModifiedBy =
                    new User(NWUtils.THE_EMAIL, "gmail.com");
        }
        this.createdBy = this.lastModifiedBy;
        tags = new ArrayList<>();
        tags.add("not-reviewed");
    }

    /**
     * Creates an object from a Datastore entity.
     *
     * @param e an entity
     */
    public PackageVersion(com.google.appengine.api.datastore.Entity e) {
        this.fileContents = (List<Object>) e.getProperty("fileContents");
        if (this.fileContents == null) {
            this.fileContents = new ArrayList<>();
        }
        this.name = e.getKey().getName();
        this.package_ = NWUtils.getString(e, "package_");
        if (this.package_ == null) {
            this.package_ = NWUtils.getString(e, "package");
        }
        this.version = NWUtils.getString(e, "version");
        this.oneFile = (Boolean) e.getProperty("oneFile");
        this.url = NWUtils.getString(e, "url");
        this.sha1 = NWUtils.getString(e, "sha1");
        this.importantFileTitles = NWUtils.getStringList(e,
                "importantFileTitles");
        this.importantFilePaths = NWUtils.getStringList(e,
                "importantFilePaths");
        this.cmdFilePaths = NWUtils.getStringList(e, "cmdFilePaths");
        this.filePaths = NWUtils.getStringList(e, "filePaths");
        this.dependencyPackages = NWUtils.getStringList(e,
                "dependencyPackages");
        this.dependencyVersionRanges = NWUtils.getStringList(e,
                "dependencyVersionRanges");
        this.dependencyEnvVars = NWUtils.getStringList(e,
                "dependencyEnvVars");
        this.detectFilePaths = NWUtils.getStringList(e, "detectFilePaths");
        this.detectFileSHA1s = NWUtils.getStringList(e, "detectFileSHA1s");
        this.tags = NWUtils.getStringList(e, "tags");
        this.lastModifiedAt = (Date) e.getProperty("lastModifiedAt");
        this.lastModifiedBy = (User) e.getProperty("lastModifiedBy");
        this.createdAt = (Date) e.getProperty("createdAt");
        this.createdBy = (User) e.getProperty("createdBy");
        this.installSucceeded = (int) NWUtils.getLong(e, "installSucceeded");
        this.installFailed = (int) NWUtils.getLong(e, "installFailed");
        this.uninstallSucceeded =
                (int) NWUtils.getLong(e, "uninstallSucceeded");
        this.uninstallFailed = (int) NWUtils.getLong(e, "uninstallFailed");

        if (this.sha1 == null) {
            this.sha1 = "";
        }

        // Bugfix: the content was stored as <String> which lead to the
        // conversion of long strings (> 500 characters) to Text and changing
        // their position in the list
        for (int i = 0; i < this.fileContents.size(); i++) {
            Object obj = this.fileContents.get(i);
            if (obj instanceof String) {
                this.fileContents.set(i, new Text((String) obj));
            }
        }

        while (this.fileContents.size() < this.filePaths.size()) {
            this.fileContents.add(new Text(""));
        }

        if (this.lastModifiedAt == null) {
            this.lastModifiedAt = NWUtils.newDate();
        }

        if (this.createdAt == null) {
            this.createdAt = this.lastModifiedAt;
        }

        while (this.dependencyEnvVars.size() < this.dependencyPackages.size()) {
            this.dependencyEnvVars.add("");
        }

        if (lastModifiedBy == null) {
            UserService us = UserServiceFactory.getUserService();
            if (us.isUserLoggedIn()) {
                this.lastModifiedBy = us.getCurrentUser();
            } else {
                this.lastModifiedBy =
                        new User(NWUtils.THE_EMAIL, "gmail.com");
            }
        }

        if (createdBy == null) {
            this.createdBy = this.lastModifiedBy;
        }

        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
    }

    /**
     * Creates an entity for saving in the Datastore.
     *
     * @return the created entity
     */
    public com.google.appengine.api.datastore.Entity createEntity() {
        com.google.appengine.api.datastore.Entity e =
                new com.google.appengine.api.datastore.Entity("PackageVersion",
                        this.name);

        e.setIndexedProperty("url", this.url);
        e.setProperty("fileContents", this.fileContents);
        e.setIndexedProperty("package_", this.package_);
        e.setIndexedProperty("version", this.version);
        e.setIndexedProperty("oneFile", this.oneFile);
        e.setIndexedProperty("url", this.url);
        e.setIndexedProperty("sha1", this.sha1);
        e.setIndexedProperty("importantFileTitles", this.importantFileTitles);
        e.setIndexedProperty("importantFilePaths", this.importantFilePaths);
        e.setIndexedProperty("cmdFilePaths", this.cmdFilePaths);
        e.setIndexedProperty("filePaths", this.filePaths);
        e.setIndexedProperty("dependencyPackages", this.dependencyPackages);
        e.setIndexedProperty("dependencyVersionRanges",
                this.dependencyVersionRanges);
        e.setIndexedProperty("dependencyEnvVars", this.dependencyEnvVars);
        e.setIndexedProperty("detectFilePaths", this.detectFilePaths);
        e.setIndexedProperty("detectFileSHA1s", this.detectFileSHA1s);
        e.setIndexedProperty("tags", this.tags);
        e.setIndexedProperty("lastModifiedAt", this.lastModifiedAt);
        e.setIndexedProperty("lastModifiedBy", this.lastModifiedBy);
        e.setIndexedProperty("createdAt", this.createdAt);
        e.setIndexedProperty("createdBy", this.createdBy);
        e.setIndexedProperty("installSucceeded", this.installSucceeded);
        e.setIndexedProperty("installFailed", this.installFailed);
        e.setIndexedProperty("uninstallSucceeded", this.uninstallSucceeded);
        e.setIndexedProperty("uninstallFailed", this.uninstallFailed);

        return e;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getPackage() {
        return package_;
    }

    public boolean getOneFile() {
        return oneFile;
    }

    public String getUrl() {
        return url;
    }

    public String getSha1() {
        return sha1;
    }

    public List<String> getDependencyPackages() {
        return dependencyPackages;
    }

    public List<String> getDependencyVersionRanges() {
        return dependencyVersionRanges;
    }

    /**
     * @return copy of this object
     */
    public PackageVersion copy() {
        PackageVersion c = new PackageVersion(this.package_, this.name);
        c.version = this.version;
        c.oneFile = this.oneFile;
        c.url = this.url;
        c.sha1 = this.sha1;
        c.importantFileTitles.addAll(this.importantFileTitles);
        c.importantFilePaths.addAll(this.importantFilePaths);
        c.cmdFilePaths.addAll(this.cmdFilePaths);
        c.filePaths.addAll(this.filePaths);
        c.fileContents.addAll(this.fileContents);
        c.dependencyPackages.addAll(this.dependencyPackages);
        c.dependencyVersionRanges.addAll(this.dependencyVersionRanges);
        c.dependencyEnvVars.addAll(this.dependencyEnvVars);
        c.detectFilePaths.addAll(this.detectFilePaths);
        c.detectFileSHA1s.addAll(this.detectFileSHA1s);
        c.tags.clear();
        c.tags.addAll(this.tags);
        c.lastModifiedBy = this.lastModifiedBy;
        c.createdAt = this.createdAt;
        c.createdBy = this.createdBy;
        return c;
    }

    /**
     * Creates &lt;version&gt;
     *
     * @param d XML document
     * @param extra export extra non-standard information
     * @return &lt;version&gt;
     */
    public void toXML(HTMLWriter d, boolean extra) {
        PackageVersion pv = this;

        d.start("version", "name", pv.version, "package", pv.package_,
                "type", pv.oneFile ? "one-file" : null);
        for (int i = 0; i < pv.importantFilePaths.size(); i++) {
            d.e("important-file", "path", pv.importantFilePaths.get(i),
                    "title", pv.importantFileTitles.get(i));
        }
        for (int i = 0; i < pv.cmdFilePaths.size(); i++) {
            d.e("cmd-file", "path", pv.cmdFilePaths.get(i));
        }
        for (int i = 0; i < pv.filePaths.size(); i++) {
            d.e("file", "path", pv.filePaths.get(i), pv.getFileContents(i));
        }
        if (!pv.url.isEmpty()) {
            d.e("url", pv.url);
        }

        String sha1 = pv.sha1.trim();
        if (!sha1.isEmpty()) {
            if (sha1.length() == 40) {
                d.e("sha1", sha1);
            } else if (sha1.length() == 64) {
                d.e("hash-sum", "type", "SHA-256", sha1);
            }
        }

        for (int i = 0; i < pv.dependencyPackages.size(); i++) {
            if (!pv.dependencyEnvVars.get(i).isEmpty()) {
                d.start("dependency", "package", pv.dependencyPackages.get(i),
                        "versions",
                        pv.dependencyVersionRanges.get(i));
                d.e("variable", pv.dependencyEnvVars.get(i));
                d.end("dependency");
            } else {
                d.e("dependency", "package", pv.dependencyPackages.get(i),
                        "versions",
                        pv.dependencyVersionRanges.get(i));
            }
        }
        for (int i = 0; i < pv.detectFilePaths.size(); i++) {
            d.start("detect-file");
            d.e("path", pv.detectFilePaths.get(i));
            d.e("sha1", pv.detectFileSHA1s.get(i));
            d.end("detect-file");
        }

        if (extra) {
            for (String tag : tags) {
                d.e("_tag", tag);
            }
        }

        d.e("_last-modified-at", DateTimeFormatter.ISO_INSTANT.format(
                lastModifiedAt.toInstant()));
        d.e("_last-modified-by", lastModifiedBy.getEmail());
        d.e("_created-at", DateTimeFormatter.ISO_INSTANT.format(
                createdAt.toInstant()));
        d.e("_created-by", createdBy.getEmail());
        d.e("_install-succeeded", Integer.toString(installSucceeded));
        d.e("_install-failed", Integer.toString(installFailed));
        d.e("_uninstall-succeeded",
                Integer.toString(uninstallSucceeded));
        d.e("_uninstall-failed", Integer.toString(uninstallFailed));

        d.end("version");
    }

    /**
     * Parses XML. This method does not clear the data so that it should be only
     * called after a new object is created.
     *
     * @param e "version"
     * @throws NumberFormatException if something is wrong in the XML
     */
    public void parseXML(Element e) throws NumberFormatException {
        final String packageName = e.getAttribute("package");
        String err = Package.checkName(packageName);
        if (err != null) {
            throw new NumberFormatException(err);
        }

        final String version = e.getAttribute("name");
        Version.parse(version);

        this.package_ = packageName;
        this.version = version;

        this.name = this.package_ + "@" + this.version;
        this.oneFile = e.getAttribute("type").equals("one-file");

        this.url = NWUtils.getSubTagContent(e, "url", "");
        err = NWUtils.validateURL(this.url, false);
        if (err != null) {
            throw new NumberFormatException(err);
        }

        String parsedSHA1 = "";
        String parsedSHA256 = "";
        NodeList children = e.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node ch = children.item(i);
            if (ch.getNodeType() == Element.ELEMENT_NODE) {
                Element che = (Element) ch;
                if (che.getNodeName().equals("important-file")) {
                    final String path = che.getAttribute("path");
                    err = NWUtils.validateRelativePath(path);
                    if (err != null) {
                        throw new NumberFormatException(err);
                    }
                    this.importantFilePaths.add(path);

                    final String title = che.getAttribute("title").trim();
                    if (title.isEmpty()) {
                        throw new NumberFormatException(
                                "Empty important file title");
                    }
                    this.importantFileTitles.add(title);
                } else if (che.getNodeName().equals("cmd-file")) {
                    final String path = che.getAttribute("path");
                    err = NWUtils.validateRelativePath(path);
                    if (err != null) {
                        throw new NumberFormatException(err);
                    }
                    this.cmdFilePaths.add(path);
                } else if (che.getNodeName().equals("file")) {
                    final String path = che.getAttribute("path");
                    err = NWUtils.validateRelativePath(path);
                    if (err != null) {
                        throw new NumberFormatException(err);
                    }

                    final String content = NWUtils.getTagContent_(che);
                    this.addFile(path, content);
                } else if (che.getNodeName().equals("dependency")) {
                    String depPackageName = che.getAttribute("package");
                    err = Package.checkName(depPackageName);
                    if (err != null) {
                        throw new NumberFormatException(err);
                    }
                    this.dependencyPackages.add(depPackageName);

                    // version range
                    final String versions = che.getAttribute("versions");
                    Dependency dep = new Dependency();
                    err = dep.setVersions(versions);
                    if (err != null) {
                        throw new NumberFormatException(err);
                    }
                    this.dependencyVersionRanges.add(versions);

                    // environment variable
                    final String var = NWUtils.getSubTagContent(che,
                            "variable", "").trim();
                    if (!var.isEmpty()) {
                        err = NWUtils.validateEnvVarName(var);
                        if (err != null) {
                            throw new NumberFormatException(err);
                        }
                    }
                    this.dependencyEnvVars.add(var);
                } else if (che.getNodeName().equals("hash-sum")) {
                    String type = che.getAttribute("type");
                    if ("SHA-1".equals(type)) {
                        if (!parsedSHA1.isEmpty()) {
                            throw new NumberFormatException(
                                    "SHA-1 was already found");
                        }
                        parsedSHA1 = NWUtils.getTagContent_(che);
                        err = NWUtils.validateSHA1(parsedSHA1);
                        if (err != null) {
                            throw new NumberFormatException(err);
                        }
                    } else if ("SHA-256".equals(type)) {
                        if (!parsedSHA256.isEmpty()) {
                            throw new NumberFormatException(
                                    "SHA-256 was already found");
                        }
                        parsedSHA256 = NWUtils.getTagContent_(che);
                        err = NWUtils.validateSHA256(parsedSHA256);
                        if (err != null) {
                            throw new NumberFormatException(err);
                        }
                    } else {
                        throw new NumberFormatException(
                                "Error in attribute 'type' in <hash-sum>");
                    }
                }
            }
        }

        // sha1
        String parsedSHA1_old = NWUtils.getSubTagContent(e, "sha1", "");
        if (!parsedSHA1_old.isEmpty()) {
            if (!parsedSHA1.isEmpty() && !parsedSHA1.equalsIgnoreCase(
                    parsedSHA1_old)) {
                throw new NumberFormatException("Two different SHA-1 values");
            }

            err = NWUtils.validateSHA1(parsedSHA1_old);
            if (err != null) {
                throw new NumberFormatException(err);
            }
        }

        if (!parsedSHA256.isEmpty()) {
            this.sha1 = parsedSHA256;
        } else if (!parsedSHA1.isEmpty()) {
            this.sha1 = parsedSHA1;
        } else if (!parsedSHA1_old.isEmpty()) {
            this.sha1 = parsedSHA1_old;
        }
    }


    /**
     * @param i index of the file
     * @return file contents &lt;file&gt;
     */
    public String getFileContents(int i) {
        Object obj = fileContents.get(i);
        if (obj instanceof Text) {
            return ((Text) obj).getValue();
        } else {
            return (String) obj;
        }
    }

    /**
     * Removes the file with the specified index.
     *
     * @param index index of the file
     */
    public void removeFile(int index) {
        this.filePaths.remove(index);
        this.fileContents.remove(index);
    }

    /**
     * Changes the content of the specified &lt;file&gt;
     *
     * @param index index of the file
     * @param content file content
     */
    public void setFileContents(int index, String content) {
        this.fileContents.set(index, new Text(content));
    }

    /**
     * Adds a new &lt;file&gt;
     *
     * @param path file path
     * @param content file content
     */
    public void addFile(String path, String content) {
        this.filePaths.add(path);
        this.fileContents.add(new Text(content));
    }

    /**
     * @return created Key for this object
     */
    public Key createKey() {
        return KeyFactory.createKey("PackageVersion", this.name);
    }

    /**
     * Removes all defined text files.
     */
    public void clearFiles() {
        this.filePaths.clear();
        this.fileContents.clear();
    }

    /**
     * @return the number of text files.
     */
    public int getFileCount() {
        return this.filePaths.size();
    }

    /**
     * @param checkSum true = also check SHA-1 or SHA-256
     * @param algorithm SHA-256 or SHA-1
     * @return info about the download or null if the download failed
     * @throws java.io.IOException error during the download
     */
    public NWUtils.Info check(boolean checkSum, String algorithm) throws
            IOException {
        NWUtils.Info info = null;
        String downloadCheckError;
        if (!this.url.isEmpty()) {
            try {
                info = NWUtils.download(this.url, algorithm, 0);
                if (checkSum) {
                    if (this.sha1.trim().isEmpty()) {
                        downloadCheckError = null;
                    } else {
                        String sha1_ = NWUtils.byteArrayToHexString(info.sha1);
                        if (sha1_.equalsIgnoreCase(this.sha1.trim())) {
                            downloadCheckError = null;
                        } else {
                            downloadCheckError =
                                    "Wrong SHA1: " + this.sha1 +
                                            " was expected, but " + sha1_ +
                                            " was found";
                        }
                    }
                } else {
                    downloadCheckError = null;
                }
            } catch (Exception e) {
                downloadCheckError =
                        "Error downloading: " + e.getMessage();
            }
        } else {
            downloadCheckError = null;
        }

        if (downloadCheckError != null) {
            throw new IOException(downloadCheckError);
        }

        return info;
    }

    /**
     * Adds a dependency
     *
     * @param package_ depends on this package
     * @param versions versions range like "[9, 10)"
     */
    public void addDependency(String package_, String versions) {
        addDependency(package_, versions, "");
    }

    /**
     * Adds a dependency
     *
     * @param package_ depends on this package
     * @param versions versions range like "[9, 10)"
     * @param envVar name of the environment variable or ""
     */
    public void addDependency(String package_, String versions, String envVar) {
        this.dependencyPackages.add(package_);
        this.dependencyVersionRanges.add(versions);
        this.dependencyEnvVars.add(envVar);
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
     * @param index index of the dependency
     * @return dependency
     */
    public Dependency getDependency(int index) {
        Dependency d = new Dependency();
        d.package_ = this.dependencyPackages.get(index);
        d.setVersions(this.dependencyVersionRanges.get(index));
        return d;
    }

    /**
     * Searches for the specified dependency.
     *
     * @param dep a dependency
     * @return the found index or -1
     */
    public int findDependency(Dependency dep) {
        int index = -1;
        for (int i = 0; i < this.dependencyPackages.size(); i++) {
            Dependency d = getDependency(i);
            if (d.equals(dep)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * Returns a human-readable description for this object
     *
     * @return "a.b.c 27.1.3"
     */
    public String getTitle() {
        return this.package_ + " " + this.version;
    }

    /**
     * Checks whether this package has the specified tag.
     *
     * @param tag tag name
     * @return true if this package has the specified tag
     */
    public boolean hasTag(String tag) {
        return this.tags != null && this.tags.contains(tag);
    }
}
