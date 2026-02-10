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

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired private CommentRepository commentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ExpenseRepository expenseRepository;

    
    private Map<String, Object> toDto(Comment c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("text", c.getText());
        map.put("createdAt", c.getCreatedAt());
        
        if (c.getUser() != null) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", c.getUser().getId());
            u.put("name", c.getUser().getName());
            // No Profile Pic to keep chat fast
            map.put("user", u);
        }
        return map;
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<List<Map<String, Object>>> getComments(@PathVariable Long expenseId) {
        List<Comment> comments = commentRepository.findByExpenseIdOrderByCreatedAtAsc(expenseId);
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Comment c : comments) {
            response.add(toDto(c));
        }
        return ResponseEntity.ok(response);
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
            c.setCreatedAt(LocalDateTime.now());
            
            Comment saved = commentRepository.save(c);
            
            
            return ResponseEntity.ok(toDto(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error adding comment");
        }
    }
}