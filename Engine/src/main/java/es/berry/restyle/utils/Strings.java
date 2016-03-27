package es.berry.restyle.utils;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.Collection;
import java.util.List;

final public class Strings {

    public static String surround(String str, String wrapper) {
        return wrapper + (str == null ? "" : str) + wrapper;
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


    public static void toFile(String str, String filename, boolean override) throws IOException {
        File file = new File(filename);

        boolean result = file.createNewFile();
        if (!result && !override)
            throw new FileAlreadyExistsException(filename);

        PrintWriter writer = new PrintWriter(new FileWriter(file, !override)); // Negate "override" to find the corresponding meaning for "append"
        writer.append(str);
        writer.close();
    }

    public static void toFile(String str, String filename) throws IOException {
        toFile(str, filename, true);
    }


    // This method is not recommended for big files
    public static String fromFile(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[(int) f.length()];
        fis.read(data);
        fis.close();

        return new String(data, "UTF-8");
    }


    public static String fromException(Exception e) {
        final StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }
}
