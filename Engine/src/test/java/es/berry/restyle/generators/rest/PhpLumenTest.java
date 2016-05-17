package es.berry.restyle.generators.rest;

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.when;

/**
 * Tests for the results of the plugin PhpLumen, namely a RESTful API using the PHP Lumen framework.
 * <p>
 * They rely on the bookstore.json example specification found in the /resources/examples directory. They are not really
 * specialized for the PhpLumen plugin, but are valid for any plugin that is said to build a web service based on the
 * same example specification.
 * <p>
 * A server is expected to be up and running before the tests are executed, for instance via
 * php -S localhost:5555 -t public/
 * That means that the program must have been run beforehand, and the outcome moved to some working HTTP server such as
 * Apache, Nginx or the one given by PHP's CLI, as shown.
 * <p>
 * Furthermore, the table marked as user, if any, is assumed to be filled with a user whose role be allowed to perform
 * any operation in the API, so that the tests can run with ease. Use or modify the values below for that purpose.
 */
public class PhpLumenTest {

    final static private String DB_ADMIN_NAME = "root";
    final static private String DB_ADMIN_PASS = "$2a$10$3GNHSPj/y3lzIFHWNAuReeRHzL1f4Q3Fg6EA8Z43peItPBqqYR5jq"; // "root"

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 5555;
        RestAssured.basic(DB_ADMIN_NAME, DB_ADMIN_PASS);
    }

    @Test
    public void upAndRunning() {
        when().get("/").then().statusCode(404);
    }

    @Test
    public void routesAreWorking() {
        when().get("/users").then().statusCode(200);
        when().get("/books").then().statusCode(200);
        when().get("/bookstores").then().statusCode(200);
        when().get("/preferences").then().statusCode(200);
        when().get("").then().statusCode(200);
        when().get("").then().statusCode(200);
        when().get("").then().statusCode(200);
    }
}
