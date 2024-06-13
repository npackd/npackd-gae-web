package com.googlecode.npackdweb.api;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.npackdweb.wlib.Page;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RecentRepXMLPage extends Page {

    private final String package_;
    private final boolean extra;
    private final String user;
    private final String tag;

    /**
     * @param user email address or null
     * @param tag tag for package versions to filter or null
     * @param package_ only return this package or null
     * @param extra export non-standard fields
     */
    public RecentRepXMLPage(String user, String tag, String package_,
                            boolean extra) {
        this.user = user;
        this.tag = tag;
        this.package_ = package_;
        this.extra = extra;
    }

    @Override
    public void create(HttpServletRequest request, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/xml");

        final String key =
                this.getClass().getName() + "@" + NWUtils.dsCache.
                        getDataVersion() +
                        "@" + user + "@" + tag + "@" + package_;
        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers
                .getConsistentLogAndContinue(Level.INFO));
        byte[] value = (byte[]) syncCache.get(key); // read from cache
        if (value == null) {
            NWUtils.LOG.warning("Found no value in cache");
            try {
                DatastoreService datastore = DatastoreServiceFactory.
                        getDatastoreService();

                final Query query = getQuery();

                // we assume there are not so many package versions in one
                // package
                if (package_ == null) {
                    query.addSort("lastModifiedAt",
                            Query.SortDirection.DESCENDING);
                }

                PreparedQuery pq = datastore.prepare(query);

                final List<Entity> list;

                // do not apply limits if only one package is requested
                FetchOptions fetchOptions;
                if (package_ == null) {
                    fetchOptions = FetchOptions.Builder.withLimit(40);
                } else {
                    fetchOptions =
                            FetchOptions.Builder.withDefaults();
                }

                list = pq.asList(fetchOptions);

                ArrayList<PackageVersion> res = new ArrayList<>();
                for (Entity e : list) {
                    res.add(new PackageVersion(e));
                }

                HTMLWriter d = RepXMLPage.toXML2(res, false, null, extra);
                value = d.getContent().toString()
                        .getBytes(StandardCharsets.UTF_8);

                syncCache.put(key, value); // populate cache
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else {
            NWUtils.LOG.log(Level.WARNING, "Found value in cache {0} bytes",
                    value.length);
        }

        ServletOutputStream ros = resp.getOutputStream();
        ros.write(value);
        ros.close();
    }

    private Query getQuery() {
        Query query =
                new Query(
                        "PackageVersion");
        if (user != null) {
            query.setFilter(
                    new Query.FilterPredicate(
                            "lastModifiedBy",
                            Query.FilterOperator.EQUAL,
                            new User(user, user.substring(user
                                    .indexOf('@')))));
        }
        if (tag != null) {
            query.setFilter(
                    new Query.FilterPredicate(
                            "tags",
                            Query.FilterOperator.EQUAL,
                            tag));
        }
        if (package_ != null) {
            query.setFilter(
                    new Query.FilterPredicate(
                            "package_",
                            Query.FilterOperator.EQUAL,
                            package_));
        }
        return query;
    }
}
