package es.berry.restyle.exceptions;

/**
 * Exception to represent that a problem was found regarding the semantic of the specification.
 */
public class SpecException extends RuntimeException {

    public SpecException() {
        super("A problem was found regarding the specification semantics.");
    }

    public SpecException(String msg) {
        super(msg);
    }

    public SpecException(String msg, Throwable t) {
        super(msg, t);
    }
}
