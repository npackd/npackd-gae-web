package com.googlecode.npackdweb.db;

import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.EntityCounter;
import com.googlecode.npackdweb.EntityCounterShard;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import static com.googlecode.objectify.ObjectifyService.ofy;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Unindex;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;

/**
 * Stores a count in shards.
 *
 * From http://thoughtsofthree.com/2011/03/implementing-a-sharded-counter-using-
 * objectify/
 */
@Entity
@Index
public class ShardedCounter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Ignore
    private transient ObjectifyFactory of;

    @Unindex
    private String name;

    private ShardedCounter() {
        of = ObjectifyService.factory();
    }

    private ShardedCounter(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void increment() {
        increment(1);
    }

    public void increment(int add) {
        // fetch the counter to determine the number of shards
        EntityCounter counter = ofy().load().key(Key.create(
                EntityCounter.class, name)).now();

        // pick a random shard
        Random generator = new Random();
        int shardNum = generator.nextInt(counter.numShards);

        // get the shard from the datastore, increment its value by 'add' and
        // persist it if the shard was modified in the datastore between the get
        // and the persist, retry the operation
        ofy().transact(() -> {
            int triesLeft = 3;
            while (true) {
                try {

                    EntityCounterShard shard = ofy()
                            .load().key(Key.create(
                                            EntityCounterShard.class, name +
                                            shardNum)).now();

                    shard.value += add;

                    ofy().save().entity(shard);
                    break;
                } catch (ConcurrentModificationException e) {
                    if (triesLeft == 0) {
                        throw e;
                    }
                    --triesLeft;
                }
            }
            return null;
        });

    }

    public void decrement() {
        decrement(1);
    }

    public void decrement(int subs) {
        increment(subs * -1);
    }

    public void addShard() {
        addShards(1);
    }

    public void addShards(int newShards) {
        int[] nextShardNumber = {-1};
        EntityCounter[] counter = {null};

        ofy().transact(() -> {

            // fetch the counter to determine the number of existing shards
            // and increment the shard count
            int tries = 3;
            while (true) {
                try {
                    counter[0] = ofy().load().key(Key.create(
                            EntityCounter.class, name)).now();
                    nextShardNumber[0] = counter[0].numShards;
                    counter[0].numShards += newShards;
                    ofy().save().entity(counter[0]);
                } catch (ConcurrentModificationException e) {
                    if (tries == 0) {
                        throw e;
                    }
                    --tries;
                }
                break;
            }
            return null;
        });

        // by increasing counter.numShards, this thread reserved
        // a shard 'range', so this thread is the only one
        // that could add shards with the shard numbers we're about to add:
        // add newShard number shards
        int shardsAdded = 0;
        Objectify ofy = of.begin();
        while (shardsAdded < newShards) {
            EntityCounterShard newShard = new EntityCounterShard(counter[0],
                    nextShardNumber[0]);
            ofy().save().entity(newShard);
            shardsAdded++;
            nextShardNumber[0]++;
        }
    }

    public int getCount() {
        EntityCounter counter = ofy().load().key(Key.create(
                EntityCounter.class, name)).now();

        List<Key<EntityCounterShard>> shardKeys =
                new ArrayList<>();
        for (int shard = 0; shard < counter.numShards; shard++) {
            shardKeys.add(Key.create(EntityCounterShard.class,
                    String.format("%s%d", name, shard)));
        }

        Collection<EntityCounterShard> shards = ofy().load().keys(shardKeys).
                values();
        int count = 0;
        for (EntityCounterShard shard : shards) {
            count += shard.value;
        }

        return count;
    }

    public static ShardedCounter getOrCreateCounter(String name, int numShards) {
        ShardedCounter shardedCounter = new ShardedCounter(name);
        ofy().transact(() -> {
            int tries = 3;
            while (true) {
                try {
                    EntityCounter counter = ofy().load().key(Key.create(
                            EntityCounter.class, name)).now();
                    if (counter == null) {
                        // create new counter
                        counter = new EntityCounter(name);
                        ofy().save().entity(counter);
                        shardedCounter.addShards(numShards);
                    }

                    break;
                } catch (ConcurrentModificationException e) {
                    if (tries == 0) {
                        throw e;
                    }
                    --tries;
                }
            }
            return null;
        });
        return shardedCounter;
    }

    public static ShardedCounter createCounter(String name, int numShards) {
        ShardedCounter shardedCounter = new ShardedCounter(name);
        ofy().transact(() -> {
            int tries = 3;
            while (true) {
                try {
                    EntityCounter counter = ofy().load().key(Key.create(
                            EntityCounter.class, name)).now();
                    if (counter == null) {
                        // create new counter
                        counter = new EntityCounter(name);
                        ofy().save().entity(counter);
                        shardedCounter.addShards(numShards);
                    } else {
                        throw new IllegalArgumentException(
                                "A counter with name " +
                                name + " does already exist!");
                    }

                    break;
                } catch (ConcurrentModificationException e) {
                    if (tries == 0) {
                        throw e;
                    }
                    --tries;
                }
            }
            return null;
        });
        return shardedCounter;
    }

    public static ShardedCounter getCounter(String name) {
        Objectify ofy = DefaultServlet.getObjectify();
        if (ofy.load().key(Key.create(EntityCounter.class, name)).now() != null) {
            return new ShardedCounter(name);
        }
        return null;
    }
}
