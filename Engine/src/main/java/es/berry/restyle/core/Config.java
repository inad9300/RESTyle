package es.berry.restyle.core;

/**
 * Some globally-accessible configuration parameters.
 */
final public class Config {
    public final static String APP_CMD = "restyle";
    public final static String LOG_FILE = "restyle.log";

    /**
     * Returns a path that will be valid with independence of the execution environment of the application (in
     * particular, it will be valid from the IDE and from the command line).
     */
    public static String getResourcePath(String path) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader.getResource(path).getPath();
    }
}
