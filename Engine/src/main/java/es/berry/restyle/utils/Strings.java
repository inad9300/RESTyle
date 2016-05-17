package es.berry.restyle.utils;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;

/**
 * Utility class for String-related operations.
 */
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

    public static String join(Collection<String> strings, String separator, boolean ignoreEmpties) {
        if (ignoreEmpties)
            strings = Strings.removeEmpty(strings);

        return join(strings, separator);
    }

    public static String join(String separator, Collection<Object> objects) {
        Collection<String> strings = new ArrayList<>();

        for (Object o : objects)
            if (o instanceof String)
                strings.add((String) o);

        return join(strings, separator);
    }


    public static Collection<String> removeEmpty(Collection<String> strings) {
        Collection<String> cleanStrings = new ArrayList<>();

        for (String str : strings)
            if (!Strings.isEmpty(str))
                cleanStrings.add(str);

        return cleanStrings;
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


    public static void toFile(String content, String filename, boolean override) throws IOException {
        File file = new File(filename);

        boolean result = file.createNewFile();
        if (!result && !override)
            throw new FileAlreadyExistsException(filename);

        PrintWriter writer = new PrintWriter(new FileWriter(file, !override)); // Negate "override" to find the corresponding meaning for "append"
        writer.append(content);
        writer.close();
    }

    public static void toFile(String content, String filename) throws IOException {
        toFile(content, filename, true);
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


    public static String ucFirst(String str) {
        return ucFirst(str, false);
    }

    public static String ucFirst(String str, boolean lowerCaseRest) {
        final String firstChar = str.substring(0, 1).toUpperCase();
        String rest = str.substring(1);
        if (lowerCaseRest)
            rest = rest.toLowerCase();

        return firstChar + rest;
    }


    /**
     * Port of the Str::studly() method implemented by Laravel. Original code (see
     * https://github.com/laravel/framework/blob/5.2/src/Illuminate/Support/Str.php):
     * <p>
     * public static function studly($value) {
     * $key = $value;
     * <p>
     * if (isset(static::$studlyCache[$key])) {
     * return static::$studlyCache[$key];
     * }
     * <p>
     * $value = ucwords(str_replace(['-', '_'], ' ', $value));
     * <p>
     * return static::$studlyCache[$key] = str_replace(' ', '', $value);
     * }
     */
    public static String studly(String value) {
        value = value.replaceAll("-", " ").replaceAll("_", " ");

        // Simplification of ucwords' " \t\r\n\f\v" delimiters. Should be enough for altering method names.
        List<String> words = Arrays.asList(value.split("\\s+"));

        for (int i = 0; i < words.size(); ++i)
            words.set(i, Strings.ucFirst(words.get(i), false));

        value = Strings.join(words, "");

        return value.replaceAll(" ", "");
    }


    public static List<String> iteratorToList(Iterator<String> itr) {
        final List<String> list = new ArrayList<>();
        while (itr.hasNext())
            list.add(itr.next());

        return list;
    }
}
