package es.berry.restyle.specification;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;

/**
 * Wrapper class for the Jackson's ObjectMapper, so that the same one is available across the application, with the
 * proper configuration, also avoiding to create multiple instances. It follows the singleton pattern.
 */
final public class SpecObjectMapper {

    private static ObjectMapper mapper = null;

    final private static Logger log = Log.getChain();

    /**
     * Private constructor to prevent other classes for instantiating.
     */
    private SpecObjectMapper() {
    }

    /**
     * Return the singleton instance. Will return null until the ObjectMapper gets configured.
     */
    public static ObjectMapper getInstance() {
        if (mapper == null)
            throw new NullPointerException("The ObjectMapper is not ready: it needs to be prepared via the \"configure\" method first.");

        return mapper;
    }

    /**
     * Configure the appropriate ObjectMapper depending on the specification file's extension, so that both JSON and
     * YAML formats are supported. Note that for the JSON format some non-standard features are enabled in order to
     * improve readability of the documents.
     */
    public static void configure(String specPath) {
        if (specPath.endsWith(".json")) {
            mapper = new ObjectMapper();

            // Allows (non-standard) C/C++ style comments in JSON
            mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            // Allows (non-standard) unquoted field names in JSON
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        } else if (specPath.endsWith(".yaml") || specPath.endsWith(".yml"))
            mapper = new ObjectMapper(new YAMLFactory());
        else
            log.error("The specification file has an unsupported extension. Valid extensions are: .json, .yaml, .yml");

        // Allow non-expected properties (same idea as JSON Schema's "additionalProperties", which seems not to be
        // sufficient)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
