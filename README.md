# RESTyle
Automatic implementation of a REST-centric back-end. More information available at [restyle.berry.es](http://restyle.berry.es/).

## Technical notes

### Running tests
From the `Engine` directory, execute `mvn test`.

Notice that to properly execute all the tests, a server (as the result of the execution of the program) must be running in `localhost:5555`. This is needed by `PhpLumenTest`, and is explained in further detail on [the PhpLumenTest class](https://github.com/inad9300/RESTyle/blob/master/Engine/src/test/java/es/berry/restyle/generators/rest/PhpLumenTest.java) itself. By default, that particular test class is ignored (marked with the JUnit's `@Ignore` annotation). Take into account that other tests in the same package may require special considerations in the future.

### Building the command-line tool
Run `./scripts/build`. The results will appear in the `./bin` directory.

