package com.googlecode.npackdweb.db;

import com.googlecode.npackdweb.AuthService;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.SearchService;
import com.googlecode.npackdweb.User;
import com.googlecode.npackdweb.pv.PackageVersionDetailAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * In-memory cache for the datastore entities.
 */
public class DatastoreCache {

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * version of the data (versions, packages, licenses): 0, 1, ...
     */
    private long dataVersion;

    private final Connection con;
    private final Document config;

    public DatastoreCache() {
        new File(NWUtils.BASE_PATH).mkdirs();

        String password;
        try {
            config = readConfig();
            password = getSetting("db-password", null);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            throw new InternalError(ex);
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost/npackd?" +
                    "user=npackd&password=" + password + "&serverTimezone=UTC&profileSQL=true");
            updateDB(con);
        } catch (ClassNotFoundException | SQLException ex) {
            throw new InternalError(ex);
        }
    }

    private static String toSQL(List<String> ids) {
        StringBuilder where = new StringBuilder();
        for (String id : ids) {
            if (!where.isEmpty()) {
                where.append(",");
            }
            where.append('\'').append(escape(id)).append('\'');
        }
        return where.toString();
    }

    private static Document readConfig() throws SAXException, IOException,
            ParserConfigurationException {
        File fXmlFile = new File("/etc/npackd/config.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.
                newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);

        //optional, but recommended
        //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();

        return doc;
    }

    private void exec(final String sql) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.execute(sql);
        stmt.close();
    }

    private void exec(final String sql, final Object... params) throws SQLException {
        if (params.length == 0) {
            Statement stmt = con.createStatement();
            stmt.execute(sql);
            stmt.close();
        } else {
            try (PreparedStatement statement = con.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i, params[i]);
                }

                statement.executeUpdate();
            }
        }
    }

    private void updateDB(final Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.execute(
                "CREATE TABLE if not exists PACKAGE (" +
                "NAME varchar(255) PRIMARY KEY," +
                "TITLE varchar(1024) NOT NULL," +
                "URL varchar(2048) NOT NULL," +
                "ICON varchar(2048) NOT NULL," +
                "DESCRIPTION varchar(4096) NOT NULL," +
                "LICENSE varchar(255) NOT NULL," +
                "CATEGORY0 int NOT NULL," +
                "CATEGORY1 int NOT NULL," +
                "CATEGORY2 int NOT NULL," +
                "CATEGORY3 int NOT NULL," +
                "CATEGORY4 int NOT NULL," +
                "STARS int NOT NULL)"
        );

        stmt.execute(
                "CREATE TABLE if not exists EDITOR (" +
                "ID int NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "EMAIL varchar(255) NOT NULL)"
        );

        stmt.execute(
                "CREATE TABLE if not exists PACKAGE_VERSION (" +
                "PACKAGE varchar(255) NOT NULL," +
                "NAME varchar(255) NOT NULL," +
                "URL varchar(2048) NOT NULL," +
                "CONTENT BLOB NOT NULL," +
                "PRIMARY KEY (PACKAGE, NAME)" +
                ")"
        );

        stmt.execute(
            "CREATE TABLE if not exists LICENSE(" +
            "NAME varchar(255) PRIMARY KEY, " +
            "TITLE varchar(255) NOT NULL, " +
            "DESCRIPTION varchar(4096) NOT NULL, " +
            "URL varchar(2048) NOT NULL" +
            ")"
        );

        stmt.execute("CREATE TABLE if not exists REPOSITORY(NAME varchar(255) NOT NULL PRIMARY KEY)");

        stmt.close();
    }

    /**
     * Saves a package. The package can be new or an already existing one. The
     * total number of packages and the index will be automatically updated.
     *
     * @param old old version of the package object or null
     * @param p package
     * @param changeLastModifiedAt change the last modification time
     */
    public void savePackage(Package old,
            Package p, boolean changeLastModifiedAt) {
        if (changeLastModifiedAt) {
            p.lastModifiedAt = NWUtils.newDate();
            p.lastModifiedBy =
                    AuthService.getInstance().getCurrentUser();
        }

        try (PreparedStatement statement = con.prepareStatement(
                "REPLACE INTO PACKAGE(NAME, TITLE, URL, ICON, DESCRIPTION, LICENSE, " +
                "CATEGORY0, CATEGORY1, CATEGORY2, CATEGORY3, CATEGORY4, STARS " +
                ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, p.name);
            statement.setString(2, p.title);
            statement.setString(3, p.url);
            statement.setString(4, p.icon);
            statement.setString(5, p.description);
            statement.setString(6, p.license);
            statement.setInt(7, 0);
            statement.setInt(8, 0);
            statement.setInt(9, 0);
            statement.setInt(10, 0);
            statement.setInt(11, 0);
            statement.setInt(12, 0);

            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new InternalError(ex);
        }

        incDataVersion();
        SearchService index = SearchService.getInstance();
        List<org.apache.lucene.document.Document> docs = new ArrayList<>();
        docs.add(p.createDocument(findAllRepositories()));
        index.addDocuments(docs);
    }

    /**
     * Saves an editor.
     *
     * @param e editor
     */
    public void saveEditor(Editor e) {
        if (e.lastLogin == null) {
            e.lastLogin = NWUtils.newDay();
        }

        try (PreparedStatement statement = con.prepareStatement(
                "REPLACE INTO EDITOR(EMAIL) VALUES(?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, e.name);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    e.id = generatedKeys.getLong(1);
                } else {
                    throw new SQLException(
                            "Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException ex) {
            throw new InternalError(ex);
        }

        incDataVersion();
    }

    /**
     * Deletes a license
     *
     * @param name license ID
     */
    public void deleteLicense(String name) {
        try {
            exec("delete from LICENSE where NAME='" + escape(name) + "'");
        } catch (SQLException e) {
            throw new InternalError(e);
        }

        incDataVersion();
    }

    /**
     * Deletes a package. The corresponding package versions are also deleted.
     *
     * @param name package name
     */
    public void deletePackage(String name) {
        try {
            exec("delete from PACKAGE_VERSION where PACKAGE='" + escape(name) + "'");
            exec("delete from PACKAGE where NAME='" + escape(name) + "'");
        } catch (SQLException e) {
            throw new InternalError(e);
        }

        SearchService index = SearchService.getInstance();
        index.delete(name);
        incDataVersion();
    }

    /**
     * Saves a license
     *
     * @param p license
     * @param changeLastModifiedAt true = change the last modification time
     */
    public void saveLicense(License p, boolean changeLastModifiedAt) {
        if (changeLastModifiedAt) {
            p.lastModifiedAt = NWUtils.newDate();
        }

        try (PreparedStatement statement = con.prepareStatement(
                "REPLACE INTO LICENSE(NAME, TITLE, DESCRIPTION, URL " +
                        ") VALUES(?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, p.name);
            statement.setString(2, p.title);
            statement.setString(3, p.description);
            statement.setString(4, p.url);

            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new InternalError(ex);
        }

        incDataVersion();
    }

    /**
     * Reads a setting
     *
     * @param name setting name
     * @param defaultValue default value returned if the setting does not exist
     * @return setting value
     */
    public String getSetting(String name, String defaultValue) {
        NodeList nl = config.getElementsByTagName(name);
        String res;
        if (nl.getLength() > 0) {
            res = nl.item(0).getTextContent();
        } else {
            res = defaultValue;
        }
        return res;
    }

    /**
     * Saves a package version.
     *
     * @param old previous state in the database or null if not existent
     * @param p package version
     * @param changeLastModified last modified at/by will be changed, if true
     * @param changeNotReviewed not-reviewed tag will be changed, if true
     */
    public void savePackageVersion(
            PackageVersion old, PackageVersion p, boolean changeLastModified,
            boolean changeNotReviewed) {
        if (changeLastModified) {
            p.lastModifiedAt = NWUtils.newDate();
            p.lastModifiedBy =
                    AuthService.getInstance().getCurrentUser();
        }
        if (changeNotReviewed) {
            if (NWUtils.isAdminLoggedIn()) {
                if (old != null && old.hasTag("not-reviewed")) {
                    AuthService us = AuthService.getInstance();
                    if (!NWUtils.
                            isEqual(us.getCurrentUser(), old.lastModifiedBy)) {
                        NWUtils.sendMailTo(
                                "The package version " + p.getTitle() +
                                " (" + PackageVersionDetailAction.getURL(p) +
                                ") was marked as reviewed",
                                old.lastModifiedBy.email);
                    }
                }
                p.tags.remove("not-reviewed");
            } else {
                p.addTag("not-reviewed");
                if (old == null || !old.hasTag("not-reviewed")) {
                    NWUtils.sendMailToAdmin("The package version " + p.
                            getTitle() +
                            " (" + PackageVersionDetailAction.getURL(p) +
                            ") was marked as not reviewed");
                }
            }
        }

        Document d = NWUtils.newXMLRepository(false);
        d.getDocumentElement().appendChild(p.toXML(d));

        try (PreparedStatement statement = con.prepareStatement(
                "REPLACE INTO PACKAGE_VERSION(NAME, PACKAGE, URL, CONTENT " +
                        ") VALUES(?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, p.version);
            statement.setString(2, p.package_);
            statement.setString(3, p.url);
            statement.setString(4, NWUtils.toString(d));

            statement.executeUpdate();
        } catch (SQLException | IOException ex) {
            throw new InternalError(ex);
        }

        incDataVersion();
    }

    /**
     * Saves a repository.
     *
     * @param r repository
     */
    public void saveRepository(Repository r) {
        try (PreparedStatement statement = con.prepareStatement(
                "REPLACE INTO REPOSITORY(NAME) VALUES(?)")) {
            statement.setString(1, r.name);

            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new InternalError(ex);
        }

        incDataVersion();
    }

    /**
     * Searches for an editor.
     *
     * @param u a user
     * @return the found editor or null
     */
    public Editor findEditor(User u) {
        List<Editor> list = selectEditors("WHERE EMAIL = '" + escape(u.email) + "'");
        Editor r;
        if (list.size() > 0)
            r = list.get(0);
        else
            r = null;
        return r;
    }

    /**
     * Adds or removes a star from a package
     *
     * @param p a package
     * @param e an editor/user
     * @param star true = star, false = unstar
     */
    public void starPackage(Package p,
            Editor e, boolean star) {
        if (star) {
            if (!e.starredPackages.contains(p.name)) {
                Package oldp = p.copy();
                p.starred++;
                savePackage(oldp, p, false);
                e.starredPackages.add(p.name);
                saveEditor(e);
            }
        } else {
            if (e != null && e.starredPackages.contains(p.name)) {
                Package oldp = p.copy();
                p.starred--;
                if (p.starred < 0) {
                    p.starred = 0;
                }
                savePackage(oldp, p, false);
                e.starredPackages.remove(p.name);
                saveEditor(e);
            }
        }
    }

    /**
     * @return all repositories
     */
    public List<Repository> findAllRepositories() {
        return selectRepositories("");
    }

    /**
     * Searches for the repository with the given tag.
     *
     * @param tag tag name
     * @param useCache true = use cache
     * @return found repository or null
     */
    public Repository findRepository(String tag, boolean useCache) {
        List<Repository> list = selectRepositories("where NAME='" + escape(tag) + "'");
        Repository r;
        if (list.size() > 0)
            r = list.get(0);
        else
            r = null;
        return r;
    }

    /**
     * Searches for the repository with the given tag.
     *
     * @param id ID of the entity
     * @return found repository or null
     */
    public Repository getRepository(String id) {
        List<Repository> reps = selectRepositories("WHERE NAME='" + escape(id) + "'");
        Repository res;
        if (reps.size() > 0)
            res = reps.get(0);
        else
            res = null;
        return res;
    }

    /**
     * @param tag a tag to filter the package versions or null
     * @param order how to order the query (e.g. "NAME ASC") or null
     * @param limit maximum number of returned package versions or 0 for
     * "unlimited"
     * @return the package versions
     */
    public List<PackageVersion> findPackageVersions(
            String tag, String order, int limit) {
        // TODO: tag
        String where = "";
        if (order != null)
            where += " ORDER BY " + order;
        if (limit > 0)
            where += " LIMIT " + limit;

        return selectPackageVersions(where);
    }

    /**
     * @param tag a tag to filter the package or null
     * @param order how to order the query (e.g. "NAME DESC") or null
     * @param limit maximum number of returned package versions or 0 for
     * "unlimited"
     * @return the packages
     */
    public List<Package> findPackages(
            String tag, String order, int limit) {
        String where = "";
        if (tag != null)
            where += " WHERE TAG = '" + escape(tag) + "'";
        if (order != null)
            where +=" ORDER BY " + order;
        if (limit > 0)
            where += " LIMIT " + limit;

        return selectPackages(where);
    }

    private List<Package> selectPackages(final String where) {
        List<Package> r = new ArrayList<>();
        try {
            PreparedStatement stmt =
                    con.prepareStatement(
                            "select * from PACKAGE " + where);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Package pv = new Package(rs);
                r.add(pv);
            }
            stmt.close();
        } catch (SQLException ex) {
            throw new InternalError(ex);
        }

        return r;
    }

    private List<Repository> selectRepositories(final String where) {
        List<Repository> r = new ArrayList<>();
        try {
            PreparedStatement stmt =
                    con.prepareStatement(
                            "select * from REPOSITORY " + where);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Repository pv = new Repository(rs);
                r.add(pv);
            }
            stmt.close();
        } catch (SQLException ex) {
            throw new InternalError(ex);
        }

        return r;
    }

    private List<License> selectLicenses(final String where) {
        List<License> r = new ArrayList<>();
        try {
            PreparedStatement stmt =
                    con.prepareStatement(
                            "select * from LICENSE " + where);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                License pv = new License(rs);
                r.add(pv);
            }
            stmt.close();
        } catch (SQLException ex) {
            throw new InternalError(ex);
        }

        return r;
    }

    private List<Editor> selectEditors(final String where) {
        List<Editor> r = new ArrayList<>();
        try {
            PreparedStatement stmt =
                    con.prepareStatement(
                            "select * from EDITOR " + where);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Editor pv = new Editor(rs);
                r.add(pv);
            }
            stmt.close();
        } catch (SQLException ex) {
            throw new InternalError(ex);
        }

        return r;
    }

    private List<PackageVersion> selectPackageVersions(final String where) {
        List<PackageVersion> r = new ArrayList<>();
        try {
            PreparedStatement stmt =
                    con.prepareStatement(
                            "select * from PACKAGE_VERSION " + where);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DocumentBuilder db =
                        javax.xml.parsers.DocumentBuilderFactory.newInstance()
                                .newDocumentBuilder();
                // TODO encoding
                ByteArrayInputStream is = new ByteArrayInputStream(rs.getString("CONTENT").getBytes());
                Document d = db.parse(is);
                PackageVersion pv = new PackageVersion((Element)
                        d.getDocumentElement().getElementsByTagName("version").item(0));
                r.add(pv);
            }
            stmt.close();
        } catch (SAXException | IOException | SQLException | ParserConfigurationException ex) {
            throw new InternalError(ex);
        }

        return r;
    }

    /**
     * @param package_ package ID
     * @return sorted versions (1.1, 1.2, 1.3) for this package
     */
    public List<PackageVersion> getSortedVersions(String package_) {
        List<PackageVersion> versions = getPackageVersions(package_);
        Collections.sort(versions, (a, b) -> {
            Version va  = Version.parse(a.version);
            Version vb = Version.parse(b.version);
            return va.compare(vb);
        });
        return versions;
    }

    /**
     * Increments the version of the data.
     *
     * @return new version number
     */
    public long incDataVersion() {
        long r;

        lock.lock();
        try {
            dataVersion++;
            r = dataVersion;
        } finally {
            lock.unlock();
        }

        return r;
    }

    /**
     * Returns packages by their IDs.
     *
     * @param ids internal package names
     * @param useCache true = use cache
     * @return found packages
     */
    public List<Package> getPackages(List<String> ids, boolean useCache) {
        if (ids.size() == 0)
            return new ArrayList<>();

        return selectPackages("where NAME IN (" + toSQL(ids) + ")");
    }

    /**
     * Returns a license by its ID.
     *
     * @param id internal license ID
     * @param useCache true = use the cache
     * @return found license or null
     */
    public License getLicense(String id, boolean useCache) {
        List<License> licenses = selectLicenses("where NAME = '" + escape(id) + "'");
        License ret;
        if (licenses.size() > 0)
            ret = licenses.get(0);
        else
            ret = null;

        return ret;
    }

    /**
     * Returns a package version by its ID.
     *
     * @param package_ internal package version ID
     * @param version version number
     * @return found package version or null
     */
    public PackageVersion getPackageVersion(String package_, String version) {
        List<PackageVersion> list = selectPackageVersions("where PACKAGE='" + escape(package_) +
                "' and NAME = '" + escape(version) + "'");
        PackageVersion r;
        if (list.size() > 0)
            r = list.get(0);
        else
            r = null;

        return r;
    }

    /**
     * Returns a package by its ID.
     *
     * @param id internal package ID
     * @param useCache true = use cache
     * @return found package or null
     */
    public Package getPackage(String id, boolean useCache) {
        List<Package> packages = selectPackages("WHERE NAME = '" + escape(id) + "'");
        Package ret = null;
        if (packages.size() > 0) {
            ret = packages.get(0);
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * @return current data version
     */
    public long getDataVersion() {
        long v;
        lock.lock();
        try {
            v = dataVersion;
        } finally {
            lock.unlock();
        }
        return v;
    }

    /**
     * Deletes a package version.
     *
     * @param p this package version will be deleted
     */
    public void deletePackageVersion(PackageVersion p) {
        try {
            exec("delete from PACKAGE_VERSION where PACKAGE='" + escape(p.package_) +
                    "' AND NAME = '" + escape(p.name) + "'");
        } catch (SQLException e) {
            throw new InternalError(e);
        }

        incDataVersion();
    }

    public Package findNextPackage(Package p) {
        List<Package> list = selectPackages("WHERE NAME > '" + escape(p.name) + "' LIMIT 1");
        Package r;
        if (list.size() > 0)
            r = list.get(0);
        else
            r = null;
        return r;
    }

    /**
     * Finds an editor by its ID.
     *
     * @param id ID
     * @return found editor or null
     */
    public Editor findEditor(int id) {
        List<Editor> list = selectEditors("WHERE ID=" + id);
        Editor r;
        if (list.size() > 0)
            r = list.get(0);
        else
            r = null;
        return null;
    }

    /**
     * Deletes a repository.
     *
     * @param id repository ID
     */
    public void deleteRepository(String id) {
        try {
            exec("delete from REPOSITORY where ID='" + escape(id) + "'");
        } catch (SQLException e) {
            throw new InternalError(e);
        }

        incDataVersion();
    }

    /**
     * @param id package ID
     * @return all versions for the package
     */
    public List<PackageVersion> getPackageVersions(String id) {
        return selectPackageVersions("WHERE PACKAGE = '" + escape(id) + "'");
    }

    /**
     * @return all licenses
     */
    public List<License> getAllLicenses() {
        return selectLicenses("");
    }

    public List<PackageVersion> getRecentlyChangedPackageVersions() {
        return selectPackageVersions("ORDER BY LAST_MODIFIED DESC LIMIT 20");

        /* TODO
        if (user != null) {
            query.setFilter(
                    new com.google.appengine.api.datastore.Query.FilterPredicate(
                            "lastModifiedBy",
                            com.google.appengine.api.datastore.Query.FilterOperator.EQUAL,
                            new User(user, user.substring(user
                                    .indexOf('@')))));
        }
         */
    }

    public List<License> getLicenses(List<String> lns2) {
        if (lns2.size() == 0)
            return new ArrayList<>();

        return selectLicenses("where NAME in (" + toSQL(lns2) + ")");
    }

    public void savePackageVersions(List<PackageVersion> toSave) {
        for (PackageVersion pv: toSave) {
            savePackageVersion(null, pv, true, true); // TODO: strange parameters
        }
    }

    static String escape(final String s) {
        // https://dev.mysql.com/doc/refman/8.0/en/string-literals.html
        // PHP mysql_real_escape_string: \x00, \n, \r, \, ', " und \x1a.
        // MySQL QUOTE function: single  quote, backslash, ASCII NUL and control-Z with a backslash.
        boolean ok = true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\0' || c == '\n' || c == '\r' || c == '\\' || c == '\'' || c == '"' || c == '\u001a' || c == '\b') {
                ok = false;
                break;
            }
        }

        if (ok) {
            return s;
        } else {
            char[] buf = new char[s.length() * 2];
            int where = 0;

            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '\0':
                        buf[where] = '\\';
                        where++;
                        buf[where] = '0';
                        where++;
                        break;
                    case '\n':
                        buf[where] = '\\';
                        where++;
                        buf[where] = 'n';
                        where++;
                        break;
                    case '\r':
                        buf[where] = '\\';
                        where++;
                        buf[where] = 'r';
                        where++;
                        break;
                    case '\\':
                        buf[where] = '\\';
                        where++;
                        buf[where] = '\\';
                        where++;
                        break;
                    case '\'':
                        buf[where] = '\\';
                        where++;
                        buf[where] = '\'';
                        where++;
                        break;
                    case '"':
                        buf[where] = '\\';
                        where++;
                        buf[where] = '"';
                        where++;
                        break;
                    case '\u001a': // Ctrl+Z
                        buf[where] = '\\';
                        where++;
                        buf[where] = 'Z';
                        where++;
                        break;
                    case '\b': // Backspace
                        buf[where] = '\\';
                        where++;
                        buf[where] = 'b';
                        where++;
                        break;
                    default:
                        buf[where] = c;
                        where++;
                }
            }
            return new String(buf, 0, where);
        }
    }
}
