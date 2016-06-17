package es.berry.restyle.utils;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class StringsTest {
    private static final List<String> list = Arrays.asList("abc", "def");
    private static final List<String> listWithNull = Arrays.asList("abc", null, "def");
    private static final List<String> listWithEmpties = Arrays.asList("abc", null, "", "def");
    private static final List<Object> listWithNonStrings = Arrays.asList("abc", 123, "def");
    private static final Iterator<String> listItr = Arrays.asList("abc", "def").iterator();
    private static final String fileContent = "File content\nwith\nsome\nlines";

    private static File tmpFolder = null;

    @Before
    public void setUp() {
        try {
            tmpFolder = File.createTempFile("__junit_test_folder__", Long.toString(System.nanoTime()));
            if (!tmpFolder.delete()) // Remove the file
                throw new IOException("Could not delete temporary file");
            if (!tmpFolder.mkdirs()) // Create the same File as a directory
                throw new IOException("Could not create temporary directory");
        } catch (IOException e) {
            assertFalse("Temporary folder creation failed", true);
        }
    }

    @Test
    public void surround() {
        assertEquals("-abc-", Strings.surround("abc", "-"));
        assertEquals("-abc_", Strings.surround("abc", "-", "_"));
    }

    @Test
    public void join() {
        assertEquals("abc,def", Strings.join(list, ","));
        assertEquals("abc,def", Strings.join(listWithNull, ",", true));
        assertEquals("abc,def", Strings.join(",", listWithNonStrings));
    }

    @Test
    public void removeEmpty() {
        assertEquals(list, Strings.removeEmpty(listWithEmpties));
    }

    @Test
    public void cut() {
        assertEquals("abcdef", Strings.cut("abcdef", 6));
        assertEquals("abc...", Strings.cut("abcdefghi", 6));
    }

    @Test
    public void list() {
        assertEquals("- abc\n- def\n", Strings.list(list));
        assertEquals("* abc\n* def\n", Strings.list(list, "*"));
    }

    @Test
    public void isEmpty() {
        assertTrue(Strings.isEmpty(""));
        assertTrue(Strings.isEmpty(null));
        assertFalse(Strings.isEmpty("abc"));
    }

    @Test
    public void toFile() {
        try {
            final String filename = tmpFolder.getAbsolutePath() + File.separator
                    + "__junit_test_file__" + Long.toString(System.nanoTime()) + ".tmp";

            Strings.toFile(fileContent, filename);
            assertEquals(fileContent, Strings.fromFile(new File(filename)));

            Strings.toFile(fileContent + "_mod_", filename);
            assertEquals(fileContent + "_mod_", Strings.fromFile(new File(filename)));
        } catch (IOException e) {
            assertFalse("Failure creating a file from a string", true);
        }
    }

    @Test
    public void fromFile() {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("__junit_test_file__" + Long.toString(System.nanoTime()), ".tmp");
            BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
            bw.write(fileContent);
            bw.close();
        } catch (IOException e) {
            assertFalse("Temporary file creation failed", true);
        }

        try {
            assertEquals(fileContent, Strings.fromFile(tmpFile));
        } catch (IOException e) {
            assertFalse("Failure creating a string from a file", true);
        }
    }

    @Test // NOTE: "(expected = FileAlreadyExistsException.class)" apparently doesn't work for non-Runtime exceptions
    public void toFileFailure() {
        final String filename = tmpFolder.getAbsolutePath() + File.separator + "sameFile.tmp";
        boolean thrown = false;

        try {
            Strings.toFile(fileContent, filename, true); // Override
            Strings.toFile("new content", filename, false); // Don't override => exception
        } catch (FileAlreadyExistsException e) {
            thrown = true;
        } catch (IOException e) {
            assertFalse("Unexpected IOException thrown", true);
        }
        assertTrue("FileAlreadyExistsException thrown", thrown);
    }

    @Test
    public void fromException() {
        final int minSize = 30;
        assertTrue(
                "String representing an exception should be bigger than " + minSize,
                Strings.fromException(new Exception()).length() >= minSize
        );
    }

    @Test
    public void ucFirst() {
        assertEquals("Abc", Strings.ucFirst("abc"));
        assertEquals("Abc", Strings.ucFirst("Abc"));
        assertEquals("ABC", Strings.ucFirst("aBC"));
        assertEquals("Abc", Strings.ucFirst("aBC", true));
    }

    @Test
    public void studly() {
        assertEquals("CamelCase", Strings.studly("camel_case"));
        assertEquals("CamelCase", Strings.studly("camelCase"));
    }

    @Test
    public void dashify() {
        assertEquals("camel-case", Strings.dashify("CamelCase"));
        assertEquals("camel-case", Strings.dashify("camelCase"));
        assertEquals("camel-case", Strings.dashify("camel-case"));
        assertEquals("camel-case", Strings.dashify("camel-Case"));
    }

    @Test
    public void iteratorToList() {
        assertEquals(list, Strings.iteratorToList(listItr));
    }

    @Test
    public void streamToString() throws IOException {
        assertEquals("abc", Strings.fromStream(new ByteArrayInputStream("abc".getBytes("UTF-8"))));
    }
}