package com.contrimate.contrimate.repository;

import com.contrimate.contrimate.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // ✅ Ab ye query sahi chalegi kyunki Entity mein userId hai
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}