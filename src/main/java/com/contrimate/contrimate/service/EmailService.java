package com.contrimate.contrimate.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    private Map<String, String> otpStorage = new HashMap<>();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String sendOtp(String toEmail) {
        // 1. Generate OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(toEmail, otp);

        // 2. Prepare JSON Body for Brevo API
        String jsonBody = String.format(
            "{\"sender\":{\"name\":\"ContriMate\",\"email\":\"%s\"},\"to\":[{\"email\":\"%s\"}],\"subject\":\"Your OTP Code\",\"htmlContent\":\"<p>Your OTP is: <b>%s</b></p>\"}",
            senderEmail, toEmail, otp
        );

        // 3. Send HTTP Request (Port 443 - Never Blocked)
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("accept", "application/json")
                .header("api-key", apiKey)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                System.out.println("✅ Email Sent via API to: " + toEmail);
            } else {
                System.err.println("❌ API Error: " + response.body());
                throw new RuntimeException("Failed to send email via API");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }

        return otp;
    }

    public boolean sendOtpEmail(String toEmail) {
        try {
            sendOtp(toEmail);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verifyOtp(String email, String userOtp) {
        return otpStorage.containsKey(email) && otpStorage.get(email).equals(userOtp);
    }
}