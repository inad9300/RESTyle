package es.berry.restyle.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NumbersTest {

    @Test
    public void getLong() {
        final int a = 3;
        final Integer b = 7;
        final String c = "44";

        final long aL = Numbers.getLong(a);
        final long bL = Numbers.getLong(b);
        final long cL = Numbers.getLong(c);

        assertEquals(aL, 3);
        assertEquals(bL, 7);
        assertEquals(cL, 44);
    }

    @Test(expected = NumberFormatException.class)
    public void getWrongLong() {
        Numbers.getLong("AOB");
    }
}
