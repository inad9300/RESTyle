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
import es.berry.restyle.specification.AdvanceValidator;
import es.berry.restyle.specification.Completor;
import es.berry.restyle.specification.FieldsTypeResolver;
import es.berry.restyle.specification.generated.Spec;
import es.berry.restyle.utils.Json;
import es.berry.restyle.utils.Strings;
import org.apache.commons.cli.*;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static java.lang.Integer.compare;

final public class Main {
    private static final Options OPTS = CommandOptions.get();
    private static final Logger log = Log.getChain();

    private static final String VALUES_SEP = ",";

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(Config.APP_NAME, OPTS);
    }

    public static void main(String[] args) {
        try {
            final String[] mockArgs = {
                    "-" + CommandOptions.SPEC_S, "/home/daniel/Code/RESTyle/Engine/src/main/resources/examples/spec.json",
                    "-" + CommandOptions.PLUGINS_S, "MysqlCreationScript" + VALUES_SEP + "PhpLumen",
                    "-" + CommandOptions.OUT_S, "/home/daniel/Code/RESTyle_output"
            };

            // Set up command-line
            // -------------------
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = null;
            try {
                cmd = parser.parse(OPTS, mockArgs);
            } catch (ParseException e) {
                log.error("Error parsing the command-line options", e);
            }
            assert cmd != null;


            // Check presence of "privileged" commands
            // ---------------------------------------

            // Get available plugins (it will be needed later anyway)
            Reflections reflections = new Reflections("es.berry.restyle.generators");
            final Set<Class<? extends Generator>> concreteGeneratorsSet = reflections.getSubTypesOf(Generator.class);

            List<String> availablePlugins = new ArrayList<>();
            for (Class<? extends Generator> gen : concreteGeneratorsSet)
                availablePlugins.add(gen.getSimpleName());

            if (cmd.hasOption(CommandOptions.LIST_PLUGINS_S)) {
                System.out.println(Strings.list(availablePlugins));
                System.exit(0);
            }


            // Validate arguments
            // ------------------
            final String specPath = cmd.getOptionValue(CommandOptions.SPEC_S);
            if (Strings.isEmpty(specPath))
                log.error("A value must be specified for option " + CommandOptions.SPEC_L + ".");

            final File specFile = new File(specPath);
            if (!specFile.exists() || !specFile.isFile())
                log.error("The specification file does not exist.");

            final String outputPath = cmd.getOptionValue(CommandOptions.OUT_S);
            if (Strings.isEmpty(outputPath))
                log.error("A value must be specified for option " + CommandOptions.OUT_L + ".");

            final File outputDir = new File(outputPath);
            if (outputDir.exists()) {
                if (outputDir.isFile())
                    log.error("Only a directory can be specified as output.");
            } else if (!outputDir.mkdirs())
                log.error("The output directory does not exist and could not be created.");


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


            // Validate JSON and resolve references ($ref)
            // -------------------------------------------
            JsonNode specNode = null;
            try {
                // specNode = mapper.readTree(specFile);
                specNode = Json.resolveReferences(specFile, mapper);
            } catch (JsonProcessingException e) {
                // Will inform about JSON validation errors
                log.error("Error processing the specification file", e);
            } catch (IOException e) {
                log.error("Error reading the specification file (or a file referenced by it)", e);
            }
            assert specNode != null;

            // Resolve baseUrl
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
                if (!report.isSuccess()) { // FIXME: type resolution happens later, so this will fail if it was needed
                    System.out.println("The specification provided does not conform the meta-specification defined for it:");

                    for (ProcessingMessage msg : report)
                        System.out.print(msg);

                    System.exit(1);
                }
            } catch (IOException e) {
                log.error("The JSON schema file could not be read", e);
            } catch (ProcessingException e) {
                log.error("An error happened when processing the JSON schema file", e);
            }


            // Load specification into Java classes and complete it
            // ----------------------------------------------------
            Spec spec = null;
            try {
                spec = mapper.treeToValue(specNode, Spec.class);
            } catch (JsonProcessingException e) {
                log.error("Error processing the file:\n" + e.getOriginalMessage());
            }
            spec = new Completor(spec).addDefaultValues().getSpec();
            spec = new FieldsTypeResolver(spec).resolve().getSpec();
            new AdvanceValidator(spec).validate();

//            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec));


            // Load and execute plugins
            // ------------------------
            List<String> selectedPlugins = Arrays.asList(cmd.getOptionValue(CommandOptions.PLUGINS_S).split(VALUES_SEP));
            for (String selectedPlugin : selectedPlugins)
                if (!availablePlugins.contains(selectedPlugin))
                    log.error("The plugin " + Strings.surround(selectedPlugin, "\"") + " is not in the list of available plugins. "
                            + "Please, select one of the following:\n" + Strings.list(availablePlugins));

            List<Class<? extends Generator>> concreteGenerators = new ArrayList<>();
            for (Class<? extends Generator> c : concreteGeneratorsSet)
                if (selectedPlugins.contains(c.getSimpleName()))
                    concreteGenerators.add(c);

            // Sort the plugins according to the exact order they were provided
            // IDEA: resolve dependencies first, so that the final user can forget about the plugins' order
            Collections.sort(concreteGenerators, (left, right) -> compare(
                    selectedPlugins.indexOf(left.getSimpleName()), selectedPlugins.indexOf(right.getSimpleName())
            ));

            Generator prevGen = null;

            for (Class<? extends Generator> genClass : concreteGenerators)
                try {
                    Generator gen = genClass.getConstructor(Spec.class, File.class).newInstance(spec, outputDir);

                    if (prevGen == null && gen.getPrevGeneratorInterface() != null)
                        log.error("The plugin " + genClass.getSimpleName() + " depends on a previous plugin, but "
                                + "none was provided");

                    if (prevGen != null && gen.getPrevGeneratorInterface() != null &&
                            !gen.getPrevGeneratorInterface().isAssignableFrom(prevGen.getClass()))
                        log.error("The plugin " + genClass.getSimpleName() + ", which depends on the plugin "
                                + prevGen.getClass().getSimpleName() + ", needs it to implement the interface "
                                + gen.getPrevGeneratorInterface().getSimpleName() + ", which it's not happening");

                    gen.setPrevGenerator(prevGen);
                    gen.generate();
                    prevGen = gen;
                } catch (InstantiationException e) {
                    log.broke("Impossible to instantiate plugin class " + genClass.getSimpleName(), e);
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