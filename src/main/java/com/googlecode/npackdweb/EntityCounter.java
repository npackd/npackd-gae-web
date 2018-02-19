package com.googlecode.npackdweb;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;

/**
 * EntityCounter is stored in the data store to reserve a counter name and
 * remember how many shards it has.
 */
@Entity
public class EntityCounter {
	@Id
	public String name;

	/** number of shards in this counter */
	public int numShards;

	@SuppressWarnings("unused")
	private EntityCounter() {
	}

	/**
	 * -
	 * 
	 * @param name
	 *            name for this counter
	 */
	public EntityCounter(String name) {
		this.name = name;
	}

	public Key<EntityCounterShard> keyForShard(int shardNumber) {
		return new Key<>(EntityCounterShard.class,
		        shardName(shardNumber));
	}

	public String shardName(int shardNumber) {
		return String.format("%s%d", name, shardNumber);
	}
}