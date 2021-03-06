package es.berry.restyle.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.berry.restyle.specification.SpecObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

public class JsonTest {

    @Before
    public void setUp() {
        SpecObjectMapper.configure("whatever.json");
    }

    @Test
    public void resolveReferences() {
        // We only test a simple case here. The library in use is trusted to work by itself.
        final String jsonWithReference = "{ \"key\": { \"$ref\": \"#/value\" }, \"value\": 33 }";

        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("__junit_test_file__" + Long.toString(System.nanoTime()), ".tmp");
            BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
            bw.write(jsonWithReference);
            bw.close();
        } catch (IOException e) {
            assertFalse("Temporary file creation failed", true);
        }

        final ObjectNode expectedNode = SpecObjectMapper.getInstance().createObjectNode();
        expectedNode.put("key", 33);
        expectedNode.put("value", 33);

        try {
            assertEquals(Json.resolveReferences(tmpFile), expectedNode);
            assertEquals(Json.resolveReferences(tmpFile, SpecObjectMapper.getInstance()), expectedNode);
        } catch (IOException e) {
            assertTrue("JSON references were not resolved properly", false);
        }
    }

    @Test
    public void validateAgainstSchema() {
        final JsonNode node = SpecObjectMapper.getInstance().createObjectNode();
        final String result = Json.validateAgainstSchema(node, "/specification/schema.json");

        // node should be invalid, therefore result should contain some explanatory error message
        assertNotNull(result);
        assertTrue(result.length() > 32);
    }
}
