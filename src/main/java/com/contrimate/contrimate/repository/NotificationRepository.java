package com.contrimate.contrimate.repository;

import com.contrimate.contrimate.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // List fetch karne ke liye
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 🔥 Badge ke liye: Count Unread Notifications
    long countByUserIdAndIsReadFalse(Long userId);

    // 🔥 Page open hone par: Mark All as Read
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
    void markAllAsRead(@Param("userId") Long userId);
}