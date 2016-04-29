package es.berry.restyle.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import es.berry.restyle.exceptions.PluginException;
import es.berry.restyle.exceptions.SpecException;
import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.specification.AdvancedValidator;
import es.berry.restyle.specification.Completor;
import es.berry.restyle.specification.FieldsTypeResolver;
import es.berry.restyle.specification.generated.Spec;
import es.berry.restyle.utils.Json;
import es.berry.restyle.utils.Strings;
import org.apache.commons.cli.*;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.compare;

/**
 * Main class. Sets up the command line tool and load the specification and the plugins.
 */
final public class App {
    private static final Options OPTS = CommandOptions.get();
    private static final Logger log = Log.getChain();

    private static final String VALUES_SEP = ",";

    public static void main(String[] args) {
        try {
            final String[] mockArgs = {
                    "-" + CommandOptions.SPEC_S, "/home/daniel/Code/RESTyle/Engine/src/main/resources/examples/spec.json",
                    "-" + CommandOptions.PLUGINS_S, "MysqlCreationScript" + VALUES_SEP + "PhpLumen",
                    "-" + CommandOptions.OUT_S, "/home/daniel/Code/RESTyle_output",
                    "-verbose"
            };
            // Temporarely to make the automatic tests pass while being able to check some manual "mock args"
            if (args.length == 0) args = mockArgs;

            final CommandLine cmd = setUpCli(args);

            // Get available plugins (which will be needed later anyway)
            final Reflections reflections = new Reflections("es.berry.restyle.generators");
            final Set<Class<? extends Generator>> allConcreteGenerators = reflections.getSubTypesOf(Generator.class);
            final List<String> availablePlugins = allConcreteGenerators.stream().map(Class::getSimpleName).collect(Collectors.toList());

            runHigherPriorityCommands(cmd, availablePlugins);

            final String specPath = cmd.getOptionValue(CommandOptions.SPEC_S);
            final String outputPath = cmd.getOptionValue(CommandOptions.OUT_S);
            final String pluginsValue = cmd.getOptionValue(CommandOptions.PLUGINS_S);

            validateArgKeys(specPath, outputPath, pluginsValue);

            final File specFile = new File(specPath);
            final File outputDir = new File(outputPath);

            validateArgValues(specFile, outputDir);

            final ObjectMapper mapper = configureObjectMapper(specPath);

            final JsonNode specNode = resolveBaseUrl(validateJson(specFile, mapper));

            validateSpecAgainstSchema(specNode);

            Spec spec = loadSpec(specNode, mapper);

            spec = new Completor(spec).addDefaultValues().getSpec();
            spec = new FieldsTypeResolver(spec).resolve().getSpec();
            new AdvancedValidator(spec).validate();

            // log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec));

            Generator.runAll(loadPlugins(pluginsValue, availablePlugins, allConcreteGenerators), spec, outputDir);
        } catch (SpecException e) {
            log.error("There exists an error with the specification", e);
        } catch (PluginException e) {
            log.error("A plugin experimented an error", e);
        } catch (Exception e) {
            log.broke("An unexpected error occurred", e);
        }
    }

