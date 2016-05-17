package es.berry.restyle.utils;

/**
 * Utility class for number-related operations.
 */
final public class Numbers {

    public static long getLong(Object n) {
        if (n instanceof Integer)
            return ((Integer) n).longValue();
        else if (n instanceof String)
            return Long.parseLong((String) n);

        return (long) n;
    }
}
