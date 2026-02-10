package com.contrimate.contrimate.controller;

import com.contrimate.contrimate.entity.Comment;
import com.contrimate.contrimate.entity.Expense;
import com.contrimate.contrimate.entity.ExpenseSplit;
import com.contrimate.contrimate.entity.Notification;
import com.contrimate.contrimate.entity.User;
import com.contrimate.contrimate.repository.CommentRepository;
import com.contrimate.contrimate.repository.ExpenseRepository;
import com.contrimate.contrimate.repository.NotificationRepository;
import com.contrimate.contrimate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired private CommentRepository commentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private NotificationRepository notificationRepository;

    private Map<String, Object> toDto(Comment c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("text", c.getText());
        map.put("createdAt", c.getCreatedAt());
        
        if (c.getUser() != null) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", c.getUser().getId());
            u.put("name", c.getUser().getName());
            map.put("user", u);
        }
        return map;
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<List<Map<String, Object>>> getComments(@PathVariable Long expenseId) {
        List<Comment> comments = commentRepository.findByExpenseIdOrderByCreatedAtAsc(expenseId);
        List<Map<String, Object>> response = new ArrayList<>();
        for (Comment c : comments) response.add(toDto(c));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    @Transactional // 🔥 Transactional zaroori hai taaki dono (Comment + Notification) save hon
    public ResponseEntity<?> addComment(@RequestBody Map<String, Object> payload) {
        try {
            String text = (String) payload.get("text");
            Long userId = Long.valueOf(payload.get("userId").toString());
            Long expenseId = Long.valueOf(payload.get("expenseId").toString());

            User sender = userRepository.findById(userId).orElseThrow();
            Expense expense = expenseRepository.findById(expenseId).orElseThrow();

            // 1. Save Comment
            Comment c = new Comment();
            c.setText(text);
            c.setUser(sender);
            c.setExpense(expense);
            c.setCreatedAt(LocalDateTime.now());
            Comment saved = commentRepository.save(c);

            // 2. 🔥 ROBUST NOTIFICATION LOGIC
            Set<Long> recipientIds = new HashSet<>();

            // Payer ko add karo (Agar payer main nahi hu)
            if (expense.getPaidBy() != null) {
                recipientIds.add(expense.getPaidBy().getId());
            }

            // Splits se logo ko nikalo
            if (expense.getSplits() != null) {
                for (ExpenseSplit split : expense.getSplits()) {
                    if (split.getUser() != null) {
                        recipientIds.add(split.getUser().getId());
                    }
                }
            }

            // Khud ko remove karo
            recipientIds.remove(sender.getId());

            // Notification Save karo
            for (Long rId : recipientIds) {
                User recipient = userRepository.findById(rId).orElse(null);
                if (recipient != null) {
                    Notification n = new Notification();
                    n.setUser(recipient);
                    n.setMessage(sender.getName() + " commented: " + text);
                    n.setIsRead(false);
                    n.setCreatedAt(LocalDateTime.now());
                    notificationRepository.save(n);
                }
            }
            
            return ResponseEntity.ok(toDto(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}