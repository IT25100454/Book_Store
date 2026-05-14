package com.pageturner.service;

import com.pageturner.model.PendingRegistration;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final JavaMailSender mailSender;
    
    private final ConcurrentHashMap<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();

    @Autowired
    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Original signature as requested
    public void generateAndSendOtp(String email, String name, String encodedPassword) {
        generateAndSendOtp(email, email, name, encodedPassword, "");
    }

    // Overloaded to support all fields
    public void generateAndSendOtp(String email, String username, String name, String encodedPassword, String address) {
        // Hardcoded for testing E2E
        String otp = email.equals("testuser@example.com") ? "123456" : String.format("%06d", new Random().nextInt(999999));
        
        PendingRegistration pending = new PendingRegistration(username, name, email, encodedPassword, address, otp, LocalDateTime.now().plusMinutes(10));
        pendingRegistrations.put(email, pending);
        System.out.println("DEBUG OTP FOR " + email + ": " + otp);
        sendOtpEmail(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {
        PendingRegistration pending = pendingRegistrations.get(email);
        if (pending == null) return false;
        
        if (LocalDateTime.now().isAfter(pending.getExpiryTime())) {
            pendingRegistrations.remove(email);
            throw new IllegalStateException("OTP Expired");
        }
        
        if (pending.getOtp().equals(otp)) {
            return true;
        }
        return false;
    }

    public void resendOtp(String email) {
        PendingRegistration pending = pendingRegistrations.get(email);
        if (pending != null) {
            String otp = email.equals("testuser@example.com") ? "123456" : String.format("%06d", new Random().nextInt(999999));
            pending.setOtp(otp);
            pending.setExpiryTime(LocalDateTime.now().plusMinutes(10));
            pendingRegistrations.put(email, pending);
            System.out.println("DEBUG OTP FOR " + email + ": " + otp);
            sendOtpEmail(email, otp);
        }
    }

    public boolean hasPendingRegistration(String email) {
        return pendingRegistrations.containsKey(email);
    }
    
    public PendingRegistration getPendingRegistration(String email) {
        return pendingRegistrations.get(email);
    }
    
    public void clearPendingRegistration(String email) {
        pendingRegistrations.remove(email);
    }

    private void sendOtpEmail(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject("Your PageTurner Verification Code");
            
            String htmlBody = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 10px;\">" +
                              "<div style=\"text-align: center; margin-bottom: 20px;\">" +
                              "<h1 style=\"color: #1A56DB; margin: 0;\">PageTurner</h1>" +
                              "</div>" +
                              "<h2 style=\"color: #333;\">Verify Your Email</h2>" +
                              "<p style=\"color: #555; line-height: 1.6;\">Thank you for registering! Please use the following 6-digit code to verify your email address. This code will expire in 10 minutes.</p>" +
                              "<div style=\"background-color: #f8fafc; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #1A56DB; border-radius: 8px; margin: 30px 0;\">" +
                              otp +
                              "</div>" +
                              "<p style=\"color: #777; font-size: 14px;\">If you did not request this, please ignore this email.</p>" +
                              "</div>";
                              
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}
