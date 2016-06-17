package es.berry.restyle.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.berry.restyle.exceptions.PluginException;
import es.berry.restyle.exceptions.SpecException;
import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.specification.AdvancedValidator;
import es.berry.restyle.specification.Completor;
import es.berry.restyle.specification.FieldsTypeResolver;
import es.berry.restyle.specification.SpecObjectMapper;
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
 * Main class. Sets up the command line tool and loads the specification and the plugins.
 */
final public class App {
    private static final Options OPTS = CommandOptions.get();
    private static final Logger log = Log.getChain();

    private static final String VALUES_SEP = ",";
    private static final String SPEC_SCHEMA = "/specification/schema.json";

    /**
     * Main method. In summary, parses the command line arguments, and executes the plugins passing them the
     * specification, all based on the given values.
     */
    public static void main(String[] args) {
        try {
            // The following bit allows for some manual testing with "mock args" while not breaking the automatic tests
            final String[] mockArgs = {
                    "-" + CommandOptions.SPEC_S, "src/main/resources/examples/bookstore.json",
                    "-" + CommandOptions.PLUGINS_S, "MysqlCreationScript" + VALUES_SEP + "PhpLumen" + VALUES_SEP + "AngularJs",
                    "-" + CommandOptions.OUT_S, "/home/daniel/Code/RESTyle_output",
                    "-verbose"
            };
            if (args.length == 0) args = mockArgs;

            final CommandLine cmd = setUpCli(args);

            // Get available plugins (which will be needed later anyway)
            final Reflections reflections = new Reflections("es.berry.restyle.generators");
            final Set<Class<? extends Generator>> allConcreteGenerators = reflections.getSubTypesOf(Generator.class);
            final List<String> availablePlugins = allConcreteGenerators.stream().map(Class::getSimpleName).collect(Collectors.toList());

            runHigherPriorityCommands(cmd, availablePlugins);

            final String specPath = cmd.getOptionValue(CommandOptions.SPEC_S);
            final String outputPath = cmd.getOptionValue(CommandOptions.OUT_S);
            final String configPath = cmd.getOptionValue(CommandOptions.CONFIG_S);
            final String pluginsValue = cmd.getOptionValue(CommandOptions.PLUGINS_S);

            validateArgKeys(specPath, outputPath, pluginsValue);

            final File specFile = new File(specPath);
            final File outputDir = new File(outputPath);

            validateArgValues(specFile, outputDir);

            Config.setConfigDir(configPath);

            SpecObjectMapper.configure(specPath);

            final ObjectMapper mapper = SpecObjectMapper.getInstance();

            JsonNode specNode = resolveBaseUrl(validateJson(specFile, mapper));

            // Type resolution must happen first, otherwise the schema validation shall not pass. Plus, it is necessary
            // to perform it at the JsonNode level, since reversing the conversion from the Spec class level (through
            // the valueToTree() function) generates values that make the specification not pass (for instance, puts
            // "enum"s as empty arrays, as oppose to nulls, but they are required to have one element at minimum.
            new FieldsTypeResolver(specNode).resolve();

            final String report = Json.validateAgainstSchema(specNode, SPEC_SCHEMA);
            if (!Strings.isEmpty(report)) {
                log.error(report);
                System.exit(1);
            }

            Spec spec = loadSpec(specNode, mapper);

            spec = new Completor(spec).addDefaultValues().getSpec();
            new AdvancedValidator(spec).validate();

            // log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec));

            Generator.runAll(selectPlugins(pluginsValue, availablePlugins, allConcreteGenerators), spec, specNode, outputDir);
        } catch (SpecException e) {
            log.error("There exists an error with the specification", e);
        } catch (PluginException e) {
            log.error("A plugin experimented an error", e);
        } catch (Exception e) {
            log.broke("An unexpected error occurred", e);
        }
    }

    /**
     * Simple builder for the CommandLine class.
     */
    private static CommandLine setUpCli(String[] args) {
        final CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(OPTS, args);
        } catch (ParseException e) {
            log.error("Error parsing the command-line options", e);
        }
        return null;
    }

    /**
     * Execute some commands that either (i) if present, should stop the execution of the tool or (ii) other commands
     * need them to be run first.
     */
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
            Reflections.log = null; // FIXME: not working
    }

    /**
     * Print a nicely formatted help message.
     */
    private static void printHelp() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        formatter.printHelp(Config.APP_CMD, OPTS);
    }

    /**
     * Ensure that the mandatory arguments are present in the command line.
     */
    private static void validateArgKeys(String specPath, String outputPath, String pluginsValue) {
        final String missingMessage = "Missing required option: ";

        if (Strings.isEmpty(specPath))
            throw new IllegalArgumentException(missingMessage + CommandOptions.SPEC_L + ".");

        if (Strings.isEmpty(outputPath))
            throw new IllegalArgumentException(missingMessage + CommandOptions.OUT_L + ".");

        if (Strings.isEmpty(pluginsValue))
            throw new IllegalArgumentException(missingMessage + CommandOptions.PLUGINS_L + ".");
    }

    /**
     * Simple checks for the values passed via the command line.
     */
    private static void validateArgValues(File specFile, File outputDir) {
        if (!specFile.exists() || !specFile.isFile())
            throw new IllegalArgumentException("The specification file does not exist.");

        if (outputDir.exists()) {
            if (outputDir.isFile())
                throw new IllegalArgumentException("Only a directory can be specified as output.");
        } else if (!outputDir.mkdirs())
            throw new IllegalArgumentException("The output directory does not exist and could not be created.");
    }

    /**
     * Validate JSON format and resolve JSON references ($ref).
     */
    private static JsonNode validateJson(File specFile, ObjectMapper mapper) {
        try {
            // NOTE: The original mapper.readTree(specFile) does not resolve JSON references
            return Json.resolveReferences(specFile, mapper); // Will complain if JSON is invalid
        } catch (JsonProcessingException e) {
            // Will inform about JSON validation errors
            log.error("Error processing the specification file", e);
        } catch (IOException e) {
            log.error("Error reading the specification file (or a file referenced by it)", e);
        }
        return null;
    }

    /**
     * The specification "baseUrl" attribute may contain a special "{version}" substring that needs to be exchanged by
     * the value of the "version" attribute.
     */
    private static JsonNode resolveBaseUrl(JsonNode specNode) {
        return ((ObjectNode) specNode).put(
                "baseUrl",
                specNode.get("baseUrl").asText()
                        .replace("{version}", specNode.get("version").asText())
        );
    }

    /**
     * Given a JsonNode as a direct representation of the JSON specification, returns the same information as POJOs,
     * taking the Spec class as root.
     */
    private static Spec loadSpec(JsonNode specNode, ObjectMapper mapper) {
        try {
            return mapper.treeToValue(specNode, Spec.class);
        } catch (JsonProcessingException e) {
            log.error("Error processing the file:\n" + e.getOriginalMessage());
        }
        return null;
    }

    /**
     * Filters the plugins provided by the user in the command line, ensuring that they are available, and that they
     * end up ordered in the same way (the user decides about the order, taking into account the dependencies).
     */
    private static List<Class<? extends Generator>> selectPlugins(
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
        Collections.sort(concreteGenerators, (left, right) -> compare(
                selectedPlugins.indexOf(left.getSimpleName()), selectedPlugins.indexOf(right.getSimpleName())
        ));

        return concreteGenerators;
    }
}