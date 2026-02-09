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

        // 2. DESIGNER HTML TEMPLATE (Professional Look) 🎨
        // Hum single quotes (') use kar rahe hain taaki JSON break na ho.
        String htmlContent = "<div style='font-family: Helvetica,Arial,sans-serif;min-width:1000px;overflow:auto;line-height:2'>"
                + "<div style='margin:50px auto;width:70%;padding:20px 0'>"
                + "<div style='border-bottom:1px solid #eee'>"
                + "  <a href='' style='font-size:1.4em;color: #10b981;text-decoration:none;font-weight:600'>ContriMate</a>"
                + "</div>"
                + "<p style='font-size:1.1em'>Hi User,</p>"
                + "<p>Thank you for choosing ContriMate. Use the following OTP to complete your verification. This code is valid for 10 minutes.</p>"
                + "<h2 style='background: #10b981;margin: 0 auto;width: max-content;padding: 0 10px;color: #fff;border-radius: 4px;'>" + otp + "</h2>"
                + "<p style='font-size:0.9em;'>Regards,<br />The ContriMate Team</p>"
                + "<hr style='border:none;border-top:1px solid #eee' />"
                + "<div style='float:right;padding:8px 0;color:#aaa;font-size:0.8em;line-height:1;font-weight:300'>"
                + "  <p>ContriMate Inc</p>"
                + "  <p>Secure Verification</p>"
                + "</div>"
                + "</div>"
                + "</div>";

        // 3. Prepare JSON Body for Brevo API
        // HTML content ko JSON mein daalne ke liye format kar rahe hain
        String jsonBody = String.format(
            "{\"sender\":{\"name\":\"ContriMate\",\"email\":\"%s\"},\"to\":[{\"email\":\"%s\"}],\"subject\":\"Your Verification Code\",\"htmlContent\":\"%s\"}",
            senderEmail, toEmail, htmlContent
        );

        // 4. Send HTTP Request (Port 443)
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