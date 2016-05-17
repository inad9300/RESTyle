package es.berry.restyle.logging;

import es.berry.restyle.core.Config;

/**
 * Simple wrapper to get the default log chain quickly.
 */
final public class Log {

    private static Logger chain = buildChain();

    /**
     * Concatenates different loggers, assigning each of them a level.
     */
    private static Logger buildChain() {
        Logger consoleLogger = new ConsoleLogger(Logger.INFO);
        Logger fileLogger = new FileLogger(Logger.ERROR, Config.LOG_FILE);

        consoleLogger.setNext(fileLogger);

        return consoleLogger; // Return the one everyone else is linked to
    }

    /**
     * Return the default chain of loggers.
     */
    public static Logger getChain() {
        return chain;
    }
}
