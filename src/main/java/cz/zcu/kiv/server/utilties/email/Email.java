package cz.zcu.kiv.server.utilties.email;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class Email {

    public static void sendMail(String to, String subject, String text) throws MessagingException {

        String from = "no-reply@WorkflowDesigner.com";

        String host = "smtp.mailtrap.io";

        final String username = "a28d8dc4f95082";

        final String password = "bf841a606d53e3";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        // SSL Port
        properties.put("mail.smtp.port", "2525");

        // enable authentication
        properties.put("mail.smtp.auth", "true");

        // SSL Factory
        properties.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");

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
            sendMail("pintojoey@gmail.com","New Account",EmailTemplates.getNewAccountPasswordEmail("Joey Pinto","pintojoey@gmail.com","12345"));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

