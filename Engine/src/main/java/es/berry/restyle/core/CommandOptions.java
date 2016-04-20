package es.berry.restyle.core;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Options available at the command line interface.
 */
final public class CommandOptions {
    public final static String SPEC_S = "s";
    public final static String SPEC_L = "spec";

    public final static String PLUGINS_S = "p";
    public final static String PLUGINS_L = "plugins";

    public final static String OUT_S = "o";
    public final static String OUT_L = "output";

    public final static String LIST_PLUGINS_S = "lp";
    public final static String LIST_PLUGINS_L = "list-plugins";

    // public final static String SHOW_ERRORS_S = "e";
    // public final static String SHOW_ERRORS_L = "display-errors";

    public static Options get() {
        Options opts = new Options();

        opts.addOption(
                Option
                        .builder(CommandOptions.SPEC_S)
                        .longOpt(CommandOptions.SPEC_L)
                        .desc("path to the specification file")
                        .required()
                        .hasArg()
                        .type(String.class)
                        .build()
        );

        opts.addOption(
                Option
                        .builder(CommandOptions.PLUGINS_S)
                        .longOpt(CommandOptions.PLUGINS_L)
                        .desc("names of the plugins to be applied to the specification, space-separated")
                        .required()
                        .hasArgs()
                        .type(String.class)
                        .build()
        );

        opts.addOption(
                Option
                        .builder(CommandOptions.LIST_PLUGINS_S)
                        .longOpt(CommandOptions.LIST_PLUGINS_L)
                        .desc("list all available plugins")
                        .type(Boolean.class)
                        .build()
        );

        /* IDEA (to hide exceptions by default):
        opts.addOption(
                Option
                        .builder(CommandOptions.SHOW_ERRORS_S)
                        .longOpt(CommandOptions.SHOW_ERRORS_L)
                        .desc("when an error happens, display a detailed, technical message describing the issue")
                        .type(Boolean.class)
                        .build()
        ); */

        opts.addOption(
                Option
                        .builder(CommandOptions.OUT_S)
                        .longOpt(CommandOptions.OUT_L)
                        .desc("directory to place the output files in")
                        .required()
                        .hasArg()
                        .type(String.class)
                        .build()
        );

        return opts;
    }
}
