package es.berry.restyle.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FileLogger extends Logger {

    private String filename;

    public FileLogger(int minSeverity, String filename) {
        super(minSeverity);
        this.filename = filename;
    }

    protected void log(String message, String devMessage) {
        File logfile = new File(this.filename);
        try {
            if (!logfile.exists())
                logfile.createNewFile();

            PrintWriter writer = new PrintWriter(new FileWriter(logfile, true));

            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            writer.append(ts + " - " + message + "\n");
            if (devMessage != null && !devMessage.equals(""))
                writer.append("\t-> More information:\n" + devMessage + "\n");

            writer.close();
        } catch (IOException e) {
            // Nothing to be done if the logging itself fails... :/
        }
    }
}
