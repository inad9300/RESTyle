package es.berry.restyle.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import me.andrz.jackson.JsonReferenceException;
import me.andrz.jackson.JsonReferenceProcessor;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for JSON-related operations.
 */
final public class Json {

    private static final Logger log = Log.getChain();

    /**
     * Resolve JSON references (https://tools.ietf.org/html/draft-pbryan-zyp-json-ref-03) present on a file.
     */
    public static JsonNode resolveReferences(File json, final ObjectMapper mapper) throws IOException {
        JsonReferenceProcessor processor = new JsonReferenceProcessor();
        processor.setStopOnCircular(true);
        processor.setMaxDepth(8);
        if (mapper != null)
            processor.setMapperFactory(url -> mapper);

        try {
            return processor.process(json);
        } catch (JsonReferenceException e) {
            log.error("Error resolving some JSON reference", e);
            return null;
        }
    }

    /**
     * Override the resolveReferences() function to take no ObjectMapper by default.
     */
    public static JsonNode resolveReferences(File json) throws IOException {
        return resolveReferences(json, null);
    }


    /**
     * Validate a JsonNode against a JSON Schema, returning null if everything went well, or a String containing a
     * report with the errors found.
     */
    public static String validateAgainstSchema(JsonNode node, String schemaPath) {
        final JsonNode schemaNode;
        try {
            schemaNode = JsonLoader.fromFile(new File(schemaPath));
            final JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema(schemaNode);

            ProcessingReport report = schema.validate(node);
            if (!report.isSuccess()) {
                String msg = "The JSON data provided does not conform the meta-specification defined for it:\n";

                for (ProcessingMessage r : report)
                    msg += r;

                return msg;
            }
        } catch (IOException e) {
            log.error("The JSON schema file could not be read", e);
        } catch (ProcessingException e) {
            log.error("An error happened when processing the JSON schema file", e);
        }
        return null;
    }
}
