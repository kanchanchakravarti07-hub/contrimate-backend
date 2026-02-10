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

    @Query("SELECT f FROM Friendship f WHERE (f.user = :user OR f.friend = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllAcceptedFriends(@Param("user") User user);

    List<Friendship> findByFriendAndStatus(User friend, String status);

    List<Friendship> findByUserAndStatus(User user, String status);

    @Query("SELECT f FROM Friendship f WHERE (f.user = :u1 AND f.friend = :u2) OR (f.user = :u2 AND f.friend = :u1)")
    Optional<Friendship> findByUserAndFriend(@Param("u1") User u1, @Param("u2") User u2);
    
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE (f.user = :u1 AND f.friend = :u2) OR (f.user = :u2 AND f.friend = :u1)")
    boolean existsByUsers(@Param("u1") User u1, @Param("u2") User u2);
}