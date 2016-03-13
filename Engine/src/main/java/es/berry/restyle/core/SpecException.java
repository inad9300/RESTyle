package es.berry.restyle.core;

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
