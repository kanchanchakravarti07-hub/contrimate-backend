package com.contrimate.contrimate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class EmailService {

    // Railway Variable se Key uthayega
    @Value("${SENDGRID_API_KEY}") 
    private String apiKey;

    // Yahan hardcode kar rahe hain taaki error na aaye (Apni verified email likhna)
    private String senderEmail = "kanchanprajapati8059@gmail.com"; 

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, String> otpStorage = new HashMap<>();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String sendOtp(String toEmail) {
        try {
            // 1. OTP Generate Karo
            String otp = String.format("%06d", new Random().nextInt(999999));
            otpStorage.put(toEmail, otp);

            // 2. HTML Design (Wahi purana Green wala) 🎨
            String htmlContent = "<div style='font-family: Helvetica,Arial,sans-serif;min-width:1000px;overflow:auto;line-height:2'>"
                    + "<div style='margin:50px auto;width:70%;padding:20px 0'>"
                    + "<div style='border-bottom:1px solid #eee'>"
                    + "  <a href='' style='font-size:1.4em;color: #10b981;text-decoration:none;font-weight:600'>ContriMate</a>"
                    + "</div>"
                    + "<p style='font-size:1.1em'>Hi User,</p>"
                    + "<p>Use the following OTP to complete your verification. Valid for 10 minutes.</p>"
                    + "<h2 style='background: #10b981;margin: 0 auto;width: max-content;padding: 0 10px;color: #fff;border-radius: 4px;'>" + otp + "</h2>"
                    + "<p style='font-size:0.9em;'>Regards,<br />The ContriMate Team</p>"
                    + "<hr style='border:none;border-top:1px solid #eee' />"
                    + "</div></div>";

            // 3. SendGrid JSON Structure Prepare Karo
            Map<String, Object> personalizations = new HashMap<>();
            personalizations.put("to", List.of(Map.of("email", toEmail)));

            Map<String, Object> from = new HashMap<>();
            from.put("email", senderEmail);
            from.put("name", "ContriMate");

            Map<String, Object> content = new HashMap<>();
            content.put("type", "text/html");
            content.put("value", htmlContent);

            Map<String, Object> payload = new HashMap<>();
            payload.put("personalizations", List.of(personalizations));
            payload.put("from", from);
            payload.put("subject", "Your OTP Code");
            payload.put("content", List.of(content));

            String jsonBody = objectMapper.writeValueAsString(payload);

            // 4. API Request Bhejo (SendGrid URL)
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // SendGrid success hone par 202 code deta hai
            if (response.statusCode() == 202 || response.statusCode() == 200) {
                System.out.println("✅ Email Sent via SendGrid to: " + toEmail);
            } else {
                System.err.println("❌ API Error: " + response.body());
                System.out.println("⚠️ FALLBACK OTP (Logs): " + otp); // Agar fail hua to console me dikhega
            }

            return otp;

        } catch (Exception e) {
            e.printStackTrace();
            return otpStorage.get(toEmail);
        }
    }

    // --- Helper Methods ---
    public boolean sendOtpEmail(String toEmail) {
        sendOtp(toEmail);
        return true;
    }

    public boolean verifyOtp(String email, String userOtp) {
        return otpStorage.containsKey(email) && otpStorage.get(email).equals(userOtp);
    }
}