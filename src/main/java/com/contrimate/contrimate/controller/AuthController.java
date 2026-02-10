package com.contrimate.contrimate.controller;

import com.contrimate.contrimate.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-otp")
    public String sendOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        boolean sent = emailService.sendOtpEmail(email);
        return sent ? "OTP_SENT" : "ERROR";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");
        boolean isValid = emailService.verifyOtp(email, otp);
        return isValid ? "VERIFIED" : "INVALID";
    }
}