package com.googlecode.npackdweb.db;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import static com.googlecode.objectify.ObjectifyService.ofy;
import java.util.ArrayList;
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
     * @return found license or null
     */
    public License getLicense(String id) {
        License ret = null;

        lock.lock();
        try {
            ret = licensesCache.get(id);
        } finally {
            lock.unlock();
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
