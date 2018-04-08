package com.googlecode.npackdweb.db;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.Version;
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
     * @return found packages
     */
    public List<Package> getPackages(List<String> ids) {
        List<Package> packages = new ArrayList<>();

        if (ids.size() > 0) {
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

            if (packages.size() == 0) {
                Objectify obj = ofy();
                List<Key<Package>> keys = new ArrayList<>();
                for (String id : ids) {
                    keys.add(Key.create(Package.class, id));
                }

                Map<Key<Package>, Package> map = obj.load().values(keys);

                lock.lock();
                try {
                    for (Map.Entry<Key<Package>, Package> e : map.entrySet()) {
                        Package p = e.getValue();
                        if (p != null) {
                            packages.add(p);
                            packagesCache.put(p.name, p);
                        }
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
            ret = ofy().load().key(Key.create(Package.class, id)).
                    now();

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
}
