package es.berry.restyle.logging;

import es.berry.restyle.utils.Strings;

/**
 * Logs to the standard output.
 */
public class ConsoleLogger extends Logger {

    public ConsoleLogger(int minSeverity) {
        super(minSeverity);
    }

    protected void log(String message, String devMessage) {
        System.out.println(message);

        if (!Strings.isEmpty(devMessage))
            System.out.println("\n-> More info: " + devMessage);
    }
}
