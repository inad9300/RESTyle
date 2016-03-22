package es.berry.restyle.exceptions;

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
