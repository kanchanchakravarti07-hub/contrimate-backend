package com.contrimate.contrimate.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.time.LocalDateTime;

@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private Double totalAmount;
    private String category; 
    private LocalDateTime createdAt; 

    @ManyToOne
    @JoinColumn(name = "paid_by_id")
    private User paidBy;

    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonIgnoreProperties("expenses")
    private AppGroup group;

    // 🔥 FIX: Yahan EAGER lagana zaroori hai, tabhi Notification jayegi
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("expense")
    private List<ExpenseSplit> splits;

    // Comments ko LAZY rakho ya default, taaki 'MultipleBagFetchException' na aaye
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("expense")
    private List<Comment> comments;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getPaidBy() { return paidBy; }
    public void setPaidBy(User paidBy) { this.paidBy = paidBy; }

    public AppGroup getGroup() { return group; }
    public void setGroup(AppGroup group) { this.group = group; }

    public List<ExpenseSplit> getSplits() { return splits; }
    public void setSplits(List<ExpenseSplit> splits) { this.splits = splits; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
}