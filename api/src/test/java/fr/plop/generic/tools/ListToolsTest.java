package fr.plop.generic.tools;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListToolsTest {


    @Test
    public void testAdded() {
        assertEquals(ListTools.added(List.of(1, 2, 3), List.of(2, 3, 5, 6)), List.of(5, 6));
    }

    @Test
    public void testRemoved() {
        assertEquals(ListTools.removed(List.of(1, 2, 3, 4), List.of(2, 3)), List.of(1, 4));
    }


}