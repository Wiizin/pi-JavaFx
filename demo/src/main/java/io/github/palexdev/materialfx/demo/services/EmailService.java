package io.github.palexdev.materialfx.demo.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USERNAME = "your-email@gmail.com";
    private static final String SMTP_PASSWORD = "your-email-password";  // ⚠️ Use App Password instead of real password

    public static void sendEmail(String recipientEmail, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("jery.wizin@gmail.com", "mibz ecmt bcnt scyz");
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("📧 Email sent successfully to " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
