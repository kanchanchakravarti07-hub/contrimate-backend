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

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody Map<String, Object> payload) {
        try {
            String message = (String) payload.get("message");
            Long userId = Long.valueOf(payload.get("recipientId").toString());

            Notification n = new Notification();
            n.setMessage(message);
            n.setUserId(userId);
            n.setIsRead(false);
            // n.setCreatedAt(java.time.LocalDateTime.now()); // Uncomment if needed
            
            notificationRepository.save(n);
            return ResponseEntity.ok("Sent");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error sending notification");
        }
    }

    @GetMapping("/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 🔥 NEW: Badge Count (1-9+)
    @GetMapping("/unread-count/{userId}")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationRepository.countByUserIdAndIsReadFalse(userId));
    }

    // 🔥 NEW: Jab Notification page khule to Read mark karo
    @PutMapping("/mark-read/{userId}")
    public ResponseEntity<?> markAllRead(@PathVariable Long userId) {
        notificationRepository.markAllAsRead(userId);
        return ResponseEntity.ok("Marked as read");
    }
    
    @DeleteMapping("/clear/{id}")
    public void clearNotification(@PathVariable Long id) {
        notificationRepository.deleteById(id);
    }
}