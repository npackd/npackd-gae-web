/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.npackdweb;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Tests for NWUtils.
 */
public class NWUtilsTest {

    @Test
    public void partition() {
        String parts[] = NWUtils.partition("http://www.example.com", "://");
        assertEquals("http", parts[0]);
        assertEquals("www.example.com", parts[1]);
    }
}
