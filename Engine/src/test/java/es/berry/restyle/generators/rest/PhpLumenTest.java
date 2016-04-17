package es.berry.restyle.generators.rest;

import com.eclipsesource.restfuse.Destination;
import com.eclipsesource.restfuse.HttpJUnitRunner;
import com.eclipsesource.restfuse.Method;
import com.eclipsesource.restfuse.Response;
import com.eclipsesource.restfuse.annotation.Context;
import com.eclipsesource.restfuse.annotation.HttpTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.berry.restyle.utils.Strings;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static com.eclipsesource.restfuse.Assert.assertOk;
import static junit.framework.TestCase.assertTrue;

/**
 * Tests for the results (namely, a RESTful API using the PHP Lumen framework) of the plugin PhpLumen, using the
 * pets-spec.json specification in the /resources/examples directory.
 * <p>
 * NOTE: a server is expected to be up and running before the tests are run, for instance via
 * php -S localhost:5555 -t public/
 */
@RunWith(HttpJUnitRunner.class)
public class PhpLumenTest {
    @Rule
    public Destination destination = getDestination();

    // ::1 is the loopback address for IPv6 (localhost should resolve to that, but it is not doing so)
    final private String baseUrl = "http://[::1]:5555";

    private Destination getDestination() {
        Destination destination = new Destination(this, this.baseUrl);
//        destination.getRequestContext()
//                .addHeader("Cookie", "name:value")
//                .addPathSegment("version", "1.1.1"); // If "{version}" in the URL or any path
        return destination;
    }

    @Context
    private Response response; // Will be injected after every request

    @HttpTest(method = Method.GET, path = "/pets")
    public void checkOnlineStatus() {
        assertOk(response);
    }

    @HttpTest(method = Method.GET, path = "/pets") // , content="{...}"
    public void getPets() {
        assertOk(response);
        assertTrue(response.hasBody());

        JsonNode jsonNode = null;
        try {
            jsonNode = new ObjectMapper().readTree(response.getBody());
        } catch (IOException e) {
            assertTrue("Returned JSON was not properly formatted", false);
        }

        assertTrue(jsonNode.isArray());
        ArrayNode arrayNode = (ArrayNode) jsonNode;

        assertTrue(arrayNode.size() > 0);
        assertTrue(arrayNode.get(0).isObject());
        ObjectNode objectNode = (ObjectNode) arrayNode.get(0);

        final List<String> fieldNames = Strings.iteratorToList(objectNode.fieldNames());

        assertTrue(fieldNames.contains("id"));
        assertTrue(fieldNames.contains("name"));
        assertTrue(fieldNames.contains("breed"));
        assertTrue(fieldNames.contains("owner_id"));
    }
}
