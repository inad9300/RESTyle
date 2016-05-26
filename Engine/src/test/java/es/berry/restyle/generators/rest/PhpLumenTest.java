package es.berry.restyle.generators.rest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Basic test suite for the results of the plugin PhpLumen, namely a RESTful API using the PHP Lumen framework.
 * <p>
 * They rely on the bookstore.json example specification found in the /resources/examples directory. They are not really
 * specialized for the PhpLumen plugin, but are valid for any plugin that is said to build a web service based on the
 * same example specification.
 * <p>
 * The setup in the bookstore.json in terms of resources and its relations, just to read it readily, is as follows:
 * users have one preferences
 * users have many books
 * books have many bookstores
 * bookstores have many books
 * <p>
 * A server is expected to be up and running before the tests are executed, for instance via
 * <p>
 * <code>php -S localhost:5555 -t public/</code>
 * <p>
 * That means that the program must have been run beforehand, and the outcome moved to some working HTTP server such as
 * Apache, Nginx or the one given by PHP's CLI, as shown.
 * <p>
 * Furthermore, the underlying tables must be filled with the data present in the test's resources folder, under the
 * test-data directory, so that the tests can be run with known data.
 * <p>
 * It is important to understand that this set of tests is not complete. It tries to cover the most common use cases,
 * but having a really complete test suite is a task that requires much more effort. Consider, for instance, that it
 * should take into account the output for all possible forms of wrong input. What is more, the automatic code
 * generation makes it really hard to test even the smallest of examples, since it has the ability to quickly produce
 * lots of big classes. It would be nice to do it sometime in the future, though.
 */
@Ignore // Ignore by default, since (i) the server is not up from the beginning and (ii) they are quite time-consuming.
public class PhpLumenTest {

    final static private String DB_ADMIN_NAME = "tyson";
    final static private String DB_ADMIN_PASS = "root";

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 5555;

