package com.googlecode.npackdweb;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Page;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;

public class RecentRepXMLPage extends Page {

    private String user;
    private String tag;

    /**
     * @param user email address or null
     * @param tag tag for package versions to filter or null
     */
    public RecentRepXMLPage(String user, String tag) {
        this.user = user;
        this.tag = tag;
    }

    @Override
    public void create(HttpServletRequest request, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/xml");

        final String key =
                this.getClass().getName() + "@" + NWUtils.dsCache.
                getDataVersion() +
                "@" + user + "@" + tag;
        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers
                .getConsistentLogAndContinue(Level.INFO));
        byte[] value = (byte[]) syncCache.get(key); // read from cache
        if (value == null) {
            NWUtils.LOG.warning("Found no value in cache");
            try {
                DatastoreService datastore = DatastoreServiceFactory.
                        getDatastoreService();

                com.google.appengine.api.datastore.Query query =
                        new com.google.appengine.api.datastore.Query(
                                "PackageVersion");
                if (user != null) {
                    query.setFilter(
                            new com.google.appengine.api.datastore.Query.FilterPredicate(
                                    "lastModifiedBy",
                                    com.google.appengine.api.datastore.Query.FilterOperator.EQUAL,
                                    new User(user, user.substring(user
                                            .indexOf('@')))));
                }
                if (tag != null) {
                    query.setFilter(
                            new com.google.appengine.api.datastore.Query.FilterPredicate(
                                    "tags",
                                    com.google.appengine.api.datastore.Query.FilterOperator.EQUAL,
                                    tag));
                }
                query.addSort("-lastModifiedAt");

                PreparedQuery pq = datastore.prepare(query);

                final List<Entity> list =
                        pq.asList(FetchOptions.Builder.withLimit(20));

                ArrayList<PackageVersion> res = new ArrayList<>();
                for (Entity e : list) {
                    res.add(new PackageVersion(e));
                }

                Document d = RepXMLPage.toXML(res, false, null);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                NWUtils.serializeXML(d, baos);
                value = baos.toByteArray();
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
}
