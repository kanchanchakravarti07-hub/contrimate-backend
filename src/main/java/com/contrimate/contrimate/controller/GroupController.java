package com.contrimate.contrimate.controller;

import com.contrimate.contrimate.entity.AppGroup;
import com.contrimate.contrimate.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList; // <--- Ye line upar imports mein add karo
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    // 1. Create Group
    @PostMapping("/add")
    public AppGroup createGroup(@RequestBody AppGroup group) {
        return groupRepository.save(group);
    }

    // 2. Get All Groups (Admin/Debug ke liye)
    @GetMapping("/all")
    public List<AppGroup> getAllGroups() {
        return groupRepository.findAll();
    }

    // 🔥 3. GET MY GROUPS (Ye missing tha - Isliye 404 aa raha tha)
    // 🔥 3. GET MY GROUPS (Java Logic - 100% Working Fix)
    @GetMapping("/my-groups")
    public List<AppGroup> getMyGroups(@RequestParam Long userId) {
        // 1. Database se saare groups le aao
        List<AppGroup> allGroups = groupRepository.findAll();
        
        // 2. Java loop chala kar check karo ki user kis group mein hai
        List<AppGroup> myGroups = new ArrayList<>();
        for (AppGroup group : allGroups) {
            if (group.getMemberIds().contains(userId)) {
                myGroups.add(group);
            }
        }
        return myGroups;
    }

    // 4. Update Group
    @PutMapping("/update/{id}")
    public AppGroup updateGroup(@PathVariable Long id, @RequestBody AppGroup updatedGroup) {
        return groupRepository.findById(id).map(group -> {
            group.setName(updatedGroup.getName());
            group.setMemberIds(updatedGroup.getMemberIds());
            return groupRepository.save(group);
        }).orElse(null);
    }

    // 5. Delete Group
    @DeleteMapping("/delete/{id}")
    public void deleteGroup(@PathVariable Long id) {
        groupRepository.deleteById(id);
    }
}