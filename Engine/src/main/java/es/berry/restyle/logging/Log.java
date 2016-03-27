package es.berry.restyle.logging;

import es.berry.restyle.core.Config;

final public class Log {

    private static Logger chain = _getChain();

    private static Logger _getChain() {
        Logger consoleLogger = new ConsoleLogger(Logger.INFO);
        Logger fileLogger = new FileLogger(Logger.ERROR, Config.LOG_FILE);
        Logger emailLogger = new EmailLogger(Logger.CRITICAL, "logging-test@berry.es", Config.DEV_EMAILS); // FIXME

        consoleLogger.setNext(fileLogger);
        fileLogger.setNext(emailLogger);

        return consoleLogger; // Return the one everyone else is linked to
    }

    public static Logger getChain() {
        return chain;
    }
}
