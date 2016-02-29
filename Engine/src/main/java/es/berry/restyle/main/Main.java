package es.berry.restyle.main;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import es.berry.restyle.logging.ConsoleLogger;
import es.berry.restyle.logging.FileLogger;
import es.berry.restyle.logging.Logger;
import org.apache.commons.cli.*;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginManager;

import java.io.File;
import java.io.IOException;

final public class Main {

    private final static String APP_NAME = "RESTyle";
    private static final File PLUGINS_DIR = new File("plugins"); // FIXME

    private static Options getCommandOptions() {
        Options opts = new Options();

        opts.addOption(
                Option
                        .builder(CommandOptions.GLOBAL_CONFIG_S)
                        .longOpt(CommandOptions.GLOBAL_CONFIG_L)
                        .desc("path to the global configuration file")
                        .required()
                        .hasArg()
                        .type(String.class)
                        .build()
        );

        opts.addOption(
                Option
                        .builder("a")
                        .longOpt("auth-config")
                        .desc("path to the configuration file describing the authentication and authorization")
                        .required()
                        .hasArg()
                        .type(String.class)
                        .build()
        );

        opts.addOption(
                Option
                        .builder("r")
                        .longOpt("resources")
                        .desc("comma-separated list of resources to be considered when generating the outcome")
                        .hasArgs()
                        .valueSeparator(',')
                        .type(String.class)
                        .build()
        );

        return opts;
    }

    private static Logger getLoggersChain() {
        Logger consoleLogger = new ConsoleLogger(Logger.DEBUG);
        Logger fileLogger = new FileLogger(Logger.ERROR, "error_log");

        consoleLogger.setNext(fileLogger);

        return consoleLogger; // Return the one everyone else is linked to
    }

    public static void main(String[] args) {
        Logger log = getLoggersChain();
        try {
            Options opts = getCommandOptions();
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(opts, args);

            // HelpFormatter formatter = new HelpFormatter();
            // formatter.printHelp(APP_NAME, opts);

            if (/*opts.getOption(CommandOptions.GLOBAL_CONFIG_S).isRequired() && */!cmd.hasOption(CommandOptions.GLOBAL_CONFIG_S))
                log.error("The option " + CommandOptions.GLOBAL_CONFIG_L + " is required.");

            String globalConfigPath = cmd.getOptionValue(CommandOptions.GLOBAL_CONFIG_S);
            if (globalConfigPath == null)
                log.error("A value must be specified for option " + CommandOptions.GLOBAL_CONFIG_L + ".");

            File globalConfigFile = new File(globalConfigPath);
            if (!globalConfigFile.exists() || !globalConfigFile.isFile())
                log.error("Global configuration file does not exist.");

            ObjectMapper mapper = null;
            if (globalConfigPath.endsWith(".json"))
                mapper = new ObjectMapper();
            else if (globalConfigPath.endsWith(".yaml") || globalConfigPath.endsWith(".yml"))
                mapper = new ObjectMapper(new YAMLFactory());
            else
                log.error("Cannot recognize extension in global configuration file.");

            JsonNode data = mapper.readTree(globalConfigFile);

            // Allows (non-standard) C/C++ style comments in JSON
            mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            // Allows (non-standard) unquoted field names in JSON
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

            // TODO: validate configuration file (define JSON schema "meta-spec")

            // Keys:                Iterator names = data.fieldNames();
            // Values:              data.elements();
            // Array items:         data.at("/numbers").elements();
            // Finding one element: data.at("/numbers/2").asInt();

            // ...

            PluginManager pm = new DefaultPluginManager(PLUGINS_DIR);
            pm.loadPlugins();
            pm.startPlugins();
            pm.stopPlugins();

        } catch (ParseException e) {
            log.error("Error parsing the command-line options: " + e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("Error processing the file: " + e.getMessage());
        } catch (IOException e) {
            log.error("Impossible to read configuration file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Sorry, an unexpected error occurred :(");
        }
    }
}
