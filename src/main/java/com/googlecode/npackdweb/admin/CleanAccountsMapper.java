package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.Mapper;

public class CleanAccountsMapper extends Mapper<Entity, Void, Void> {

	private transient DatastoreMutationPool pool;

	@Override
	public void beginShard() {
		// Ikai gives examples of using the pool for
		// better parallel datastore operations
		// TODO: this.pool = DatastoreMutationPool.forWorker(this);
		// You could optionally use the normal datastore like this
		// this.datastore =
		// DatastoreServiceFactory.getDatastoreService();
	}

	@Override
	public void map(Entity value) {

		// During my testing, some of our workers
		// died with NullPointer's on this field,
		// as if in some circumstances it goes away.
		// This ensures we always have it.
		/*
		 * if (this.pool == null) { this.pool =
		 * DatastoreMutationPool.forWorker(this); }
		 * 
		 * // Slightly paranoid check we have an account if
		 * (value.getKind().equals("Account")) { // Logic goes here to determine
		 * // if the account should be deleted // if (itShouldBeDeleted) { //
		 * pool.delete(value.getKey()); // } // You could create/update/count
		 * here too }
		 */
	}
}