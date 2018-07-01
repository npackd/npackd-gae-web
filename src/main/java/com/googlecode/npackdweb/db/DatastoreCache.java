package com.googlecode.npackdweb.db;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.pv.PackageVersionDetailAction;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import static com.googlecode.objectify.ObjectifyService.ofy;
import com.googlecode.objectify.cmd.Query;
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
        ofy().save().entity(p);
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
        ofy().save().entity(e);
        incDataVersion();
    }

    /**
     * Deletes a license
     *
     * @param name license ID
     */
    public void deleteLicense(String name) {
        Objectify ob = ofy();
        License p = ob.load().key(Key.create(License.class, name)).now();
        ob.delete().entity(p);
        incDataVersion();
    }

    /**
     * Writes a setting
     *
     * @param name setting name
     * @param value new setting value
     */
    public void setSetting(String name, String value) {
        Objectify ob = ofy();
        Setting st = ob.load().key(Key.create(Setting.class, name)).now();
        if (st == null) {
            st = new Setting();
            st.name = name;
        }
        st.value = value;
        ob.save().entity(st);
    }

    /**
     * Deletes a package
     *
     * @param name package name
     */
    public void deletePackage(String name) {
        Objectify ob = ofy();
        Package p = ob.load().key(Key.create(Package.class, name)).now();
        ob.delete().entity(p);
        QueryResultIterable<Key<PackageVersion>> k =
                ob.load().type(PackageVersion.class).filter("package_ =", name).
                        keys();
        ob.delete().keys(k);
        Index index = NWUtils.getIndex();
        index.delete(p.name);
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
        ofy().save().entity(p);
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
        Objectify ob = ofy();
        Setting st = ob.load().key(Key.create(Setting.class, name)).now();
        String value;
        if (st == null) {
            st = new Setting();
            st.name = name;
            st.value = defaultValue;
            ob.save().entity(st);
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
        ofy().save().entity(p);
        incDataVersion();
    }

    /**
     * Saves a repository.
     *
     * @param r repository
     */
    public void saveRepository(Repository r) {
        ofy().save().entity(r);
        incDataVersion();
    }

    /**
     * Searches for an editor.
     *
     * @param u a user
     * @return the found editor or null
     */
    public Editor findEditor(User u) {
        return ofy().load().key(Key.create(Editor.class, u.getEmail())).now();
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
        return ofy().load().type(Repository.class).list();
    }

    /**
     * Searches for the repository with the given tag.
     *
     * @param tag tag name
     * @return found repository or null
     */
    public Repository findRepository(String tag) {
        return ofy().load().key(Key.create(Repository.class, tag)).now();
    }

    /**
     * @param tag a tag to filter the package versions or null
     * @param order how to order the query (e.g. "-lastModifiedAt") or null
     * @return first 20 package versions with errors downloading the binary
     */
    public List<PackageVersion> find20PackageVersions(
            String tag, String order) {
        Query<PackageVersion> q = ofy().load().type(PackageVersion.class).
                limit(20);
        if (tag != null) {
            q = q.filter("tags ==", tag);
        }
        if (order != null) {
            q = q.order(order);
        }
        return q.list();
    }

    /**
     * Searches for a package version.
     *
     * @param packageName full package name
     * @param v version number
     * @return found version or null
     */
    public PackageVersion findPackageVersion(String packageName, String v) {
        return ofy().load().key(Key.create(PackageVersion.class,
                packageName + "@" + v)).now();
    }

    /**
     * @param package_ package ID
     * @return sorted versions (1.1, 1.2, 1.3) for this package
     */
    public List<PackageVersion> getSortedVersions(String package_) {
        List<PackageVersion> versions = ofy().load().type(PackageVersion.class)
                .filter("package_ =", package_).list();
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
     * Searches for a license with the given full license ID.
     *
     * @param id full license ID
     * @return found license or null
     */
    public License findLicense(String id) {
        return ofy().load().key(Key.create(License.class, id)).now();
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
            ret = ofy().load().key(Key.create(License.class, id)).
                    now();

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
        return ofy().load().key(Key.create(
                PackageVersion.class, id)).now();
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
        ofy().delete().entity(p);
    }

    public Package findNextPackage(Package p) {
        List<Package> ps =
                ofy().load().type(Package.class).limit(5).filter("title >=",
                        p.title).order("title").list();

        Package next = null;

        // find the next package
        for (int i = 0; i < ps.size() - 1; i++) {
            Package n = ps.get(i);

            if (n.name.equals(p.name)) {
                next = ps.get(i + 1);
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
        List<Editor> editors = ofy().load().type(Editor.class).filter(
                "id =", id).limit(1).list();
        if (editors.size() > 0) {
            return editors.get(0);
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
        Objectify ofy = ofy();
        ofy.delete().key(Key.create(Repository.class, id));
        incDataVersion();
    }

    /**
     * @param id package ID
     * @return all versions for the package
     */
    public Iterable<PackageVersion> getPackageVersions(String id) {
        return ofy().load().type(PackageVersion.class)
                .filter("package_ =", id).list();
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
