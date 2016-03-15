package es.berry.restyle.logging;

// IDEA: consider the environment (dev, test, prod)

public abstract class Logger {

    public static final int DEBUG = 0;
    public static final int INFO = 1;
    public static final int ERROR = 2;

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
        else if (req.getSeverity() == Logger.ERROR)
            System.exit(1); // If an error occurred and there are no more handlers, exit informing the OS
    }

    public void debug(String msg) {
        handleRequest(new LogRequest(Logger.DEBUG, msg));
    }

    public void info(String msg) {
        handleRequest(new LogRequest(Logger.INFO, msg));
    }

    public void error(String msg) {
        handleRequest(new LogRequest(Logger.ERROR, msg));
    }

    protected abstract void log(String message, String devMessage);
}
