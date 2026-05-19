package com.pageturner.service;

import com.pageturner.model.Order;
import com.pageturner.model.OrderItem;
import com.pageturner.model.User;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailNotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${MAIL_USERNAME}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendOrderStatusEmail(Order order, String subject, String messageText) {
        String html = buildOrderEmail(
                order,
                subject,
                messageText,
                getStatusMessage(order.getStatus()),
                getStatusColor(order.getStatus())
        );

        sendHtmlEmail(order.getUser().getEmail(), subject, html);
    }

    public void sendWelcomeEmail(User user) {
        String html = buildSimpleEmail(
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
        String html = buildOrderEmail(
                order,
                "Order Confirmed #" + order.getOrderNumber(),
                "Your order has been placed successfully.",
                "Thank you for shopping with PageTurner. We will notify you when your order status changes.",
                "#2ECC71"
        );

        sendHtmlEmail(
                order.getUser().getEmail(),
                "Order Confirmed - PageTurner #" + order.getOrderNumber(),
                html
        );
    }

    private String buildOrderEmail(Order order, String title, String mainMessage, String extraMessage, String accentColor) {
        StringBuilder itemsHtml = new StringBuilder();

        for (OrderItem item : order.getItems()) {
            String coverUrl = item.getBook().getCoverUrl();

            if (coverUrl == null || coverUrl.isBlank()) {
                coverUrl = baseUrl + "/images/book-placeholder.png";
            } else if (coverUrl.startsWith("/")) {
                coverUrl = baseUrl + coverUrl;
            }

            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            itemsHtml.append("""
                    <tr>
                        <td style="padding:12px;border-bottom:1px solid #eee;">
                            <img src="%s" width="70" height="100" style="object-fit:cover;border-radius:8px;">
                        </td>
                        <td style="padding:12px;border-bottom:1px solid #eee;">
                            <h4 style="margin:0;color:#1A202C;">%s</h4>
                            <p style="margin:5px 0;color:#718096;">Author: %s</p>
                            <p style="margin:5px 0;color:#718096;">Quantity: %d</p>
                        </td>
                        <td style="padding:12px;border-bottom:1px solid #eee;text-align:right;font-weight:bold;">
                            Rs. %s
                        </td>
                    </tr>
                    """.formatted(
                    coverUrl,
                    item.getBook().getTitle(),
                    item.getBook().getAuthor(),
                    item.getQuantity(),
                    itemTotal
            ));
        }

        return """
                <div style="font-family:Arial,sans-serif;max-width:650px;margin:0 auto;background:#ffffff;border-radius:14px;overflow:hidden;box-shadow:0 5px 25px rgba(0,0,0,0.12);">
                    
                    <div style="background:%s;padding:30px;text-align:center;">
                        <h1 style="color:white;margin:0;font-size:28px;">📚 PageTurner</h1>
                        <p style="color:white;margin:8px 0 0;">Online Bookstore</p>
                    </div>

                    <div style="padding:35px;">
                        <h2 style="color:#1A202C;margin-top:0;">%s</h2>

                        <p style="font-size:16px;color:#4A5568;">Hi %s,</p>
                        <p style="font-size:16px;color:#4A5568;">%s</p>
                        <p style="font-size:16px;color:#4A5568;">%s</p>

                        <div style="background:#F7FAFC;border-left:5px solid %s;padding:18px;margin:25px 0;border-radius:8px;">
                            <p style="margin:6px 0;"><b>Order Number:</b> #%s</p>
                            <p style="margin:6px 0;"><b>Order Status:</b> %s</p>
                            <p style="margin:6px 0;"><b>Total Amount:</b> Rs. %s</p>
                            <p style="margin:6px 0;"><b>Shipping Address:</b> %s</p>
                        </div>

                        <h3 style="color:#1A202C;">Ordered Books</h3>

                        <table style="width:100%%;border-collapse:collapse;">
                            %s
                        </table>

                        <div style="text-align:center;margin:35px 0;">
                            <a href="%s/orders/%d" style="background:%s;color:white;padding:14px 30px;border-radius:8px;text-decoration:none;font-weight:bold;">
                                View Your Order
                            </a>
                        </div>

                        <p style="font-size:14px;color:#718096;">
                            If you have any questions, please contact PageTurner support.
                        </p>
                    </div>

                    <div style="background:#F7FAFC;padding:20px;text-align:center;color:#718096;font-size:14px;">
                        © 2026 PageTurner Bookstore. All rights reserved.
                    </div>
                </div>
                """.formatted(
                accentColor,
                title,
                order.getUser().getName(),
                mainMessage,
                extraMessage,
                accentColor,
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                itemsHtml,
                baseUrl,
                order.getId(),
                accentColor
        );
    }

    private String buildSimpleEmail(String title, String greeting, String body, String btnText, String btnUrl, String accentColor) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);">
                    <div style="background:%s;padding:30px;text-align:center;">
                        <h1 style="color:white;margin:0;font-size:24px;">📚 PageTurner</h1>
                    </div>
                    <div style="padding:40px;">
                        <h2 style="color:#1A202C;">%s</h2>
                        <p style="color:#4A5568;font-size:16px;">%s</p>
                        <p style="color:#4A5568;font-size:16px;">%s</p>
                        <div style="text-align:center;margin:30px 0;">
                            <a href="%s" style="background:%s;color:white;padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:bold;font-size:16px;">%s</a>
                        </div>
                    </div>
                    <div style="background:#F7FAFC;padding:20px;text-align:center;color:#718096;font-size:14px;">
                        © 2026 PageTurner Bookstore. All rights reserved.
                    </div>
                </div>
                """.formatted(accentColor, title, greeting, body, btnUrl, accentColor, btnText);
    }

    private String getStatusMessage(String status) {
        if (status == null) return "Your order status has been updated.";

        return switch (status) {
            case "Processing" -> "Good news! Your order is now being prepared by our team.";
            case "Shipped" -> "Your books are on the way. Please keep your contact number active.";
            case "Delivered" -> "Your order has been delivered successfully. We hope you enjoy your books!";
            case "Cancelled" -> "Your order has been cancelled. Please contact support if this was unexpected.";
            default -> "Your order status has been updated.";
        };
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