    private static void printHelp() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        formatter.printHelp(Config.APP_CMD, OPTS);
    }

    private static CommandLine setUpCli(String[] args) {
        final CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(OPTS, args);
        } catch (ParseException e) {
            log.error("Error parsing the command-line options", e);
        }
        return null;
    }

    private static void runHigherPriorityCommands(CommandLine cmd, List<String> availablePlugins) {
        if (cmd.hasOption(CommandOptions.HELP_S)) {
            printHelp();
            System.exit(0);
        }

        if (cmd.hasOption(CommandOptions.LIST_PLUGINS_S)) {
            log.info(Strings.list(availablePlugins));
            System.exit(0);
        }

        if (!cmd.hasOption(CommandOptions.VERBOSE_S))
            Reflections.log = null;
    }

    private static void validateArgKeys(String specPath, String outputPath, String pluginsValue) {
        final String missingMessage = "Missing required option: ";

        if (Strings.isEmpty(specPath))
            throw new IllegalArgumentException(missingMessage + CommandOptions.SPEC_L + ".");

        if (Strings.isEmpty(outputPath))
            throw new IllegalArgumentException(missingMessage + CommandOptions.OUT_L + ".");

        if (Strings.isEmpty(pluginsValue))
            throw new IllegalArgumentException(missingMessage + CommandOptions.PLUGINS_L + ".");
    }

    private static void validateArgValues(File specFile, File outputDir) {
        if (!specFile.exists() || !specFile.isFile())
            throw new IllegalArgumentException("The specification file does not exist.");

        if (outputDir.exists()) {
            if (outputDir.isFile())
                throw new IllegalArgumentException("Only a directory can be specified as output.");
        } else if (!outputDir.mkdirs())
            throw new IllegalArgumentException("The output directory does not exist and could not be created.");
    }

    private static ObjectMapper configureObjectMapper(String specPath) {
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

        return mapper;
    }

    /**
     * Validate JSON format and resolve JSON references ($ref).
     */
    private static JsonNode validateJson(File specFile, ObjectMapper mapper) {
        try {
            // NOTE: The original mapper.readTree(specFile) does not resolve JSON references
            return Json.resolveReferences(specFile, mapper);
        } catch (JsonProcessingException e) {
            // Will inform about JSON validation errors
            log.error("Error processing the specification file", e);
        } catch (IOException e) {
            log.error("Error reading the specification file (or a file referenced by it)", e);
        }
        return null;
    }

    private static JsonNode resolveBaseUrl(JsonNode specNode) {
        return ((ObjectNode) specNode).put(
                "baseUrl",
                specNode.get("baseUrl").asText()
                        .replace("{version}", specNode.get("version").asText())
        );
    }

    /**
     * Validate specification agains JSON schema.
     */
    private static void validateSpecAgainstSchema(JsonNode specNode) {
        final String schemaPath = "./src/main/resources/specification/schema.json";
        final JsonNode schemaNode;
        try {
            schemaNode = JsonLoader.fromFile(new File(schemaPath));
            final JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema(schemaNode);

            ProcessingReport report = schema.validate(specNode);
            if (!report.isSuccess()) { // FIXME: type resolution happens later, so this will fail if it was needed (?) -- may help: mapper.writeValueAsString(obj);
                String msg = "The specification provided does not conform the meta-specification defined for it:\n";

                for (ProcessingMessage r : report)
                    msg += r;

                log.error(msg);
                System.exit(1);
            }
        } catch (IOException e) {
            log.error("The JSON schema file could not be read", e);
        } catch (ProcessingException e) {
            log.error("An error happened when processing the JSON schema file", e);
        }
    }

    private static Spec loadSpec(JsonNode specNode, ObjectMapper mapper) {
        try {
            return mapper.treeToValue(specNode, Spec.class);
        } catch (JsonProcessingException e) {
            log.error("Error processing the file:\n" + e.getOriginalMessage());
        }
        return null;
    }

    private static List<Class<? extends Generator>> loadPlugins(
            String pluginsValue,
            List<String> availablePlugins,
            Collection<Class<? extends Generator>> allConcreteGenerators
    ) {
        final List<String> selectedPlugins = Arrays.asList(pluginsValue.split(VALUES_SEP));
        for (String selectedPlugin : selectedPlugins)
            if (!availablePlugins.contains(selectedPlugin))
                log.error("The plugin " + Strings.surround(selectedPlugin, "\"") + " is not in the list of available plugins. "
                        + "Please, select one of the following:\n" + Strings.list(availablePlugins));

        final List<Class<? extends Generator>> concreteGenerators = new ArrayList<>();
        for (Class<? extends Generator> c : allConcreteGenerators)
            if (selectedPlugins.contains(c.getSimpleName()))
                concreteGenerators.add(c);

        // Sort the plugins according to the exact order they were provided
        // IDEA: resolve dependencies first, so that the final user can forget about the plugins' order
        Collections.sort(concreteGenerators, (left, right) -> compare(
                selectedPlugins.indexOf(left.getSimpleName()), selectedPlugins.indexOf(right.getSimpleName())
        ));

        return concreteGenerators;
    }
}