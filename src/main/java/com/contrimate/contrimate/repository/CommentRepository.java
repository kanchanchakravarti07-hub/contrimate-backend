package com.contrimate.contrimate.repository;

import com.contrimate.contrimate.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByExpenseIdOrderByCreatedAtAsc(Long expenseId);
}