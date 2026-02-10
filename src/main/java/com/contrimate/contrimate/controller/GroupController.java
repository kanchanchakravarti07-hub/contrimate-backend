package com.contrimate.contrimate.controller;

import com.contrimate.contrimate.entity.AppGroup;
import com.contrimate.contrimate.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    // 2. Get All Groups
    @GetMapping("/all")
    public List<AppGroup> getAllGroups() {
        return groupRepository.findAll();
    }

    // 🔥 3. GET MY GROUPS (Java Filter Logic - 100% Safe)
    @GetMapping("/my-groups")
    public List<AppGroup> getMyGroups(@RequestParam Long userId) {
        // Step A: Database se sab kuch le aao (No complex query)
        List<AppGroup> allGroups = groupRepository.findAll();
        
        // Step B: Java loop laga kar check karo user member hai ya nahi
        List<AppGroup> myGroups = new ArrayList<>();
        for (AppGroup group : allGroups) {
            // Null check zaroori hai taaki crash na ho
            if (group.getMemberIds() != null && group.getMemberIds().contains(userId)) {
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