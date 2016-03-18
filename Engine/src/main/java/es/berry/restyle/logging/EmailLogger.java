package es.berry.restyle.logging;

import es.berry.restyle.core.Config;
import es.berry.restyle.utils.Strings;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailLogger extends Logger { // FIXME: not sending emails...

    private String from;
    private String[] to;

    public EmailLogger(int minSeverity, String from, String[] to) {
        super(minSeverity);
        this.from = from;
        this.to = to;
    }

    protected void log(String message, String devMessage) {
        Properties sysProps = System.getProperties();

        // Setup mail server (assuming the emails are being sent from localhost)
        sysProps.setProperty("mail.smtp.host", "localhost");

        Session session = Session.getDefaultInstance(sysProps);

        try {
            MimeMessage email = new MimeMessage(session);

            email.setFrom(new InternetAddress(this.from));

//            email.addRecipients(Message.RecipientType.TO, Strings.join(this.to, ","));
            email.addRecipient(Message.RecipientType.TO, new InternetAddress(this.to[0]));

            email.setSubject(Config.APP_NAME + " logging: " + Strings.cut(message, 80));

            String finalMsg = message;
            if (!Strings.isEmpty(devMessage))
                finalMsg += "\n\n-> More info:\n" + devMessage;

            email.setText(finalMsg);

            Transport.send(email);
//            System.out.print(email);
        } catch (MessagingException e) {
//            System.out.println("-> " +  e.getMessage());
//            e.printStackTrace();
            // Nothing to do if the logging itself fails... :/
        }
    }
}
