package es.berry.restyle.utils;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;

/**
 * Utility class for String-related operations.
 */
final public class Strings {

    /**
     * Surround a string with another (usually of length one)
     */
    public static String surround(String str, String wrapper) {
        return wrapper + (str == null ? "" : str) + wrapper;
    }

    /**
     * Surround a string with another two, one on the left, one on the right.
     */
    public static String surround(String str, String leftWrapper, String rightWrapper) {
        return leftWrapper + str + rightWrapper;
    }


    /**
     * Concatenate a group of strings together with some "glue".
     */
    public static String join(Collection<String> strings, String separator) {
        final int lastElem = strings.size() - 1;

        int i = 0;
        String result = "";

        for (String str : strings)
            result += (i++ != lastElem) ? str + separator : str;

        return result;
    }

    /**
     * Special case to join a group of strings that ignores the empty strings on that group.
     */
    public static String join(Collection<String> strings, String separator, boolean ignoreEmpties) {
        if (ignoreEmpties)
            strings = Strings.removeEmpty(strings);

        return join(strings, separator);
    }

    /**
     * Another version to join a group of "things", taking only the strings from it.
     */
    public static String join(String separator, Collection<Object> objects) {
        Collection<String> strings = new ArrayList<>();

        for (Object o : objects)
            if (o instanceof String)
                strings.add((String) o);

        return join(strings, separator);
    }


    /**
     * Given a collection of strings, return another one without empty strings.
     */
    public static Collection<String> removeEmpty(Collection<String> strings) {
        Collection<String> cleanStrings = new ArrayList<>();

        for (String str : strings)
            if (!Strings.isEmpty(str))
                cleanStrings.add(str);

        return cleanStrings;
    }


    /**
     * Forces a string to fit in a fixed number of characters, appending an ellipsis if it is longer.
     */
    public static String cut(String str, int maxLen) {
        return str.length() <= maxLen ? str : str.substring(0, maxLen - 3) + "...";
    }


    /**
     * Build a visual list from a group of strings.
     */
    public static String list(Collection<String> strings, String listingSymbol) {
        String result = "";

        for (String str : strings)
            result += listingSymbol + " " + str + "\n";

        return result;
    }

    /**
     * Overload the list() function to build lists based on the "-" symbol by default.
     */
    public static String list(List<String> strings) {
        return list(strings, "-");
    }


    /**
     * Determine if a string is empty, meaning it is null or it has a size equals to 0.
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }


    /**
     * Easy way to save a string into a file in the filesystem, allowing to control if existing files should be
     * overwritten.
     */
    public static void toFile(String content, String filename, boolean override) throws IOException {
        File file = new File(filename);

        boolean result = file.createNewFile();
        if (!result && !override)
            throw new FileAlreadyExistsException(filename);

        PrintWriter writer = new PrintWriter(new FileWriter(file, !override)); // Negate "override" to find the corresponding meaning for "append"
        writer.append(content);
        writer.close();
    }

    /**
     * Overload toFile() to override existing files by default.
     */
    public static void toFile(String content, String filename) throws IOException {
        toFile(content, filename, true);
    }

    /**
     * Retrieve a string based on a file's content. This function is not recommended for very big files.
     */
    public static String fromFile(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[(int) f.length()];
        fis.read(data);
        fis.close();

        return new String(data, "UTF-8");
    }


    /**
     * Build a nice string from a Java Exception.
     */
    public static String fromException(Exception e) {
        final StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }


    /**
     * Uppercase the first character of a string, also deciding about the case of the rest of the string.
     */
    public static String ucFirst(String str, boolean lowerCaseRest) {
        final String firstChar = str.substring(0, 1).toUpperCase();
        String rest = str.substring(1);
        if (lowerCaseRest)
            rest = rest.toLowerCase();

        return firstChar + rest;
    }

    /**
     * Overload ucFirst() to let untouched everything but the first character by default.
     */
    public static String ucFirst(String str) {
        return ucFirst(str, false);
    }


    /**
     * Convert a value from snake case to camel case, wrongly named as studly case by the Laravel team (see
     * https://en.wikipedia.org/wiki/Studly_caps). It is a port of the Str::studly() method implemented by Laravel.
     * <p>
     * Original code (see https://github.com/laravel/framework/blob/5.2/src/Illuminate/Support/Str.php#L428):
     * <p>
     * public static function studly($value) {
     * $key = $value;
     * if (isset(static::$studlyCache[$key])) return static::$studlyCache[$key];
     * $value = ucwords(str_replace(['-', '_'], ' ', $value));
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


    /**
     * Convert a Iterator of Strings into a List of Strings.
     */
    public static List<String> iteratorToList(Iterator<String> itr) {
        final List<String> list = new ArrayList<>();
        while (itr.hasNext())
            list.add(itr.next());

        return list;
    }


    /**
     * Convert an InputStream into a String.
     */
    public static String fromStream(InputStream inStream) throws IOException {
        final StringWriter writer = new StringWriter();
        IOUtils.copy(inStream, writer, "UTF-8");
        return writer.toString();
    }
}
