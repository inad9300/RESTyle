package es.berry.restyle.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Strings {

    public static String surround(String str, String wrapper) {
        return wrapper + str + wrapper;
    }

    public static String surround(String str, String leftWrapper, String rightWrapper) {
        return leftWrapper + str + rightWrapper;
    }


    public static String join(Collection<String> strings, String separator) {
        final int lastElem = strings.size() - 1;

        int i = 0;
        String result = "";

        for (String str : strings)
            result += (i++ != lastElem) ? str + separator : str;

        return result;
    }


    public static String cut(String str, int maxLen) {
        return str.length() <= maxLen ? str : str.substring(0, maxLen - 3) + "...";
    }


    public static String list(List<String> strings) {
        return list(strings, "-");
    }

    public static String list(Collection<String> strings, String listingSymbol) {
        String result = "";

        for (String str : strings)
            result += listingSymbol + " " + str + "\n";

        return result;
    }


    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }


    public static void toFile(String str, String filename) throws IOException {
        File file = new File(filename);

        boolean result = file.createNewFile();
        if (!result)
            throw new FileAlreadyExistsException(filename);

        PrintWriter writer = new PrintWriter(new FileWriter(file, true));
        writer.append(str);
        writer.close();
    }
}
