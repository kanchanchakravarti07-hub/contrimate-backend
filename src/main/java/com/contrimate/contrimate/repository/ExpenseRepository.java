package com.contrimate.contrimate.repository;

import com.contrimate.contrimate.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    // Group ke kharche laane ke liye
    List<Expense> findByGroupId(Long groupId);

    // 🔥 NEW: User ke saare kharche (Jo maine pay kiye OR Jisme main shamil hoon)
    // 'LEFT JOIN FETCH' zaroori hai taaki splits ka data turant mile
    @Query("SELECT DISTINCT e FROM Expense e LEFT JOIN FETCH e.splits s WHERE e.paidBy.id = :userId OR s.user.id = :userId")
    List<Expense> findByUserId(@Param("userId") Long userId);
}