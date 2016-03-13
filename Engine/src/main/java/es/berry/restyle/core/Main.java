package es.berry.restyle.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import es.berry.restyle.logging.ConsoleLogger;
import es.berry.restyle.logging.FileLogger;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.specification.Spec;
import es.berry.restyle.utils.Strings;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.Iterator;

final public class Main {

    private final static String APP_NAME = "RESTyle";
    private final static File PLUGINS_DIR = new File("plugins"); // FIXME

    private final static Options OPTS = CommandOptions.get();
    private final static Logger LOG = getLoggersChain();

//    private static void printHelp() {
//        HelpFormatter formatter = new HelpFormatter();
//        formatter.printHelp(APP_NAME, OPTS);
//    }

    private static Logger getLoggersChain() {
        Logger consoleLogger = new ConsoleLogger(Logger.DEBUG);
        // Logger fileLogger = new FileLogger(Logger.ERROR, "error_log");

        // consoleLogger.setNext(fileLogger);

        return consoleLogger; // Return the one everyone else is linked to
    }

    public static void main(String[] args) {
        try {
            final String[] mockArgs = {
                    "-" + CommandOptions.SPEC_S, "/home/daniel/Code/RESTyle/Engine/src/main/resources/examples/spec.json",
                    "-" + CommandOptions.PLUGINS_S, "",
                    "-" + CommandOptions.OUT_S, ""
            };

            // Set up command-line
            // -------------------
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = null;
            try {
                cmd = parser.parse(OPTS, mockArgs);
            } catch (ParseException e) {
                LOG.error("Error parsing the command-line options:\n" + e.getMessage());
            }


            // Validate arguments
            // ------------------
            final String specPath = cmd.getOptionValue(CommandOptions.SPEC_S);
            if (Strings.isEmpty(specPath))
                LOG.error("A value must be specified for option " + CommandOptions.SPEC_L + ".");

            final File specFile = new File(specPath);
            if (!specFile.exists() || !specFile.isFile())
                LOG.error("The specification file does not exist.");


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
                LOG.error("The specification file has an unsupported extension.");


            // Validate JSON, and prepare the baseUrl
            // --------------------------------------
            JsonNode specNode = null;
            try {
                specNode = mapper.readTree(specFile);
            } catch (JsonProcessingException e) {
                // Will inform about JSON validation errors
                LOG.error("Error processing the specification file:\n" + e.getMessage());
            }
            specNode = ((ObjectNode) specNode).put(
                    "baseUrl",
                    specNode.get("baseUrl").asText()
                            .replace("{version}", specNode.get("version").asText())
            );


            // Validate specification against schema
            // -------------------------------------
            final String schemaPath = "./src/main/resources/specification/schema.json";
            final JsonNode schemaNode = JsonLoader.fromFile(new File(schemaPath));
            final JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema(schemaNode);

            ProcessingReport report = schema.validate(specNode);
            if (!report.isSuccess()) {
                System.out.println("The specification provided does not conform the meta-specification defined for it:");
                System.out.println("--- BEGIN REPORT ---");

                Iterator it = report.iterator();
                while (it.hasNext())
                    System.out.print(it.next());

                System.out.println("--- END REPORT ---");
            }


            // Load specification into Java classes and complete it
            // ----------------------------------------------------
            Spec spec = null;
            try {
                spec = mapper.treeToValue(specNode, Spec.class);
            } catch (JsonProcessingException e) {
                LOG.error("Error processing the file:\n" + e.getOriginalMessage());
            }
            spec = new SpecCompletor(spec).addDefaultValues().getSpec();
            spec = new FieldsTypeResolver(spec).resolve().getSpec();

            System.out.println( mapper.writeValueAsString(spec) );

            // Load and execute plugins
            // ------------------------
            // TODO
//            PluginManager pm = new DefaultPluginManager(PLUGINS_DIR);
//            pm.loadPlugins();
//            pm.startPlugins();
//            pm.stopPlugins();
        } catch (Exception e) { // TODO: catch more specific exceptions, in more specific places
            e.printStackTrace();
            LOG.error("Sorry, an unexpected error occurred :( ...\n" + e.getMessage());
        }
    }
}