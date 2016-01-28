package es.berry.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) {

        // Apache Commons CLI
        // ------------------

        Options opts = new Options();
        opts.addOption(
            Option
                .builder(CommandOptions.GLOBAL_CONFIG_S)
                .longOpt(CommandOptions.GLOBAL_CONFIG_L)
                .desc("path to the global configuration file")
                .required()
                .hasArg()
                .type(Integer.class)
                .build()
        );

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(opts, args);
            if (cmd.hasOption("g")) {
                String g = cmd.getOptionValue("g");
                if (g != null) {
                    System.out.println("g is present! " + g);
                }
            }
        } catch (ParseException e) {
            System.out.println("Error parsing the command-line options: " + e.getMessage());
        }

        // HelpFormatter formatter = new HelpFormatter();
        // formatter.printHelp("ant", options);


        // JSON/YAML parsing
        // -----------------

        try {
            JsonNode data = new ObjectMapper().readTree("{\"age\":22,\"numbers\":[20,21,22]}");
            // JsonNode data = new ObjectMapper(new YAMLFactory()).readTree("---\nage: 22\nnumbers:\n - 20\n - 21\n - 22\n");

            // Keys
            System.out.println("-----");
            Iterator names = data.fieldNames();
            while (names.hasNext()) {
                System.out.println(names.next());
            }

            // Values
            System.out.println("-----");
            Iterator allElems = data.elements();
            while (allElems.hasNext()) {
                System.out.println(allElems.next());
            }

            // Array items
            System.out.println("-----");
            Iterator elems = data.at("/numbers").elements();
            while (elems.hasNext()) {
                System.out.println(elems.next());
            }

            // Finding one element
            System.out.println("-----");
            int age = data.at("/numbers/2").asInt();
            System.out.println(age);

            System.out.print("-----");
        } catch (IOException e) {
            System.out.println("Impossible to read JSON data :( " + e.getMessage());
        }
    }
}
