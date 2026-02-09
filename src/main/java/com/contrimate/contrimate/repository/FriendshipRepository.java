package com.contrimate.contrimate.repository;

import com.contrimate.contrimate.entity.Friendship;
import com.contrimate.contrimate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    // ✅ 1. SAARE DOST (Accepted - Chahe maine bheja ho ya usne)
    @Query("SELECT f FROM Friendship f WHERE (f.user = :user OR f.friend = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllAcceptedFriends(@Param("user") User user);

    // ✅ 2. INCOMING REQUESTS (Jo mujhe aayi hain)
    List<Friendship> findByFriendAndStatus(User friend, String status);

    // ✅ 3. OUTGOING REQUESTS (Jo maine bheji hain)
    List<Friendship> findByUserAndStatus(User user, String status);

    // 🔥 4. CHECK BOTH WAYS (Dono taraf check karo taaki duplicate na ho)
    // Ye check karega: Kya A->B ya B->A exist karta hai?
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f " +
           "WHERE (f.user = :u1 AND f.friend = :u2) OR (f.user = :u2 AND f.friend = :u1)")
    boolean existsByUsers(@Param("u1") User u1, @Param("u2") User u2);
    
    // ✅ 5. DELETE KE LIYE (Specific entry dhoondne ke liye)
    Optional<Friendship> findByUserAndFriend(User user, User friend);
}