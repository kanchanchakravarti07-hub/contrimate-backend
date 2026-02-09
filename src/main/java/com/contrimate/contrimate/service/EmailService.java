package com.contrimate.contrimate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private Map<String, String> otpStorage = new HashMap<>();

    // --- METHOD 1: Send OTP ---
    public String sendOtp(String toEmail) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(toEmail, otp);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // Note: Sender email ab 'application.properties' se uthaya jayega
            message.setFrom("ContriMate <noreply@contrimate.com>"); 
            message.setTo(toEmail);
            message.setSubject("ContriMate - Verification OTP");
            message.setText("Welcome!\n\nYour OTP is: " + otp + "\n\nThis expires in 10 minutes.");

            mailSender.send(message); // ✅ ASLI EMAIL BHEJEGA
            System.out.println("✅ Email sent successfully to " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Email Failed: " + e.getMessage());
            e.printStackTrace();
            throw e; // Error throw karo taaki frontend ko pata chale
        }

        return otp;
    }

    // --- METHOD 2: Wrapper ---
    public boolean sendOtpEmail(String toEmail) {
        try {
            sendOtp(toEmail);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // --- Verify ---
    public boolean verifyOtp(String email, String userOtp) {
        return otpStorage.containsKey(email) && otpStorage.get(email).equals(userOtp);
    }
}