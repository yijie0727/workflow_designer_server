package cz.zcu.kiv.server.utilties.email;

import cz.zcu.kiv.server.utilities.config.Conf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.*;
import javax.mail.internet.*;

public class Email {
    private static Log logger=LogFactory.getLog(Email.class);

    public static void sendMail(String to, String subject, String text) throws MessagingException {

        String from = Conf.getConf().getFromEmailAddress();

        final String username = Conf.getConf().getEmailUsername();

        final String password = Conf.getConf().getEmailPassword();

        // creating Session instance referenced to 
        // Authenticator object to pass in 
        // Session.getInstance argument
        Session session = Session.getDefaultInstance(Conf.getConf().getProperties(),
                new javax.mail.Authenticator() {

                    // override the getPasswordAuthentication
                    // method
                    protected PasswordAuthentication
                    getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

            // javax.mail.internet.MimeMessage class is mostly
            // used for abstraction.
            MimeMessage message = new MimeMessage(session);

            // header field of the header.
            message.setFrom(new InternetAddress(from));

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setContent(text,"text/html; charset=utf-8");

            // Send message
            Transport.send(message);
    }

}

