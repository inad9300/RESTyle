package es.berry.restyle.exceptions;

/**
 * Exception to represent problems encountered during plugins' execution.
 */
public class PluginException extends RuntimeException {

    public PluginException() {
        super("Some problem occurred inside a plugin.");
    }

    public PluginException(String msg) {
        super(msg);
    }

    public PluginException(String msg, Throwable t) {
        super(msg, t);
    }
}
