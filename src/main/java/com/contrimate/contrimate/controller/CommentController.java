package com.contrimate.contrimate.controller;

import com.contrimate.contrimate.entity.Comment;
import com.contrimate.contrimate.entity.Expense;
import com.contrimate.contrimate.entity.User;
import com.contrimate.contrimate.repository.CommentRepository;
import com.contrimate.contrimate.repository.ExpenseRepository;
import com.contrimate.contrimate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime; // 🔥 Import zaroori hai
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")

public class CommentController {

    @Autowired private CommentRepository commentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ExpenseRepository expenseRepository;

    @GetMapping("/{expenseId}")
    public List<Comment> getComments(@PathVariable Long expenseId) {
        return commentRepository.findByExpenseIdOrderByCreatedAtAsc(expenseId);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addComment(@RequestBody Map<String, Object> payload) {
        try {
            String text = (String) payload.get("text");
            Long userId = Long.valueOf(payload.get("userId").toString());
            Long expenseId = Long.valueOf(payload.get("expenseId").toString());

            User user = userRepository.findById(userId).orElseThrow();
            Expense expense = expenseRepository.findById(expenseId).orElseThrow();

            Comment c = new Comment();
            c.setText(text);
            c.setUser(user);
            c.setExpense(expense);
            c.setCreatedAt(LocalDateTime.now()); // 🔥 Message ka time save karo
            
            return ResponseEntity.ok(commentRepository.save(c));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding comment");
        }
    }
}