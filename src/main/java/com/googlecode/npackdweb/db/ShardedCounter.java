package com.googlecode.npackdweb.db;

import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.EntityCounter;
import com.googlecode.npackdweb.EntityCounterShard;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Unindexed;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;
import javax.persistence.Transient;

/**
 * Stores a count in shards.
 *
 * From http://thoughtsofthree.com/2011/03/implementing-a-sharded-counter-using-
 * objectify/
 */
public class ShardedCounter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Transient
    private transient ObjectifyFactory of;

    @Unindexed
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
        Objectify ofy = of.begin();

        // fetch the counter to determine the number of shards
        EntityCounter counter = ofy.get(new Key<>(
                EntityCounter.class, name));

        // pick a random shard
        Random generator = new Random();
        int shardNum = generator.nextInt(counter.numShards);

        // get the shard from the datastore, increment its value by 'add' and
        // persist it if the shard was modified in the datastore between the get
        // and the persist, retry the operation
        Objectify trans = of.beginTransaction();
        int triesLeft = 3;
        while (true) {
            try {

                EntityCounterShard shard = trans
                        .get(new Key<>(
                                EntityCounterShard.class, name + shardNum));

                shard.value += add;

                trans.put(shard);
                trans.getTxn().commit();
                break;
            } catch (ConcurrentModificationException e) {
                if (triesLeft == 0) {
                    throw e;
                }
                --triesLeft;
            } finally {
                if (trans.getTxn().isActive()) {
                    trans.getTxn().rollback();
                }
            }
        }
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
        Objectify counterTrans = of.beginTransaction();
        int nextShardNumber = -1;
        EntityCounter counter = null;

        // fetch the counter to determine the number of existing shards
        // and increment the shard count
        int tries = 3;
        while (true) {
            try {
                counter = counterTrans.get(new Key<>(
                        EntityCounter.class, name));
                nextShardNumber = counter.numShards;
                counter.numShards += newShards;
                counterTrans.put(counter);
                counterTrans.getTxn().commit();
            } catch (ConcurrentModificationException e) {
                if (tries == 0) {
                    throw e;
                }
                --tries;
            } finally {
                if (counterTrans.getTxn().isActive()) {
                    counterTrans.getTxn().rollback();
                }
            }
            break;
        }

        // by increasing counter.numShards, this thread reserved
        // a shard 'range', so this thread is the only one
        // that could add shards with the shard numbers we're about to add:
        // add newShard number shards
        int shardsAdded = 0;
        Objectify ofy = of.begin();
        while (shardsAdded < newShards) {
            EntityCounterShard newShard = new EntityCounterShard(counter,
                    nextShardNumber);
            ofy.put(newShard);
            shardsAdded++;
            nextShardNumber++;
        }
    }

    public int getCount() {
        Objectify ofy = of.begin();
        EntityCounter counter = ofy.get(new Key<>(
                EntityCounter.class, name));

        List<Key<EntityCounterShard>> shardKeys =
                new ArrayList<>();
        for (int shard = 0; shard < counter.numShards; shard++) {
            shardKeys.add(new Key<>(EntityCounterShard.class,
                    String.format("%s%d", name, shard)));
        }

        Collection<EntityCounterShard> shards = ofy.get(shardKeys).values();
        int count = 0;
        for (EntityCounterShard shard : shards) {
            count += shard.value;
        }

        return count;
    }

    public static ShardedCounter getOrCreateCounter(String name, int numShards) {
        ShardedCounter shardedCounter = new ShardedCounter(name);
        Objectify trans = ObjectifyService.beginTransaction();
        int tries = 3;
        while (true) {
            try {
                EntityCounter counter = trans.find(new Key<>(
                        EntityCounter.class, name));
                if (counter == null) {
                    // create new counter
                    counter = new EntityCounter(name);
                    trans.put(counter);
                    trans.getTxn().commit();
                    shardedCounter.addShards(numShards);
                }

                break;
            } catch (ConcurrentModificationException e) {
                if (tries == 0) {
                    throw e;
                }
                --tries;
            } finally {
                if (trans.getTxn().isActive()) {
                    trans.getTxn().rollback();
                }
            }
        }
        return shardedCounter;
    }

    public static ShardedCounter createCounter(String name, int numShards) {
        ShardedCounter shardedCounter = new ShardedCounter(name);
        Objectify trans = ObjectifyService.beginTransaction();
        int tries = 3;
        while (true) {
            try {
                EntityCounter counter = trans.find(new Key<>(
                        EntityCounter.class, name));
                if (counter == null) {
                    // create new counter
                    counter = new EntityCounter(name);
                    trans.put(counter);
                    trans.getTxn().commit();
                    shardedCounter.addShards(numShards);
                } else {
                    throw new IllegalArgumentException("A counter with name " +
                             name + " does already exist!");
                }

                break;
            } catch (ConcurrentModificationException e) {
                if (tries == 0) {
                    throw e;
                }
                --tries;
            } finally {
                if (trans.getTxn().isActive()) {
                    trans.getTxn().rollback();
                }
            }
        }
        return shardedCounter;
    }

    public static ShardedCounter getCounter(String name) {
        Objectify ofy = DefaultServlet.getObjectify();
        if (ofy.find(new Key<>(EntityCounter.class, name)) != null) {
            return new ShardedCounter(name);
        }
        return null;
    }
}
