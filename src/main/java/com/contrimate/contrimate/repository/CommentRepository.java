package com.contrimate.contrimate.repository;

import com.contrimate.contrimate.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Ek specific expense ke saare comments laao, purane pehle
    List<Comment> findByExpenseIdOrderByCreatedAtAsc(Long expenseId);
}