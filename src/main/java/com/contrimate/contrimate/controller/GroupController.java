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

    @PostMapping("/add")
    public AppGroup createGroup(@RequestBody AppGroup group) {
        return groupRepository.save(group);
    }

    @GetMapping("/all")
    public List<AppGroup> getAllGroups() {
        return groupRepository.findAll();
    }

    @GetMapping("/my-groups")
    public List<AppGroup> getMyGroups(@RequestParam Long userId) {
        List<AppGroup> allGroups = groupRepository.findAll();
        List<AppGroup> myGroups = new ArrayList<>();
        
        for (AppGroup group : allGroups) {
            if (group.getMemberIds() != null && group.getMemberIds().contains(userId)) {
                myGroups.add(group);
            }
        }
        return myGroups;
    }

    @PutMapping("/update/{id}")
    public AppGroup updateGroup(@PathVariable Long id, @RequestBody AppGroup updatedGroup) {
        return groupRepository.findById(id).map(group -> {
            group.setName(updatedGroup.getName());
            group.setMemberIds(updatedGroup.getMemberIds());
            return groupRepository.save(group);
        }).orElse(null);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteGroup(@PathVariable Long id) {
        groupRepository.deleteById(id);
    }
}