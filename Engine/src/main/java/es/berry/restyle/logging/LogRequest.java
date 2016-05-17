package es.berry.restyle.logging;

/**
 * Wrapper object to be passed to the loggers' handlers, encapsulating the information that may be of use for them.
 */
public class LogRequest {

    private int severity;
    private String message;
    private String devMessage;

    public LogRequest(int severity, String message) {
        this.severity = severity;
        this.message = message;
    }

    public LogRequest(int severity, String message, String devMessage) {
        this.severity = severity;
        this.message = message;
        this.devMessage = devMessage;
    }

    public int getSeverity() {
        return this.severity;
    }

    public String getMessage() {
        return this.message;
    }

    public String getDevMessage() {
        return this.devMessage;
    }
}
