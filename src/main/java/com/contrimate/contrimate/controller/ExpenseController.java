package com.contrimate.contrimate.controller;

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
@CrossOrigin(origins = "*") // Mobile testing ke liye "*" better hai
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserRepository userRepository; // ✨ Friends ki details fetch karne ke liye zaroori

    // 1. USER BALANCE
    @GetMapping("/user-balance")
    public ResponseEntity<Double> getUserBalance(@RequestParam Long userId) {
        try {
            Double balance = expenseService.getUserBalance(userId);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.ok(0.0);
        }
    }

    // 2. GET USER EXPENSES
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Expense>> getUserExpenses(@PathVariable Long userId) {
        List<Expense> expenses = expenseService.getExpensesByUser(userId);
        return ResponseEntity.ok(expenses != null ? expenses : List.of());
    }

    // 3. GET GROUP EXPENSES
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Expense>> getExpensesByGroup(@PathVariable Long groupId) {
        List<Expense> expenses = expenseService.getExpensesByGroup(groupId);
        return ResponseEntity.ok(expenses != null ? expenses : List.of());
    }

    // 4. ADD EXPENSE
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
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // 5. SETTLE UP
    @PostMapping("/settle")
    public ResponseEntity<?> settleUp(@RequestBody Map<String, Object> payload) {
        try {
            Long payerId = Long.valueOf(payload.get("payerId").toString());
            Long receiverId = Long.valueOf(payload.get("receiverId").toString());
            Double amount = Double.valueOf(payload.get("amount").toString());
            return ResponseEntity.ok(expenseService.settleUp(payerId, receiverId, amount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 6. DELETE & CLEAR
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

    // 🔥 7. GET DEBTS (UPDATED: Added UPI & PFP for Real Payments)
    @GetMapping("/debts/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserDebts(@PathVariable Long userId) {
        try {
            // Service se basic balance list lo
            List<Map<String, Object>> rawDebts = expenseService.getFriendBalances(userId);
            List<Map<String, Object>> detailedDebts = new ArrayList<>();

            for (Map<String, Object> debt : rawDebts) {
                Long friendId = Long.valueOf(debt.get("userId").toString());
                
                // Dost ki extra details fetch karo (UPI ID aur Photo)
                Optional<User> friendOpt = userRepository.findById(friendId);
                
                if (friendOpt.isPresent()) {
                    User friend = friendOpt.get();
                    Map<String, Object> debtMap = new HashMap<>(debt); // Existing data (id, balance) copy karo
                    
                    debtMap.put("upiId", friend.getUpiId()); // ✨ Real payment ke liye
                    debtMap.put("profilePic", friend.getProfilePic()); // ✨ UI pfp ke liye
                    
                    detailedDebts.add(debtMap);
                }
            }
            return ResponseEntity.ok(detailedDebts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}