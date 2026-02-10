package com.contrimate.contrimate.controller;

import com.contrimate.contrimate.entity.Notification;
import com.contrimate.contrimate.entity.User; // Import zaroori hai
import com.contrimate.contrimate.repository.NotificationRepository;
import com.contrimate.contrimate.repository.UserRepository; // Import zaroori hai
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody Map<String, Object> payload) {
        try {
            String message = (String) payload.get("message");
            
            
            Long userId = Long.valueOf(payload.get("recipientId").toString());

            
            User recipient = userRepository.findById(userId).orElse(null);

            if (recipient != null) {
                Notification n = new Notification();
                n.setMessage(message);
                n.setUser(recipient); 
                n.setIsRead(false);
                n.setCreatedAt(LocalDateTime.now());
                
                notificationRepository.save(n);
                return ResponseEntity.ok("Sent");
            } else {
                return ResponseEntity.badRequest().body("User not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error sending notification");
        }
    }

    @GetMapping("/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @GetMapping("/unread-count/{userId}")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationRepository.countByUserIdAndIsReadFalse(userId));
    }

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