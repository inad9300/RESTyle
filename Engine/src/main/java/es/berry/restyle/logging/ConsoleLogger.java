package es.berry.restyle.logging;

public class ConsoleLogger extends Logger {

    public ConsoleLogger(int minSeverity) {
        super(minSeverity);
    }

    protected void log(String message, String devMessage) {
        System.out.println(message); // Ignore the developer message
    }
}
