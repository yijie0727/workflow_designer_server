package cz.zcu.kiv.server.utilties.email;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class Email {
    private static Log logger=LogFactory.getLog(Email.class);

    public static void sendMail(String to, String subject, String text) throws MessagingException {

        // Get system properties
        Properties properties = new Properties();
        try {
            properties.load(Email.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            logger.error("Could not find config.properties file");
            logger.fatal(e);
        }

        String from = properties.getProperty("fromEmailAddress");

        final String username = properties.getProperty("emailUsername");

        final String password = properties.getProperty("emailPassword");

        // creating Session instance referenced to 
        // Authenticator object to pass in 
        // Session.getInstance argument
        Session session = Session.getDefaultInstance(properties,
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

    public static void main(String[] args) {
        try {
            sendMail("pintojoey@gmail.com","New Account",Templates.getNewAccountPasswordEmail("Joey Pinto","pintojoey@gmail.com","12345"));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

