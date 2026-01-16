package fr.plop.generic.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringToolsAlmostEqualsTest {

    @Test
    public void test_false() {
        assertFalse(StringTools.almostEquals("one", "other"));
    }

    @Test
    public void testEquals() {
        assertTrue(StringTools.almostEquals("Hello", "Hello"));
    }

    @Test
    public void testEquals_lessOne() {
        assertTrue(StringTools.almostEquals("Hello", "helo"));
    }

    @Test
    public void testEquals_otherOne() {
        assertTrue(StringTools.almostEquals("Dupont", "dupond"));
    }

    @Test
    public void testFalse_2diffs() {
        assertFalse(StringTools.almostEquals("Dupont", "duond"));
    }



}