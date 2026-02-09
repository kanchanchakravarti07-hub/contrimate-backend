package com.contrimate.contrimate.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "app_groups") // Table ka naam alag rakha
public class AppGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Long adminId; // Jo group banayega wo Admin

    @ElementCollection // List of IDs ko store karne ke liye
    private List<Long> memberIds;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public List<Long> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds; }
}