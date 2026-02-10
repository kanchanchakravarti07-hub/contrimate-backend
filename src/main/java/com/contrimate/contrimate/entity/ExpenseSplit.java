package com.contrimate.contrimate.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore; // 🔥 Import Zaroori hai

@Entity
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    @JsonIgnore // 🔥 YE BAHUT ZAROORI HAI: Isse recursion rukega aur app crash nahi karegi
    private Expense expense;

    private Double amount;
    
    // Kabhi-kabhi frontend 'amountOwed' expect karta hai
    private Double amountOwed; 

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Expense getExpense() { return expense; }
    public void setExpense(Expense expense) { this.expense = expense; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Double getAmountOwed() { return amountOwed; }
    public void setAmountOwed(Double amountOwed) { this.amountOwed = amountOwed; }
}