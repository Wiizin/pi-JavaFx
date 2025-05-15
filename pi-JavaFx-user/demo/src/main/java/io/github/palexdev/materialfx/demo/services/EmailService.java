package io.github.palexdev.materialfx.demo.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

import io.github.palexdev.materialfx.demo.model.Order;
import java.util.List;

public class EmailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USERNAME = "your-email@gmail.com";
    private static final String SMTP_PASSWORD = "your-email-password";  // ⚠️ Use App Password instead of real password

    public void sendConfirmationEmail(String recipientEmail, String subject, String body) {
        sendEmail(recipientEmail, subject, body);
    }
    
    // New method to send order confirmation with details
    public void sendOrderConfirmationEmail(String recipientEmail, List<Order> orders, String phoneNumber, String homeAddress) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Thank you for your order!\n\n");
        emailBody.append("ORDER SUMMARY:\n");
        emailBody.append("=============\n\n");
        
        double totalAmount = 0.0;
        
        // Add each order item
        for (Order order : orders) {
            if (!"Paid".equalsIgnoreCase(order.getStatus())) {
                emailBody.append(String.format("Product: %s\n", order.getProductName()));
                emailBody.append(String.format("Quantity: %d\n", order.getQuantity()));
                emailBody.append(String.format("Price: $%.2f\n\n", order.getTotalPrice()));
                totalAmount += order.getTotalPrice();
            }
        }
        
        // Add total and delivery information
        emailBody.append(String.format("Total Amount: $%.2f\n\n", totalAmount));
        emailBody.append("DELIVERY INFORMATION:\n");
        emailBody.append("===================\n\n");
        emailBody.append(String.format("Phone Number: %s\n", phoneNumber));
        emailBody.append(String.format("Delivery Address: %s\n\n", homeAddress));
        
        // Add footer
        emailBody.append("Thank you for shopping with Sportify!\n");
        emailBody.append("If you have any questions, please contact our customer service.");
        
        sendEmail(recipientEmail, "Your Sportify Order Confirmation", emailBody.toString());
    }
    
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
