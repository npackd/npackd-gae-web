package com.googlecode.npackdweb;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Cache for Query results.
 */
public class QueryCache {
	/**
	 * Returns possible cached result of a query.
	 * 
	 * @param <T>
	 *            datastore object class
	 * @param ofy
	 *            Objectify
	 * @param q
	 *            a query
	 * @param cacheSuffix
	 *            suffix for the cache key
	 * @return possible cached result of the query
	 */
	public static <T> List<Key<T>> getKeys(Objectify ofy,
	        com.googlecode.objectify.Query<T> q, String cacheSuffix) {
		String key = q.toString() + cacheSuffix;

		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers
		        .getConsistentLogAndContinue(Level.INFO));
		String value = (String) syncCache.get(key); // read from cache
		List<Key<T>> result;
		if (value == null) {
			result = q.listKeys();

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < result.size(); i++) {
				if (i != 0)
					sb.append(' ');
				sb.append(result.get(i).getString());
			}
			value = sb.toString();

			syncCache.put(key, value); // populate cache
		} else {
			List<String> keys = NWUtils.split(value, ' ');
			result = new ArrayList<>();
			for (String k : keys) {
				result.add(new Key<T>(k));
			}
		}
		return result;
	}
}
