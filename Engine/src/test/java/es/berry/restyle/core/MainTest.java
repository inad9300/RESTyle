package es.berry.restyle.core;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Suite of integration tests.
 */
public class MainTest {

    @Test
    public void mainSuccessfulFlow() {
        final String[] mockArgs = {
                "-" + CommandOptions.SPEC_S, "/home/daniel/Code/RESTyle/Engine/src/main/resources/examples/spec.json",
                "-" + CommandOptions.PLUGINS_S, "MysqlCreationScript,PhpLumen",
                "-" + CommandOptions.OUT_S, "/tmp/.RESTyle_output"
        };

        Main.main(mockArgs);

        assertTrue("Ensure that the program ends", true);
    }
}
