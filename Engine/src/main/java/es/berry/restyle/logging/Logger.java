package es.berry.restyle.logging;

// IDEA: consider the environment (dev, test, prod)

import es.berry.restyle.utils.Strings;

public abstract class Logger {

    public static final int INFO = 0;
    public static final int WARN = 1;
    public static final int ERROR = 2;
    public static final int CRITICAL = 3;

    private Logger next;

    protected int minimumSeverity;

    public Logger(int minSeverity) {
        this.minimumSeverity = minSeverity;
    }

    public void setNext(Logger next) {
        this.next = next;
    }

    protected void handleRequest(LogRequest req) {
        if (req.getSeverity() >= this.minimumSeverity)
            this.log(req.getMessage(), req.getDevMessage());

        if (this.next != null)
            this.next.handleRequest(req);
        // If an error occurred and there are no more handlers, exit informing the OS
        else if (req.getSeverity() == Logger.ERROR)
            System.exit(1);
    }

    public void info(String msg) {
        handleRequest(new LogRequest(Logger.INFO, msg));
    }

    public void warn(String msg) {
        handleRequest(new LogRequest(Logger.WARN, msg));
    }

    public void error(String msg) {
        handleRequest(new LogRequest(Logger.ERROR, msg));
    }

    public void broke(String msg) {
        handleRequest(new LogRequest(Logger.CRITICAL, msg));
    }

    public void info(String introMsg, Exception e) {
        introMsg = Strings.isEmpty(introMsg) ? "" : introMsg + ": ";
        handleRequest(new LogRequest(Logger.INFO, introMsg + e.getMessage(), Strings.fromException(e)));
    }

    public void warn(String introMsg, Exception e) {
        introMsg = Strings.isEmpty(introMsg) ? "" : introMsg + ": ";
        handleRequest(new LogRequest(Logger.WARN, introMsg + e.getMessage(), Strings.fromException(e)));
    }

    public void error(String introMsg, Exception e) {
        introMsg = Strings.isEmpty(introMsg) ? "" : introMsg + ": ";
        handleRequest(new LogRequest(Logger.ERROR, introMsg + e.getMessage(), Strings.fromException(e)));
    }

    public void broke(String introMsg, Exception e) {
        introMsg = Strings.isEmpty(introMsg) ? "" : introMsg + ": ";
        handleRequest(new LogRequest(Logger.CRITICAL, introMsg + e.getMessage(), Strings.fromException(e)));
    }

    public void info(String msg, String devMsg) {
        handleRequest(new LogRequest(Logger.INFO, msg, devMsg));
    }

    public void warn(String msg, String devMsg) {
        handleRequest(new LogRequest(Logger.WARN, msg, devMsg));
    }

    public void error(String msg, String devMsg) {
        handleRequest(new LogRequest(Logger.ERROR, msg, devMsg));
    }

    public void broke(String msg, String devMsg) {
        handleRequest(new LogRequest(Logger.CRITICAL, msg, devMsg));
    }

    protected abstract void log(String message, String devMessage);
}
