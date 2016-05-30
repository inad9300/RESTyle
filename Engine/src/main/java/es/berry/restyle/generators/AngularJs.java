package es.berry.restyle.generators;

import com.fasterxml.jackson.databind.JsonNode;
import es.berry.restyle.core.Generator;
import es.berry.restyle.specification.generated.Spec;

import java.io.File;

/**
 * Plugin to create a REST client based on the AngularJS 1 JavaScript framework.
 */
public class AngularJs extends Generator {

    public AngularJs(Spec spec, JsonNode specNode, File outputDir) {
        super(spec, specNode, outputDir);
    }

    /**
     * Main method.
     */
    @Override
    public void generate() {
        // TODO:
        // - HTTP interceptor for Basic Auth parameters and JSON headers (and something special for files) -- check if the URL begins with API_URL
        // - HTML forms to create and update information
        // - Angular Services for each resource
        // - Define (client-side) routes, and list them all in the main page
    }
}
