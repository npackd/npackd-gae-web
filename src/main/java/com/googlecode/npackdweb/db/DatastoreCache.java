package com.googlecode.npackdweb.db;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.pv.PackageVersionDetailAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * In-memory cache for the datastore entities.
 */
public class DatastoreCache {

    private ReentrantLock lock = new ReentrantLock();

    /**
     * version of the data (versions, packages, licenses): 0, 1, ...
     */
    private long dataVersion;

    private Map<String, Package> packagesCache =
            new HashMap<>();

    private Map<String, License> licensesCache =
            new HashMap<>();

    private Map<String, Editor> editorsCache = new HashMap<>();

    private boolean allLicensesRead;

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
                    UserServiceFactory.getUserService().getCurrentUser();
        }
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        datastore.put(p.createEntity());
        incDataVersion();
        Index index = NWUtils.getIndex();
        index.put(p.createDocument());
    }

    /**
     * Saves an editor.
     *
     * @param e editor
     */
    public void saveEditor(Editor e) {
        if (e.id <= 0) {
            e.createId();
        }
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        datastore.put(e.createEntity());
        incDataVersion();
    }

    /**
     * Deletes a license
     *
     * @param name license ID
     */
    public void deleteLicense(String name) {
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        datastore.delete(KeyFactory.createKey("License", name));

        incDataVersion();
    }

    /**
     * Writes a setting
     *
     * @param name setting name
     * @param value new setting value
     */
    public void setSetting(String name, String value) {
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();

        Entity e = null;
        try {
            e = datastore.get(KeyFactory.createKey("Setting", name));
        } catch (EntityNotFoundException ex) {
            // ignore
        }

        Setting st;
        if (e == null) {
            st = new Setting();
            st.name = name;
        } else {
            st = new Setting(e);
        }
        st.value = value;

        datastore.put(st.createEntity());
    }

    /**
     * Deletes a package
     *
     * @param name package name
     */
    public void deletePackage(String name) {
        // delete the package
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        datastore.delete(KeyFactory.createKey("Package", name));

        // delete package versions
        com.google.appengine.api.datastore.Query query =
                new com.google.appengine.api.datastore.Query("PackageVersion");
        query.setFilter(
                new com.google.appengine.api.datastore.Query.FilterPredicate(
                        "package_", FilterOperator.EQUAL, name));
        query.setKeysOnly();

        PreparedQuery pq = datastore.prepare(query);
        final List<Entity> list = pq.asList(FetchOptions.Builder.withDefaults());
        ArrayList<com.google.appengine.api.datastore.Key> keys =
                new ArrayList<>();
        for (Entity e : list) {
            keys.add(e.getKey());
        }
        datastore.delete(keys);

        Index index = NWUtils.getIndex();
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
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        datastore.put(p.createEntity());
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
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        Setting st = null;
        try {
            st = new Setting(datastore.get(KeyFactory.createKey(
                    "Setting", name)));
        } catch (EntityNotFoundException ex) {
            // ignore
        }

        String value;
        if (st == null) {
            st = new Setting();
            st.name = name;
            st.value = defaultValue;
            datastore.put(st.createEntity());
        }
        value = st.value;
        return value;
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
                    UserServiceFactory.getUserService().getCurrentUser();
        }
        if (changeNotReviewed) {
            if (NWUtils.isAdminLoggedIn()) {
                if (old != null && old.hasTag("not-reviewed")) {
                    UserService us = UserServiceFactory.getUserService();
                    if (!NWUtils.
                            isEqual(us.getCurrentUser(), old.lastModifiedBy)) {
                        NWUtils.sendMailTo(
                                "The package version " + p.getTitle() +
                                " (" + PackageVersionDetailAction.getURL(p) +
                                ") was marked as reviewed",
                                old.lastModifiedBy.getEmail());
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
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        datastore.put(p.createEntity());
        incDataVersion();
    }

    /**
     * Saves a repository.
     *
     * @param r repository
     */
    public void saveRepository(Repository r) {
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        datastore.put(r.createEntity());
        incDataVersion();
    }

    /**
     * Searches for an editor.
     *
     * @param u a user
     * @return the found editor or null
     */
    public Editor findEditor(User u) {
        final String email = u.getEmail();
        Editor ret = null;

        lock.lock();
        try {
            ret = editorsCache.get(email);
        } finally {
            lock.unlock();
        }
        if (ret != null) {
            ret = ret.clone();
        }

        if (ret == null) {
            DatastoreService datastore = DatastoreServiceFactory.
                    getDatastoreService();
            try {
                ret = new Editor(datastore.
                        get(KeyFactory.createKey("Editor", email)));
                lock.lock();
                try {
                    editorsCache.put(email, ret.clone());
                } finally {
                    lock.unlock();
                }
            } catch (EntityNotFoundException ex) {
                // ignore
            }
        }
        return ret;
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
            if (e.starredPackages.indexOf(p.name) < 0) {
                Package oldp = p.copy();
                p.starred++;
                savePackage(oldp, p, false);
                e.starredPackages.add(p.name);
                saveEditor(e);
            }
        } else {
            if (e != null && e.starredPackages.indexOf(p.name) >= 0) {
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
     * @return all defined repositories
     */
    public List<Repository> findAllRepositories() {
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();

        com.google.appengine.api.datastore.Query query =
                new com.google.appengine.api.datastore.Query("Repository");

        PreparedQuery pq = datastore.prepare(query);
        final List<Entity> list =
                pq.asList(FetchOptions.Builder.withDefaults());

        List<Repository> res = new ArrayList<>();
        for (Entity e : list) {
            res.add(new Repository(e));
        }

        return res;
    }

    /**
     * Searches for the repository with the given tag.
     *
     * @param tag tag name
     * @return found repository or null
     */
    public Repository findRepository(String tag) {
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        Repository ret = null;
        try {
            ret = new Repository(datastore.get(KeyFactory.createKey(
                    "Repository",
                    tag)));
        } catch (EntityNotFoundException ex) {
            // ignore
        }
        return ret;
    }

    /**
     * Searches for the repository with the given tag.
     *
     * @param id ID of the entity
     * @return found repository or null
     */
    public Repository getRepository(long id) {
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        Repository ret = null;
        try {
            ret = new Repository(datastore.get(KeyFactory.createKey(
                    "Repository",
                    id)));
        } catch (EntityNotFoundException ex) {
            // ignore
        }
        return ret;
    }

    /**
     * Returns all entities for a query fetching them in batches.
     *
     * @param q a query
     * @return found entities
     */
    public List<Entity> getAllEntities(Query q) {
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();

        List<Entity> r = new ArrayList<>();
        final FetchOptions fo = FetchOptions.Builder.withDefaults();
        PreparedQuery pq = datastore.prepare(q);
        fo.limit(500);

        Cursor cursor = null;
        while (true) {
            if (cursor != null) {
                fo.startCursor(cursor);
            }
            QueryResultList<Entity> list = pq.asQueryResultList(fo);
            if (list.size() == 0) {
                break;
            }
            cursor = list.getCursor();

            r.addAll(list);
        }

        return r;
    }

    /**
     * @param tag a tag to filter the package versions or null
     * @param order how to order the query (e.g. "-lastModifiedAt") or null
     * @param limit maximum number of returned package versions or 0 for
     * "unlimited"
     * @return first 20 package versions with errors downloading the binary
     */
    public List<PackageVersion> findPackageVersions(
            String tag, String order, int limit) {
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();

        com.google.appengine.api.datastore.Query query =
                new com.google.appengine.api.datastore.Query("PackageVersion");
        if (tag != null) {
            query.setFilter(
                    new com.google.appengine.api.datastore.Query.FilterPredicate(
                            "tags", FilterOperator.EQUAL, tag));
        }
        if (order != null) {
            query.addSort(order);
        }

        List<Entity> list;
        if (limit <= 0) {
            list = getAllEntities(query);
        } else {
            PreparedQuery pq = datastore.prepare(query);
            final FetchOptions fo = FetchOptions.Builder.withDefaults();
            if (limit > 0) {
                fo.limit(limit);
            }
            list = pq.asList(fo);
        }

        List<PackageVersion> res = new ArrayList<>();
        for (Entity e : list) {
            res.add(new PackageVersion(e));
        }

        return res;
    }

    /**
     * @param package_ package ID
     * @return sorted versions (1.1, 1.2, 1.3) for this package
     */
    public List<PackageVersion> getSortedVersions(String package_) {
        List<PackageVersion> versions = getPackageVersions(package_);
        Collections.sort(versions, new Comparator<PackageVersion>() {
            @Override
            public int compare(PackageVersion a, PackageVersion b) {
                Version va = Version.parse(a.version);
                Version vb = Version.parse(b.version);
                return va.compare(vb);
            }
        });
        return versions;
    }

    /**
     * Increments the version of the data.
     *
     * @return new version number
     */
    public long incDataVersion() {
        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers
                .getConsistentLogAndContinue(Level.INFO));
        Long v = syncCache.increment("DataVersion", 1L);
        if (v == null) {
            syncCache.put("DataVersion", 1L);
            v = 1L;
        }

        lock.lock();
        try {
            dataVersion = v;
            packagesCache.clear();
            licensesCache.clear();
            editorsCache.clear();
            allLicensesRead = false;
        } finally {
            lock.unlock();
        }

        return v;
    }

    /**
     * Reads the data version from the memcached and updates the static
     * in-memory cache.
     */
    public void updateDataVersion() {
        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers
                .getConsistentLogAndContinue(Level.INFO));
        Long v = (Long) syncCache.get("DataVersion");
        if (v == null) {
            syncCache.put("DataVersion", 1L);
            v = 1L;
        }

        lock.lock();
        try {
            if (dataVersion != v) {
                dataVersion = v;
                packagesCache.clear();
                licensesCache.clear();
                editorsCache.clear();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns packages by their IDs.
     *
     * @param ids internal package names
     * @param useCache true = use cache
     * @return found packages
     */
    public List<Package> getPackages(List<String> ids, boolean useCache) {
        List<Package> packages = new ArrayList<>();

        if (ids.size() > 0) {
            if (useCache) {
                lock.lock();
                try {
                    for (String id : ids) {
                        Package p = packagesCache.get(id);
                        if (p == null) {
                            packages.clear();
                            break;
                        }
                        packages.add(p);
                    }
                } finally {
                    lock.unlock();
                }
            }

            if (packages.size() == 0) {
                DatastoreService datastore = DatastoreServiceFactory.
                        getDatastoreService();

                List<com.google.appengine.api.datastore.Key> keys =
                        new ArrayList<>();
                for (String id : ids) {
                    keys.add(KeyFactory.createKey("Package", id));
                }

                final Map<com.google.appengine.api.datastore.Key, Entity> map =
                        datastore.get(keys);
                for (Map.Entry<com.google.appengine.api.datastore.Key, Entity> e
                        : map.entrySet()) {
                    Entity p = e.getValue();
                    if (p != null) {
                        packages.add(new Package(p));
                    }
                }

                lock.lock();
                try {
                    for (Package p : packages) {
                        packagesCache.put(p.name, p);
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                NWUtils.LOG.info("Got packages from the memory cache!");
            }
            for (int i = 0; i < packages.size(); i++) {
                packages.set(i, packages.get(i).copy());
            }
        }

        return packages;
    }

    /**
     * Returns a license by its ID.
     *
     * @param id internal license ID
     * @param useCache true = use the cache
     * @return found license or null
     */
    public License getLicense(String id, boolean useCache) {
        License ret = null;

        if (useCache) {
            lock.lock();
            try {
                ret = licensesCache.get(id);
            } finally {
                lock.unlock();
            }
        }

        if (ret == null) {
            DatastoreService datastore = DatastoreServiceFactory.
                    getDatastoreService();
            try {
                ret = new License(datastore.get(KeyFactory.createKey(
                        "License",
                        id)));
            } catch (EntityNotFoundException ex) {
                // ignore
            }

            if (ret != null) {
                lock.lock();
                try {
                    licensesCache.put(id, ret);
                } finally {
                    lock.unlock();
                }
            }
        }

        if (ret != null) {
            ret = ret.copy();
        }

        return ret;
    }

    /**
     * Returns a package version by its ID.
     *
     * @param id internal package version ID
     * @return found package version or null
     */
    public PackageVersion getPackageVersion(String id) {
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        try {
            return new PackageVersion(datastore.get(KeyFactory.createKey(
                    "PackageVersion",
                    id)));
        } catch (EntityNotFoundException ex) {
            return null;
        }
    }

    /**
     * Returns a package by its ID.
     *
     * @param id internal package ID
     * @param useCache true = use cache
     * @return found package or null
     */
    public Package getPackage(String id, boolean useCache) {
        Package ret = null;

        if (useCache) {
            lock.lock();
            try {
                ret = packagesCache.get(id);
            } finally {
                lock.unlock();
            }
        }

        if (ret == null) {
            DatastoreService datastore = DatastoreServiceFactory.
                    getDatastoreService();
            try {
                ret = new Package(datastore.get(KeyFactory.createKey("Package",
                        id)));
            } catch (EntityNotFoundException ex) {
                // ignore
                //NWUtils.LOG.info("Cannot find the package " + id);
            }

            if (ret != null) {
                lock.lock();
                try {
                    packagesCache.put(id, ret);
                } finally {
                    lock.unlock();
                }
            }
        }

        if (ret != null) {
            ret = ret.copy();
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
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        datastore.delete(KeyFactory.createKey("PackageVersion", p.name));

        incDataVersion();
    }

    public Package findNextPackage(Package p) {
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();

        com.google.appengine.api.datastore.Query query =
                new com.google.appengine.api.datastore.Query("Package");
        query.setFilter(
                new com.google.appengine.api.datastore.Query.FilterPredicate(
                        "title", FilterOperator.GREATER_THAN_OR_EQUAL, p.title));
        query.addSort("title");

        PreparedQuery pq = datastore.prepare(query);
        final List<Entity> ps =
                pq.asList(FetchOptions.Builder.withLimit(5));

        Package next = null;

        // find the next package
        for (int i = 0; i < ps.size() - 1; i++) {
            Entity n = ps.get(i);

            if (n.getKey().getName().equals(p.name)) {
                next = new Package(ps.get(i + 1));
                break;
            }
        }

        return next;
    }

    /**
     * Finds an editor by its ID.
     *
     * @param id ID
     * @return found editor or null
     */
    public Editor findEditor(int id) {
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();

        com.google.appengine.api.datastore.Query query =
                new com.google.appengine.api.datastore.Query("Editor");
        query.setFilter(
                new com.google.appengine.api.datastore.Query.FilterPredicate(
                        "id", FilterOperator.EQUAL, id));

        PreparedQuery pq = datastore.prepare(query);
        final List<Entity> editors =
                pq.asList(FetchOptions.Builder.withDefaults());

        if (editors.size() > 0) {
            return new Editor(editors.get(0));
        } else {
            return null;
        }
    }

    /**
     * Deletes a repository.
     *
     * @param id repository ID
     */
    public void deleteRepository(long id) {
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();
        datastore.delete(KeyFactory.createKey("Repository", id));

        incDataVersion();
    }

    /**
     * @param id package ID
     * @return all versions for the package
     */
    public List<PackageVersion> getPackageVersions(String id) {
        DatastoreService datastore = DatastoreServiceFactory.
                getDatastoreService();

        com.google.appengine.api.datastore.Query query =
                new com.google.appengine.api.datastore.Query("PackageVersion");
        query.setFilter(
                new com.google.appengine.api.datastore.Query.FilterPredicate(
                        "package_", FilterOperator.EQUAL, id));

        PreparedQuery pq = datastore.prepare(query);
        final List<Entity> list =
                pq.asList(FetchOptions.Builder.withDefaults());

        List<PackageVersion> res = new ArrayList<>();
        for (Entity e : list) {
            res.add(new PackageVersion(e));
        }

        return res;
    }

    /**
     * @return all licenses
     */
    public List<License> getAllLicenses() {
        List<License> licenses = new ArrayList<>();
        lock.lock();
        try {
            if (allLicensesRead) {
                for (License lic : licensesCache.values()) {
                    licenses.add(lic.copy());
                }
            }
        } finally {
            lock.unlock();
        }

        if (licenses.size() == 0) {
            DatastoreService datastore = DatastoreServiceFactory.
                    getDatastoreService();

            com.google.appengine.api.datastore.Query query =
                    new com.google.appengine.api.datastore.Query("License");
            query.addSort("title");
            PreparedQuery pq = datastore.prepare(query);
            final List<Entity> list =
                    pq.asList(FetchOptions.Builder.withDefaults());

            lock.lock();
            try {
                licensesCache.clear();
                allLicensesRead = true;
                for (Entity e : list) {
                    License lic = new License(e);
                    licensesCache.put(lic.name, lic);
                    licenses.add(lic.copy());
                }
            } finally {
                lock.unlock();
            }
        }

        return licenses;
    }
}
