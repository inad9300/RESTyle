package es.berry.restyle.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import es.berry.restyle.exceptions.PluginException;
import es.berry.restyle.exceptions.SpecException;
import es.berry.restyle.logging.ConsoleLogger;
import es.berry.restyle.logging.EmailLogger;
import es.berry.restyle.logging.FileLogger;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.specification.Spec;
import es.berry.restyle.utils.Strings;
import org.apache.commons.cli.*;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

final public class Main {
    private final static Options OPTS = CommandOptions.get();
    private final static Logger log = getLoggersChain();

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(Config.APP_NAME, OPTS);
    }

    private static Logger getLoggersChain() {
        Logger consoleLogger = new ConsoleLogger(Logger.INFO);
        Logger fileLogger = new FileLogger(Logger.ERROR, Config.LOG_FILE);
        Logger emailLogger = new EmailLogger(Logger.CRITICAL, "logging-test@berry.es", Config.DEV_EMAILS); // FIXME

        consoleLogger.setNext(fileLogger);
        fileLogger.setNext(emailLogger);

        return consoleLogger; // Return the one everyone else is linked to
    }

    public static void main(String[] args) {
        try {
            final String[] mockArgs = {
                    "-" + CommandOptions.SPEC_S, "/home/daniel/Code/RESTyle/Engine/src/main/resources/examples/spec.json",
                    "-" + CommandOptions.PLUGINS_S, "MysqlCreationScript",
                    "-" + CommandOptions.OUT_S, ""
            };

            // Set up command-line
            // -------------------
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = null;
            try {
                cmd = parser.parse(OPTS, mockArgs);
            } catch (ParseException e) {
                log.error("Error parsing the command-line options:\n" + e.getMessage());
            }
            assert cmd != null;


            // Validate arguments
            // ------------------
            final String specPath = cmd.getOptionValue(CommandOptions.SPEC_S);
            if (Strings.isEmpty(specPath))
                log.error("A value must be specified for option " + CommandOptions.SPEC_L + ".");

            final File specFile = new File(specPath);
            if (!specFile.exists() || !specFile.isFile())
                log.error("The specification file does not exist.");


            // Configure mapper depending on the format
            // ----------------------------------------
            ObjectMapper mapper = null;
            if (specPath.endsWith(".json")) {
                mapper = new ObjectMapper();

                // Allows (non-standard) C/C++ style comments in JSON
                mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
                // Allows (non-standard) unquoted field names in JSON
                mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            } else if (specPath.endsWith(".yaml") || specPath.endsWith(".yml"))
                mapper = new ObjectMapper(new YAMLFactory());
            else
                log.error("The specification file has an unsupported extension. Valid extensions are: .json, .yaml, .yml");

            assert mapper != null;


            // Validate JSON, and prepare the baseUrl
            // --------------------------------------
            JsonNode specNode = null;
            try {
                specNode = mapper.readTree(specFile);
            } catch (JsonProcessingException e) {
                // Will inform about JSON validation errors
                log.error("Error processing the specification file: " + e.getMessage());
            } catch (IOException e) {
                log.error("Error reading the specification file: " + e.getMessage());
            }
            assert specNode != null;

            specNode = ((ObjectNode) specNode).put(
                    "baseUrl",
                    specNode.get("baseUrl").asText()
                            .replace("{version}", specNode.get("version").asText())
            );


            // Validate specification against schema
            // -------------------------------------
            final String schemaPath = "./src/main/resources/specification/schema.json";
            final JsonNode schemaNode;
            try {
                schemaNode = JsonLoader.fromFile(new File(schemaPath));
                final JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema(schemaNode);

                ProcessingReport report = schema.validate(specNode);
                if (!report.isSuccess()) {
                    System.out.println("The specification provided does not conform the meta-specification defined for it:"
                            + "\n--- BEGIN REPORT ---");

                    for (ProcessingMessage msg : report)
                        System.out.print(msg);

                    System.out.println("--- END REPORT ---");
                }
            } catch (IOException e) {
                log.error("The JSON schema file could not be read.");
            } catch (ProcessingException e) {
                log.error("An error happened when processing the JSON schema file.");
            }


            // Load specification into Java classes and complete it
            // ----------------------------------------------------
            Spec spec = null;
            try {
                spec = mapper.treeToValue(specNode, Spec.class);
            } catch (JsonProcessingException e) {
                log.error("Error processing the file:\n" + e.getOriginalMessage());
            }
            new SpecAdvanceValidator(spec).validate();
            spec = new SpecCompletor(spec).addDefaultValues().getSpec();
            spec = new FieldsTypeResolver(spec).resolve().getSpec();
//            System.out.println( mapper.writeValueAsString(spec) );


            // Load and execute plugins
            // ------------------------
            Reflections reflections = new Reflections("es.berry.restyle.generators");
            Set<Class<? extends Generator>> concreteGenerators = reflections.getSubTypesOf(Generator.class);

            List<String> availablePlugins = new ArrayList<String>();
            for (Class<? extends Generator> gen : concreteGenerators)
                availablePlugins.add(gen.getSimpleName());

            List<String> selectedPlugins = Arrays.asList(cmd.getOptionValues(CommandOptions.PLUGINS_S));
            for (String selectedPlugin : selectedPlugins)
                if (!availablePlugins.contains(selectedPlugin))
                    log.error("The plugin " + Strings.surround(selectedPlugin, "\"") + " is not in the list of available plugins. "
                            + "Please, select one of the following:\n" + Strings.list(availablePlugins));

            for (Class<? extends Generator> gen : concreteGenerators)
                if (selectedPlugins.contains(gen.getSimpleName()))
                    try {
                        gen.getConstructor(Spec.class).newInstance(spec).generate();
                    } catch (InstantiationException e) {
                        log.broke("Impossible to instantiate plugin class " + gen.getSimpleName(), e);
                    } catch (IllegalAccessException e) {
                        log.broke("Impossible to access generate method", e);
                    } catch (InvocationTargetException e) {
                        log.broke("Impossible to invoke generate method", e);
                    } catch (NoSuchMethodException e) {
                        log.broke("Impossible to find generate method", e);
                    }
        } catch (SpecException e) {
            log.error("There exists an error with the specification", e);
        } catch (PluginException e) {
            log.error("A plugin experimented an error", e);
        } catch (Exception e) {
            log.broke("An unexpected error occurred", e);
        }
    }
}