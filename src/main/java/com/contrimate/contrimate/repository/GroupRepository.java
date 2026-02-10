package com.contrimate.contrimate.repository;

import com.contrimate.contrimate.entity.AppGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- Ye import zaroori hai
import org.springframework.data.repository.query.Param; // <-- Ye bhi import karo
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<AppGroup, Long> {

    // 🔥 Hum automatic query ki jagah apni manually likhi hui Query use karenge
    // Logic: "Wo groups dhoondo jahan ye userId 'memberIds' list ka hissa hai"
    
    @Query("SELECT g FROM AppGroup g WHERE :userId MEMBER OF g.memberIds")
    List<AppGroup> findByMemberIdsContaining(@Param("userId") Long userId);

}