package es.berry.restyle.core;

import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.utils.Strings;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Some globally-accessible configuration parameters.
 */
final public class Config {
    public static final String APP_CMD = "restyle";
    public static final String LOG_FILE = "restyle.log";

    private static final Logger log = Log.getChain();

    private static File configDir;

    private static final String CONFIG_DEF_NAME = ".restyle.config"; // NOTE: is intended to be a hidden file
    private static final File CONFIG_DEF_A = new File(getJarDir(App.class).getAbsolutePath() + "/" + CONFIG_DEF_NAME);
    private static final File CONFIG_DEF_B = new File("~/" + CONFIG_DEF_NAME);

    // Used to run the program from the IDE with the "resources" directory as the one holding the "configuration"
    private static final File CONFIG_DEF_DEBUG = new File("src/main/resources/");

    /**
     * Sets the value of the template directory. Must be called sometime at the beginning of the execution, based on the
     * value of the corresponding command-line argument.
     */
    /* package */ static void setConfigDir(String path) {
        if (!Strings.isEmpty(path)) {
            final File config = new File(path);
            if (config.exists() && config.isDirectory()) {
                configDir = config;
                return;
            }
        }
        if (CONFIG_DEF_A.exists() && CONFIG_DEF_A.isDirectory()) {
            configDir = CONFIG_DEF_A;
            return;
        }
        if (CONFIG_DEF_B.exists() && CONFIG_DEF_B.isDirectory()) {
            configDir = CONFIG_DEF_B;
            return;
        }
        if (CONFIG_DEF_DEBUG.exists() && CONFIG_DEF_DEBUG.isDirectory()) {
            configDir = CONFIG_DEF_DEBUG;
            log.info("Selecting " + configDir.getPath() + " as the configuration directory...");
            return;
        }

        if (Strings.isEmpty(path))
            throw new IllegalArgumentException("No configuration path provided, and no default configuration " +
                    "directories found (" + CONFIG_DEF_A.getPath() + " nor " + CONFIG_DEF_B.getPath() + ")");

        throw new IllegalArgumentException("Configuration path not found (" + path + "), and no default configuration " +
                "directories found either (" + CONFIG_DEF_A.getPath() + " nor " + CONFIG_DEF_B.getPath() + ")");
    }

    /**
     * Returns a path to where the resources (particularly, the plugins' templates) are expected to be, based on the
     * provided value of the corresponding command-line argument.
     */
    public static File getConfigDir() {
        return configDir;
    }

    /**
     * Compute the absolute file path to the JAR file.
     * The framework is based on http://stackoverflow.com/a/12733172/1614775, but that gets it right for only one of the
     * four cases.
     *
     * @param aclass A class residing in the required JAR.
     * @return A File object for the directory in which the JAR file resides.
     * <p>
     * During testing with NetBeans, the result is ./build/classes/, which is the directory containing what will be in
     * the JAR. [In this case (IntelliJ + Maven), the result would be ./target/classes/]
     * <p>
     * NOTE: all is based on http://stackoverflow.com/a/20953376/2018219
     */
    public static File getJarDir(Class aclass) {
        URL url;
        String extURL; //  url.toExternalForm();

        // get an url
        try {
            url = aclass.getProtectionDomain().getCodeSource().getLocation();
        } catch (SecurityException ex) {
            url = aclass.getResource(aclass.getSimpleName() + ".class");
        }

        // convert to external form
        extURL = url.toExternalForm();

        // prune for various cases
        if (extURL.endsWith(".jar")) // from getCodeSource
            extURL = extURL.substring(0, extURL.lastIndexOf("/"));
        else {  // from getResource
            String suffix = "/" + (aclass.getName()).replace(".", "/") + ".class";
            extURL = extURL.replace(suffix, "");
            if (extURL.startsWith("jar:") && extURL.endsWith(".jar!"))
                extURL = extURL.substring(4, extURL.lastIndexOf("/"));
        }

        // convert back to url
        try {
            url = new URL(extURL);
        } catch (MalformedURLException mux) {
            // leave url unchanged; probably does not happen
        }

        // convert url to File
        try {
            return new File(url.toURI());
        } catch (URISyntaxException ex) {
            return new File(url.getPath());
        }
    }
}
