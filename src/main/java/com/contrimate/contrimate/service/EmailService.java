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

    // Stores email -> OTP
    private Map<String, String> otpStorage = new HashMap<>();

    // --- METHOD 1: For UserController (Returns the OTP String) ---
    public String sendOtp(String toEmail) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(toEmail, otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2314114kanchan.2023cse@gmail.com");
        message.setTo(toEmail);
        message.setSubject("ContriMate - Your Verification OTP");
        message.setText("Welcome to ContriMate!\n\nYour OTP is: " + otp);

        mailSender.send(message);
        return otp;
    }

    // --- METHOD 2: For AuthController (Returns Boolean) ---
    // This fixes the error in AuthController.java
    public boolean sendOtpEmail(String toEmail) {
        try {
            sendOtp(toEmail); // Re-use the logic above
            return true;      // If no error, return true
        } catch (Exception e) {
            e.printStackTrace();
            return false;     // If error, return false
        }
    }

    // --- Verify Method ---
    // --- Verify Method ---
public boolean verifyOtp(String email, String userOtp) {
    if (otpStorage.containsKey(email) && otpStorage.get(email).equals(userOtp)) {
        // 👇 IS LINE KO COMMENT KAR DO (Taaki OTP 2 baar use ho sake)
        // otpStorage.remove(email); 
        return true;
    }
    return false;
}
}