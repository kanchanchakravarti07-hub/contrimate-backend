package com.contrimate.contrimate.repository;

import com.contrimate.contrimate.entity.Friendship;
import com.contrimate.contrimate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    @Query("SELECT f FROM Friendship f WHERE (f.user = :user OR f.friend = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllAcceptedFriends(@Param("user") User user);

    // Incoming Requests (Jo mujhe aayi hain)
    List<Friendship> findByFriendAndStatus(User friend, String status);

    // 🔥 NEW: Outgoing Requests (Jo maine bheji hain)
    List<Friendship> findByUserAndStatus(User user, String status);

    // Check existing
    boolean existsByUserAndFriend(User user, User friend);
    
    // Delete ke liye exact friendship dhundna
    Optional<Friendship> findByUserAndFriend(User user, User friend);
}