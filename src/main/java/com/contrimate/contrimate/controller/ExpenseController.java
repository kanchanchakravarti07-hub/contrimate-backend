package com.contrimate.contrimate.controller;

import com.contrimate.contrimate.entity.Comment;
import com.contrimate.contrimate.entity.Expense;
import com.contrimate.contrimate.entity.ExpenseSplit;
import com.contrimate.contrimate.entity.User;
import com.contrimate.contrimate.repository.UserRepository;
import com.contrimate.contrimate.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserRepository userRepository;

    // --- HELPER: Convert Heavy Expense to Light Map (No Profile Pics) ---
    private Map<String, Object> toLightExpense(Expense e) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", e.getId());
        map.put("description", e.getDescription());
        map.put("totalAmount", e.getTotalAmount());
        map.put("category", e.getCategory());
        map.put("createdAt", e.getCreatedAt());

        // Light User (No Photo)
        if (e.getPaidBy() != null) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", e.getPaidBy().getId());
            u.put("name", e.getPaidBy().getName());
            map.put("paidBy", u);
        }

        // Light Splits
        List<Map<String, Object>> splits = new ArrayList<>();
        if (e.getSplits() != null) {
            for (ExpenseSplit s : e.getSplits()) {
                Map<String, Object> sm = new HashMap<>();
                sm.put("amountOwed", s.getAmountOwed());
                if (s.getUser() != null) {
                    Map<String, Object> su = new HashMap<>();
                    su.put("id", s.getUser().getId());
                    su.put("name", s.getUser().getName());
                    sm.put("user", su);
                }
                splits.add(sm);
            }
        }
        map.put("splits", splits);

        // Light Comments
        List<Map<String, Object>> comments = new ArrayList<>();
        if (e.getComments() != null) {
            for (Comment c : e.getComments()) {
                Map<String, Object> cm = new HashMap<>();
                cm.put("text", c.getText());
                cm.put("createdAt", c.getCreatedAt());
                if (c.getUser() != null) {
                    Map<String, Object> cu = new HashMap<>();
                    cu.put("id", c.getUser().getId());
                    cu.put("name", c.getUser().getName());
                    cm.put("user", cu);
                }
                comments.add(cm);
            }
        }
        map.put("comments", comments);

        return map;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserExpenses(@PathVariable Long userId) {
        List<Expense> expenses = expenseService.getExpensesByUser(userId);
        List<Map<String, Object>> lightList = new ArrayList<>();
        if (expenses != null) {
            for (Expense e : expenses) {
                lightList.add(toLightExpense(e));
            }
        }
        return ResponseEntity.ok(lightList);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Map<String, Object>>> getExpensesByGroup(@PathVariable Long groupId) {
        List<Expense> expenses = expenseService.getExpensesByGroup(groupId);
        List<Map<String, Object>> lightList = new ArrayList<>();
        if (expenses != null) {
            for (Expense e : expenses) {
                lightList.add(toLightExpense(e));
            }
        }
        return ResponseEntity.ok(lightList);
    }

    @GetMapping("/user-balance")
    public ResponseEntity<Double> getUserBalance(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(expenseService.getUserBalance(userId));
        } catch (Exception e) {
            return ResponseEntity.ok(0.0);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> createExpense(@RequestBody Expense expense) {
        try {
            expense.setCreatedAt(LocalDateTime.now());
            if (expense.getSplits() != null) {
                for (ExpenseSplit split : expense.getSplits()) {
                    split.setExpense(expense);
                }
            }
            Expense saved = expenseService.saveExpense(expense);
            return ResponseEntity.ok(toLightExpense(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/settle")
    public ResponseEntity<?> settleUp(@RequestBody Map<String, Object> payload) {
        try {
            Long payerId = Long.valueOf(payload.get("payerId").toString());
            Long receiverId = Long.valueOf(payload.get("receiverId").toString());
            Double amount = Double.valueOf(payload.get("amount").toString());
            Expense saved = expenseService.settleUp(payerId, receiverId, amount);
            return ResponseEntity.ok(toLightExpense(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok("Deleted");
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clear() {
        expenseService.clearAll();
        return ResponseEntity.ok("Cleared");
    }

    @GetMapping("/debts/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserDebts(@PathVariable Long userId) {
        try {
            List<Map<String, Object>> rawDebts = expenseService.getFriendBalances(userId);
            List<Map<String, Object>> detailedDebts = new ArrayList<>();

            for (Map<String, Object> debt : rawDebts) {
                Long friendId = Long.valueOf(debt.get("userId").toString());
                Optional<User> friendOpt = userRepository.findById(friendId);
                
                if (friendOpt.isPresent()) {
                    User friend = friendOpt.get();
                    Map<String, Object> debtMap = new HashMap<>(debt);
                    debtMap.put("upiId", friend.getUpiId()); 
                    
                    debtMap.put("profilePic", friend.getProfilePic()); 
                    detailedDebts.add(debtMap);
                }
            }
            return ResponseEntity.ok(detailedDebts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}