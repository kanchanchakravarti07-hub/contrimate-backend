package com.contrimate.contrimate.controller;

import com.contrimate.contrimate.entity.Expense;
import com.contrimate.contrimate.entity.ExpenseSplit;
import com.contrimate.contrimate.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "http://localhost:3000")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    // 1. NEW API: USER BALANCE
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

    // 🔥 4. ADD EXPENSE (UPDATED FIX)
    @PostMapping("/add")
    public ResponseEntity<?> createExpense(@RequestBody Expense expense) {
        try {
            // 🔥 FIX 1: Set Current Date & Time
            expense.setCreatedAt(LocalDateTime.now());

            // 🔥 FIX 2: Link Splits to Expense (Zaroori hai taaki database mein link bane)
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

    // 7. GET DEBTS (For Settle Up List)
    @GetMapping("/debts/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserDebts(@PathVariable Long userId) {
        try {
            List<Map<String, Object>> debts = expenseService.getFriendBalances(userId);
            return ResponseEntity.ok(debts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}