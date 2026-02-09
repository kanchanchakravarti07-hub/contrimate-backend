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
        // 1. OTP Generate karo
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        // 2. Storage mein daalo taaki Verify ho sake
        otpStorage.put(toEmail, otp);

        // 3. Email Message banao (Bas dikhawe ke liye)
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2314114kanchan.2023cse@gmail.com");
        message.setTo(toEmail);
        message.setSubject("ContriMate - Your Verification OTP");
        message.setText("Welcome to ContriMate!\n\nYour OTP is: " + otp);

        // 🛑 ASLI EMAIL MAT BHEJO (Timeout se bachne ke liye)
        // mailSender.send(message); 

        // ✅ LOGS MEIN PRINT KARO (Railway Dashboard par dikhega)
        System.out.println("\n==================================================");
        System.out.println("🚀 [FAKE EMAIL SENT] To: " + toEmail);
        System.out.println("🔑 YOUR OTP IS: " + otp);
        System.out.println("==================================================\n");

        return otp;
    }

    // --- METHOD 2: For AuthController (Returns Boolean) ---
    public boolean sendOtpEmail(String toEmail) {
        try {
            sendOtp(toEmail); // Upar wala logic use karega
            return true;      // Hamesha True return karega (Success)
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Verify Method ---
    public boolean verifyOtp(String email, String userOtp) {
        if (otpStorage.containsKey(email) && otpStorage.get(email).equals(userOtp)) {
            // OTP match ho gaya!
            return true;
        }
        return false;
    }
}