package com.contrimate.contrimate.repository;

import com.contrimate.contrimate.entity.AppGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<AppGroup, Long> {

    // 🔥 FIX: "MEMBER OF" ki jagah hum "JOIN" use kar rahe hain. 
    // Ye check karega ki kis group ki memberIds list mein ye userId hai.
    
    @Query("SELECT g FROM AppGroup g JOIN g.memberIds m WHERE m = :userId")
    List<AppGroup> findByMemberIdsContaining(@Param("userId") Long userId);

}