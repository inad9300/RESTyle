package es.berry.restyle.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

/**
 * Suite of integration tests.
 */
public class AppTest {
    private PrintStream realConsole;
    private ByteArrayOutputStream fakeConsole;
    private File tmpOut;

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Before
    public void setUp() {
        realConsole = System.out;
        fakeConsole = new ByteArrayOutputStream();
        System.setOut(new PrintStream(fakeConsole)); // Redirect output to a known, private variable

        try {
            tmpOut = File.createTempFile("__junit_test_folder__", Long.toString(System.nanoTime()));
            if (!tmpOut.delete()) // Remove the file
                throw new IOException("Could not delete temporary file");
            if (!tmpOut.mkdirs()) // Create the same File as a directory
                throw new IOException("Could not create temporary directory");
        } catch (IOException e) {
            assertFalse("Temporary folder creation failed", true);
        }
    }

    @After
    public void tearDown() {
        System.out.flush();
        System.setOut(realConsole);
    }

    @Test
    public void mainSuccessfulFlow() {
        final String[] args = {
                "-" + CommandOptions.SPEC_S, "/home/daniel/Code/RESTyle/Engine/src/main/resources/examples/bookstore.json",
                "-" + CommandOptions.PLUGINS_S, "MysqlCreationScript,PhpLumen",
                "-" + CommandOptions.OUT_S, tmpOut.getAbsolutePath()
        };
        App.main(args);
        assertTrue("The program ends, and the output directory exists and contains something",
                tmpOut.exists() && tmpOut.list().length > 0);
    }

    @Test
    public void askForHelp() {
        final String[] args = {"-" + CommandOptions.HELP_L};
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(() -> {
            String output = fakeConsole.toString().toLowerCase();
            // Some terms that we should find for sure...
            assertThat(output, containsString(Config.APP_CMD));
            assertThat(output, containsString("spec"));
            assertThat(output, containsString("help"));
            assertThat(output, containsString("plugins"));
            assertThat(output, containsString("output"));
        });
        App.main(args);
    }

    @Test
    public void noSpecArg() {
        final String[] args = {
                "-" + CommandOptions.PLUGINS_S, "MysqlCreationScript,PhpLumen",
                "-" + CommandOptions.OUT_S, tmpOut.getAbsolutePath()
        };
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            String output = fakeConsole.toString().toLowerCase();
            assertThat(output, containsString("error"));
            assertThat(output, containsString("required"));
            assertThat(output, containsString("required option: " + CommandOptions.SPEC_L));
        });
        App.main(args);
    }

    @Test
    public void noPluginsArg() {
        final String[] args = {
                "-" + CommandOptions.SPEC_S, "/home/daniel/Code/RESTyle/Engine/src/main/resources/examples/bookstore.json",
                "-" + CommandOptions.OUT_S, tmpOut.getAbsolutePath()
        };
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            String output = fakeConsole.toString().toLowerCase();
            assertThat(output, containsString("error"));
            assertThat(output, containsString("required option: " + CommandOptions.PLUGINS_L));
        });
        App.main(args);
    }

    @Test
    public void noOutputArg() {
        final String[] args = {
                "-" + CommandOptions.SPEC_S, "/home/daniel/Code/RESTyle/Engine/src/main/resources/examples/bookstore.json",
                "-" + CommandOptions.PLUGINS_S, "MysqlCreationScript,PhpLumen"
        };
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            String output = fakeConsole.toString().toLowerCase();
            assertThat(output, containsString("error"));
            assertThat(output, containsString("required option: " + CommandOptions.OUT_L));
        });
        App.main(args);
    }
}
