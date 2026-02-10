package com.contrimate.contrimate.controller;

import com.contrimate.contrimate.entity.Notification;
import com.contrimate.contrimate.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")

public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    // 1. Send Notification (Jab koi Request karega)
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody Map<String, Object> payload) {
        try {
            String message = (String) payload.get("message");
            Long userId = Long.valueOf(payload.get("recipientId").toString());

            Notification n = new Notification();
            n.setMessage(message);
            n.setUserId(userId); // ✅ Ab ye setUserId kaam karega
            n.setIsRead(false);
            
            notificationRepository.save(n);
            return ResponseEntity.ok("Sent");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error sending notification");
        }
    }

    // 2. Get User Notifications (Bell Icon click karne par)
    @GetMapping("/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    // 3. Mark as Read / Delete (Clear karne ke liye)
    @DeleteMapping("/clear/{id}")
    public void clearNotification(@PathVariable Long id) {
        notificationRepository.deleteById(id);
    }
}