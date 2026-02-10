package com.contrimate.contrimate.controller;

import com.contrimate.contrimate.entity.AppGroup;
import com.contrimate.contrimate.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // 3. Update Group (Members add/remove)
    @PutMapping("/update/{id}")
    public AppGroup updateGroup(@PathVariable Long id, @RequestBody AppGroup updatedGroup) {
        return groupRepository.findById(id).map(group -> {
            group.setName(updatedGroup.getName());
            group.setMemberIds(updatedGroup.getMemberIds());
            return groupRepository.save(group);
        }).orElse(null);
    }

    // 4. Delete Group
    @DeleteMapping("/delete/{id}")
    public void deleteGroup(@PathVariable Long id) {
        groupRepository.deleteById(id);
    }
}