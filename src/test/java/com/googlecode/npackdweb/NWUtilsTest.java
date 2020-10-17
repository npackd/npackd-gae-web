/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.npackdweb;

import java.util.regex.Pattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

    @Test
    public void analyzer() {
        Pattern p = Pattern.compile("\\d+(\\.\\d+){1,2}");
        assertTrue(p.matcher("1.2.3").matches());
    }

    @Test
    public void analyzeText() {
        assertEquals("rstudio", NWUtils.analyzeText("RStudio"));
        assertEquals("", NWUtils.analyzeText("windows"));
        assertEquals("c++", NWUtils.analyzeText("C++"));
        assertEquals("c", NWUtils.analyzeText("C+++"));
        assertEquals("c#", NWUtils.analyzeText("C#"));
        assertEquals("f#", NWUtils.analyzeText("F#"));
        assertEquals("c++", NWUtils.analyzeText("CPP"));
    }

}
