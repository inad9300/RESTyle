package es.berry.restyle.utils;

import java.util.List;

public class Strings {

    public static String join(List<String> strings, String separator) {
        final int size = strings.size();
        final int lastElem = size - 1;

        String result = "";

        for (int i = 0; i < size; i++)
            result += i != lastElem ? strings.get(i) + separator : strings.get(i);

        return result;
    }

    public static String list(List<String> strings) {
        return list(strings, "-");
    }

    public static String list(List<String> strings, String listingSymbol) {
        final int size = strings.size();

        String result = "";

        for (int i = 0; i < size; ++i)
            result += listingSymbol + " " + strings.get(i) + "\n";

        return result;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

}
