package com.contrimate.contrimate.repository;

import com.contrimate.contrimate.entity.AppGroup; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // <-- Ye import zaroori hai List ke liye

@Repository
public interface GroupRepository extends JpaRepository<AppGroup, Long> {
    
    // 🔥 Ye method add karna zaroori hai taaki controller ise call kar sake
    List<AppGroup> findByMemberIdsContaining(Long userId);

}