        // "preemptive()" is needed because... https://github.com/jayway/rest-assured/issues/356#issuecomment-123187692
        RestAssured.authentication = preemptive().basic(DB_ADMIN_NAME, DB_ADMIN_PASS);
    }

    @Test
    public void upAndRunning() {
        get("/").then().statusCode(200);
    }

    @Test
    public void routesAreWorking() {
        isOk("/users");
        isOk("/users/1");
        isOk("/users/1/books");
        isOk("/users/1/preferences");

        isOk("/preferences");
        isOk("/preferences/1");
        isOk("/preferences/1/users");

        isOk("/books");
        isOk("/books/1");
        isOk("/books/1/files/cover");
        isOk("/books/1/bookstores");
        isOk("/books/1/bookstores/1");
        isOk("/books/1/users");

        isOk("/bookstores");
        isOk("/bookstores/1");
        isOk("/bookstores/1/books");
        isOk("/bookstores/1/books/1");
    }

    /**
     * Shorthand for checking if a GET request returns a 200 OK response.
     */
    private ValidatableResponse isOk(String path) {
        return get(path).then().statusCode(200);
    }

    @Test
    public void routesAreNotFound() {
        notFound("/books/9876543210");
        notFound("/non-existent-end-point-9876543210");
    }

    /**
     * Shorthand for checking if a GET request returns a 404 Not Found response.
     */
    private ValidatableResponse notFound(String path) {
        return get(path).then().statusCode(404);
    }

    @Test
    public void getResources() {
        // Collection
        get("/users").then()
                .contentType(ContentType.JSON)
                .body("count", equalTo(3))
                .body("total", equalTo(3))
                .body("query.page", equalTo(1))
                .body("_links.self.href", containsString("/users"))
                .body("_embedded.users[0].username", equalTo(DB_ADMIN_NAME))
                .body("_embedded.users[0]._links.self.href", containsString("/users"));

        // Item
        get("/books/1").then()
                .contentType(ContentType.JSON)
                .body("title", equalTo("Don Quijote"))
                .body("cover", containsString("/cover"))
                .body("_links.self.href", containsString("/books/1"));

        // File
        get("/books/1/files/cover").then()
                .contentType("image/jpeg");

        // Collection through relationship
        get("/books/1/bookstores").then()
                .contentType(ContentType.JSON)
                .body("count", equalTo(2))
                .body("count", equalTo(2))
                .body("_embedded.bookstores", hasSize(2));

        // "Pivot" resource, from a many to many relationship
        get("/books/1/bookstores/1").then()
                .contentType(ContentType.JSON)
                .body("book_id", equalTo(1))
                .body("bookstore_id", equalTo(1))
                .body("stock", equalTo(7));
    }

    @Test
    public void getResourcesWithFilter() {
        given().param("limit", 1).when().get("/users").then()
                .body("count", equalTo(1))
                .body("_embedded.users", hasSize(1))
                .body("_embedded.users[0].username", equalTo("tyson"));

        given().param("limit", 1).param("page", 2).when().get("/users").then()
                .body("count", equalTo(1))
                .body("query.page", equalTo(2))
                .body("_embedded.users", hasSize(1))
                .body("_embedded.users[0].username", equalTo("mkaku"));

        given().param("fields", "name").when().get("/users").then()
                .body("count", equalTo(3))
                .body("_embedded.users", hasSize(3))
                .body("_embedded.users[0]", hasEntry("name", "Neil deGrasse"))
                .body("_embedded.users[0]", not(hasKey("username")));

        given().param("sort", "id").when().get("/users").then()
                .body("count", equalTo(3))
                .body("_embedded.users", hasSize(3))
                .body("_embedded.users[0].id", equalTo(1))
                .body("_embedded.users[1].id", equalTo(2))
                .body("_embedded.users[2].id", equalTo(3));

        given().param("sort", "-id").when().get("/users").then()
                .body("count", equalTo(3))
                .body("_embedded.users", hasSize(3))
                .body("_embedded.users[0].id", equalTo(3))
                .body("_embedded.users[1].id", equalTo(2))
                .body("_embedded.users[2].id", equalTo(1));

        given().param("filter", "name.eq(*sAgaN)").when().get("/users").then()
                .body("count", equalTo(1))
                .body("_embedded.users", hasSize(1))
                .body("_embedded.users[0].name", equalTo("Carl Sagan"));

        given().param("filter", "id.in(1,2)").when().get("/users").then()
                .body("count", equalTo(2))
                .body("_embedded.users", hasSize(2))
                .body("_embedded.users[0].username", equalTo("tyson"))
                .body("_embedded.users[1].username", equalTo("mkaku"));

        given().param("filter", "name.neq(Carl Sagan);id.neq(1)").when().get("/users").then()
                .body("count", equalTo(1))
                .body("_embedded.users", hasSize(1))
                .body("_embedded.users[0].username", equalTo("mkaku"));
    }

    /**
     * Creation, update and deletion tests are put together to guarantee the order of execution, since JUnit doesn't
     * allow to establish one. Thus, creation should happen first, updates should be done against the created entities,
     * and the same ones will be attempt to be deleted afterwards.
     */
    @Test
    public void createUpdateAndDeleteResources() {
        final String newBook = "{ \"title\": \"New Book\", \"author\": \"New Author\" }";

        // Creation
        final int bookId =
                given().contentType(ContentType.JSON).body(newBook).when()
                        .post("/users/1/books").then()
                        .body("title", equalTo("New Book"))
                        .body("author", equalTo("New Author"))
                        .extract().path("id");

        // Partial update
        final String newTitle = "{\"title\": \"New New Book\"}";

        given().contentType(ContentType.JSON).body(newTitle).when()
                .patch("/books/" + bookId).then()
                .body("title", equalTo("New New Book"))
                .body("author", equalTo("New Author"));

        get("/books/" + bookId).then().body("title", equalTo("New New Book"));

        // Full update - user_id needs to be provided
        final String newNewBook = "{\"title\": \"New New New Book\", \"author\": \"New New Author\", \"user_id\": 1}";

        given().contentType(ContentType.JSON).body(newNewBook).when()
                .put("/books/" + bookId).then()
                .body("title", equalTo("New New New Book"))
                .body("author", equalTo("New New Author"))
                .body("user_id", equalTo(1));

        // Deletion
        delete("/books/" + bookId).then()
                .body("title", equalTo("New New New Book"))
                .body("author", equalTo("New New Author"));
    }
}
