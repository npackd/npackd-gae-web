package com.googlecode.npackdweb.db;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DatastoreCacheTest {
    @Test
    public void escape() {
        // single characters
        assertEquals("\\0", DatastoreCache.escape("\0"));
        assertEquals("\\n", DatastoreCache.escape("\n"));
        assertEquals("\\r", DatastoreCache.escape("\r"));
        assertEquals("\\\\", DatastoreCache.escape("\\"));
        assertEquals("\\'", DatastoreCache.escape("'"));
        assertEquals("\\\"", DatastoreCache.escape("\""));
        assertEquals("\\Z", DatastoreCache.escape("\u001a"));
        assertEquals("\\b", DatastoreCache.escape("\b"));

        // simple string
        assertEquals("asdl lsdkjf askldfj laskfj", DatastoreCache.escape("asdl lsdkjf askldfj laskfj"));

        assertEquals("\\\"what a sldkjf\\\\, sdjk\\'\\\"", DatastoreCache.escape("\"what a sldkjf\\, sdjk'\""));
    }
}
