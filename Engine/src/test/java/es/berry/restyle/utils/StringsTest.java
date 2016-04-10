package es.berry.restyle.utils;

import org.junit.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class StringsTest {
    private static final List<String> list = Arrays.asList("abc", "def");

    @Test
    public void surround() {
        assertEquals(Strings.surround("abc", "-"), "-abc-");
        assertEquals(Strings.surround("abc", "-", "_"), "-abc_");
    }

    @Test
    public void join() {
        assertEquals(Strings.join(list, ","), "abc,def");
    }

    @Test
    public void cut() {
        assertEquals(Strings.cut("abcdef", 6), "abcdef");
        assertEquals(Strings.cut("abcdefghi", 6), "abc...");
    }

    @Test
    public void list() {
        assertEquals(Strings.list(list), "- abc\n- def\n");
        assertEquals(Strings.list(list, "*"), "* abc\n* def\n");
    }

    @Test
    public void isEmpty() {
        assertTrue(Strings.isEmpty(""));
        assertTrue(Strings.isEmpty(null));
        assertFalse(Strings.isEmpty("abc"));
    }

    @Test
    public void ucFirst() {
        assertEquals(Strings.ucFirst("abc"), "Abc");
        assertEquals(Strings.ucFirst("Abc"), "Abc");
        assertEquals(Strings.ucFirst("aBC"), "ABC");
    }
}