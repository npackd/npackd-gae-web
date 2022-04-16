/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.npackdweb;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

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

    @Test
    /**
     * how to convert Date to LocalDate
     */
    public void testTime() {
        long days = ChronoUnit.DAYS.between(
                LocalDate.from(new Date().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()),
                LocalDate.now(ZoneId.of("Europe/Berlin"))
        );
        System.out.println(days + " days");
    }

    @Test
    /**
     * .validateRelativePath()
     */
    public void testValidateRelativePath() {
        assertNull(NWUtils.validateRelativePath("abc/def.exe"));
        assertNull(NWUtils.validateRelativePath("\\abc\\def.exe"));

        assertNotNull(NWUtils.validateRelativePath("..\\abc\\def.exe"));
        assertNotNull(NWUtils.validateRelativePath(""));
        assertNotNull(NWUtils.validateRelativePath(">"));
        assertNotNull(NWUtils.validateRelativePath("<"));
        assertNotNull(NWUtils.validateRelativePath("C:\\test.abc"));
        assertNotNull(NWUtils.validateRelativePath("LPT1:"));
        assertNotNull(NWUtils.validateRelativePath("\\LPT2"));
        assertNotNull(NWUtils.validateRelativePath("\nfilename\r"));
    }
}
