package com.googlecode.npackdweb;

import com.googlecode.npackdweb.db.Version;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Tests for the Version class.
 */
public class VersionTest {

    @Test
    /**
     * Tests the normalize() method
     */
    public void testNormalize() {
        Version v = Version.parse("1.0");
        v.normalize();
        assertEquals("1", v.toString());

        v = Version.parse("123.0.0.0");
        v.normalize();
        assertEquals("123", v.toString());

        v = Version.parse("17.2.0.0");
        v.normalize();
        assertEquals("17.2", v.toString());

        v = Version.parse("178");
        v.normalize();
        assertEquals("178", v.toString());

        v = Version.parse("178.12");
        v.normalize();
        assertEquals("178.12", v.toString());
    }
}
