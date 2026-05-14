package com.pageturner.service;

import com.pageturner.model.Order;
import com.pageturner.model.User;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${MAIL_USERNAME}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendOrderStatusEmail(Order order, String subject, String messageText) {
        String html = buildEmailTemplate(
            subject,
            "Hi " + order.getUser().getName() + ",",
            messageText,
            "View Your Order",
            baseUrl + "/orders/" + order.getId(),
            getStatusColor(order.getStatus())
        );
        sendHtmlEmail(order.getUser().getEmail(), subject, html);
    }

    public void sendWelcomeEmail(User user) {
        String html = buildEmailTemplate(
            "Welcome to PageTurner! 📚",
            "Hi " + user.getName() + "!",
            "Your account has been created successfully. Start exploring thousands of books in our collection.",
            "Browse Books",
            baseUrl + "/books",
            "#1A56DB"
        );
        sendHtmlEmail(user.getEmail(), "Welcome to PageTurner!", html);
    }

    public void sendOrderConfirmationEmail(Order order) {
        String html = buildEmailTemplate(
            "Order Confirmed #" + order.getOrderNumber(),
            "Hi " + order.getUser().getName() + ",",
            "Your order #" + order.getOrderNumber() + " has been placed successfully! Total: $" + order.getTotalAmount() + ". We will notify you when it ships.",
            "Track Your Order",
            baseUrl + "/orders/" + order.getId(),
            "#2ECC71"
        );
        sendHtmlEmail(order.getUser().getEmail(), "Order Confirmed - PageTurner #" + order.getOrderNumber(), html);
    }

    private String buildEmailTemplate(String title, String greeting, String body, String btnText, String btnUrl, String accentColor) {
        return String.format(
            "<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1)\">" +
            "  <div style=\"background:%s;padding:30px;text-align:center\">" +
            "    <h1 style=\"color:white;margin:0;font-size:24px\">📚 PageTurner</h1>" +
            "  </div>" +
            "  <div style=\"padding:40px\">" +
            "    <h2 style=\"color:#1A202C\">%s</h2>" +
            "    <p style=\"color:#4A5568;font-size:16px\">%s</p>" +
            "    <p style=\"color:#4A5568;font-size:16px\">%s</p>" +
            "    <div style=\"text-align:center;margin:30px 0\">" +
            "      <a href=\"%s\" style=\"background:%s;color:white;padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:bold;font-size:16px\">%s</a>" +
            "    </div>" +
            "  </div>" +
            "  <div style=\"background:#F7FAFC;padding:20px;text-align:center;color:#718096;font-size:14px\">" +
            "    © 2026 PageTurner Bookstore. All rights reserved." +
            "  </div>" +
            "</div>",
            accentColor, title, greeting, body, btnUrl, accentColor, btnText
        );
    }

    private String getStatusColor(String status) {
        if (status == null) return "#1A56DB";
        return switch (status) {
            case "Processing" -> "#F59E0B";
            case "Shipped" -> "#1A56DB";
            case "Delivered" -> "#2ECC71";
            case "Cancelled" -> "#EF4444";
            default -> "#1A56DB";
        };
    }

    private void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Email send failed: " + e.getMessage());
        }
    }
}
