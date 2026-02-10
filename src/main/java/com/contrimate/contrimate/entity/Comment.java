package com.contrimate.contrimate.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore; // 🔥 Import Zaroori hai
import java.time.LocalDateTime;

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    @JsonIgnore // 🔥 YE CRITICAL HAI: Isse recursion rukega
    private Expense expense;

    // --- GETTERS & SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Expense getExpense() { return expense; }
    public void setExpense(Expense expense) { this.expense = expense; }
}