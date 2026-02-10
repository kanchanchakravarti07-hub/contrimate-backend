package com.contrimate.contrimate.repository;

import com.contrimate.contrimate.entity.AppGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<AppGroup, Long> {
    
    // 🔥 Ye line add karne se Group wala 500 error aur compilation error hat jayega
    @Query("SELECT g FROM AppGroup g JOIN g.memberIds m WHERE m = :userId")
    List<AppGroup> findGroupsByUserId(@Param("userId") Long userId);
}