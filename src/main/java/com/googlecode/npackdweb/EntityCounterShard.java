package com.googlecode.npackdweb;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

/**
 * EntityCounterShard represents one shard and stores the result of a fraction
 * of the increment and decrement actions as its value.
 */
@Entity
public class EntityCounterShard {

    /**
     * id is equal to the counter name appended with the number of the shard
     */
    @Id
    public String name;

    @Unindex
    public int value;

    @SuppressWarnings("unused")
    private EntityCounterShard() {
    }

    public EntityCounterShard(EntityCounter counter, int shardNumber) {
        name = counter.shardName(shardNumber);
    }
}
