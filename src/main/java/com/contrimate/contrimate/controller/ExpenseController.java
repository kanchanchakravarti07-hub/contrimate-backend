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
@CrossOrigin(origins = "*") 
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserRepository userRepository;

    // 1. GET USER BALANCE
    @GetMapping("/user-balance")
    public ResponseEntity<Double> getUserBalance(@RequestParam Long userId) {
        try {
            Double balance = expenseService.getUserBalance(userId);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.ok(0.0);
        }
    }

    // 2. GET ALL EXPENSES FOR A USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Expense>> getUserExpenses(@PathVariable Long userId) {
        List<Expense> expenses = expenseService.getExpensesByUser(userId);
        return ResponseEntity.ok(expenses != null ? expenses : List.of());
    }

    // 3. GET EXPENSES FOR A SPECIFIC GROUP
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Expense>> getExpensesByGroup(@PathVariable Long groupId) {
        List<Expense> expenses = expenseService.getExpensesByGroup(groupId);
        return ResponseEntity.ok(expenses != null ? expenses : List.of());
    }

    // 🔥 4. ADD EXPENSE (FIXED: Error Saving & DB Sync)
    @PostMapping("/add")
    public ResponseEntity<?> createExpense(@RequestBody Expense expense) {
        try {
            // Timestamp set karna zaroori hai
            expense.setCreatedAt(LocalDateTime.now());

            // Splits ko Expense Object se manually link karna padta hai foreign key ke liye
            if (expense.getSplits() != null && !expense.getSplits().isEmpty()) {
                for (ExpenseSplit split : expense.getSplits()) {
                    split.setExpense(expense);
                }
            } else {
                return ResponseEntity.badRequest().body("Error: Splits list is empty. Cannot save expense.");
            }

            Expense saved = expenseService.saveExpense(expense);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            // Railway logs mein poori detail dikhegi
            e.printStackTrace(); 
            return ResponseEntity.badRequest().body("Backend Error: " + e.getMessage());
        }
    }

    // 5. SETTLE UP (RECORD PAYMENT)
    @PostMapping("/settle")
    public ResponseEntity<?> settleUp(@RequestBody Map<String, Object> payload) {
        try {
            Long payerId = Long.valueOf(payload.get("payerId").toString());
            Long receiverId = Long.valueOf(payload.get("receiverId").toString());
            Double amount = Double.valueOf(payload.get("amount").toString());
            return ResponseEntity.ok(expenseService.settleUp(payerId, receiverId, amount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Settlement Failed: " + e.getMessage());
        }
    }

    // 6. DELETE EXPENSE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.ok("Expense Deleted Successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Delete Failed: " + e.getMessage());
        }
    }

    // 7. CLEAR ALL DATA (Testing Only)
    @DeleteMapping("/clear")
    public ResponseEntity<?> clear() {
        expenseService.clearAll();
        return ResponseEntity.ok("All Data Cleared");
    }

    // 🔥 8. GET DEBTS (With UPI & Profile Pictures for Real Pay)
    @GetMapping("/debts/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserDebts(@PathVariable Long userId) {
        try {
            List<Map<String, Object>> rawDebts = expenseService.getFriendBalances(userId);
            List<Map<String, Object>> detailedDebts = new ArrayList<>();

            for (Map<String, Object> debt : rawDebts) {
                Long friendId = Long.valueOf(debt.get("userId").toString());
                
                // Har dost ki extra details database se fetch karna
                Optional<User> friendOpt = userRepository.findById(friendId);
                
                if (friendOpt.isPresent()) {
                    User friend = friendOpt.get();
                    Map<String, Object> debtMap = new HashMap<>(debt);
                    
                    // UPI ID aur Profile Pic add karna frontend ke liye
                    debtMap.put("upiId", friend.getUpiId()); 
                    debtMap.put("profilePic", friend.getProfilePic()); 
                    
                    detailedDebts.add(debtMap);
                }
            }
            return ResponseEntity.ok(detailedDebts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}