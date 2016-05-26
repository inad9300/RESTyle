# RESTyle
Automatic implementation of a REST-centric back-end. More information available at [restyle.berry.es](http://restyle.berry.es/).

## Available plugins
To this day, the following plugins have been created:
- `MysqlCreationScript`, whose output is a DDL file capable of creating a MySQL database representing the data model specified.
- `PhpLumen`, which generates an entire server based on the PHP's [Lumen](https://lumen.laravel.com/) framework.

## Requirements
In order to execute the application, it is necessary to meet two simple requirements:
- A GNU/Linux operating system.
- Java 8.

## Technical notes

### Running tests
To make sure that everything is up and running, located on the `Engine` directory, execute the command `mvn test`.

Notice that to properly execute all the tests, a server (understood as the result of the execution of the program) must be running in `localhost:5555`. This is needed by `PhpLumenTest`, and is explained in further detail on the [PhpLumenTest class](https://github.com/inad9300/RESTyle/blob/master/Engine/src/test/java/es/berry/restyle/generators/rest/PhpLumenTest.java) itself. That said, that particular test class is ignored by default (marked with the JUnit's `@Ignore` annotation). Take into account that other tests in the same package may require special considerations in the future.

### Building the command-line tool
To produce an up-to-date executable of the application, run `./scripts/build`. The outcome will appear in the `./bin` directory.
