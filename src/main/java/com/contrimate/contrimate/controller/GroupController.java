package com.contrimate.contrimate.controller;

import com.contrimate.contrimate.entity.AppGroup;
import com.contrimate.contrimate.entity.User;
import com.contrimate.contrimate.repository.GroupRepository;
import com.contrimate.contrimate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public AppGroup createGroup(@RequestBody AppGroup group) {
        return groupRepository.save(group);
    }

    @GetMapping("/all")
    public List<AppGroup> getAllGroups() {
        return groupRepository.findAll();
    }


    @GetMapping("/my-groups")
    public List<Map<String, Object>> getMyGroups(@RequestParam Long userId) {
        List<AppGroup> allGroups = groupRepository.findGroupsByUserId(userId);
        List<Map<String, Object>> lightGroups = new ArrayList<>();

        for (AppGroup group : allGroups) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", group.getId());
            map.put("name", group.getName());
            map.put("adminId", group.getAdminId());
            
            
            List<Map<String, Object>> members = new ArrayList<>();
            if(group.getMemberIds() != null) {
                for(Long mid : group.getMemberIds()) {
                    userRepository.findById(mid).ifPresent(u -> {
                        Map<String, Object> um = new HashMap<>();
                        um.put("id", u.getId());
                        um.put("name", u.getName());
                        // NO PIC
                        members.add(um);
                    });
                }
            }
            map.put("members", members);
            map.put("memberIds", group.getMemberIds());
            lightGroups.add(map);
        }
        return lightGroups;
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