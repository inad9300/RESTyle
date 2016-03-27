package es.berry.restyle.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import me.andrz.jackson.JsonReferenceException;
import me.andrz.jackson.JsonReferenceProcessor;
import me.andrz.jackson.ObjectMapperFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

final public class Json {

    private static final Logger log = Log.getChain();

    public static JsonNode resolveReferences(File json, final ObjectMapper mapper) throws IOException {
        JsonReferenceProcessor processor = new JsonReferenceProcessor();
        processor.setStopOnCircular(true);
        processor.setMaxDepth(8);
        if (mapper != null)
            processor.setMapperFactory(new ObjectMapperFactory() {
                public ObjectMapper create(URL url) {
                    return mapper;
                }
            });

        try {
            return processor.process(json);
        } catch (JsonReferenceException e) {
            log.error("Error resolving some JSON reference", e);
            return null;
        }
    }

    public static JsonNode resolveReferences(File json) throws IOException {
        return resolveReferences(json, null);
    }
}
