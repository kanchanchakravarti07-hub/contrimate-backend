package com.contrimate.contrimate.service;

import com.contrimate.contrimate.entity.*;
import com.contrimate.contrimate.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ExpenseService {

    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private UserRepository userRepository;

    // --- 1. SAVE EXPENSE ---
    @Transactional
    public Expense saveExpense(Expense expense) {
        if (expense.getSplits() != null) {
            for (ExpenseSplit split : expense.getSplits()) {
                split.setExpense(expense);
            }
        }
        return expenseRepository.save(expense);
    }

    // --- 2. GET GROUP EXPENSES ---
    public List<Expense> getExpensesByGroup(Long groupId) {
        return expenseRepository.findByGroupId(groupId);
    }

    // --- 3. GET USER EXPENSES ---
    public List<Expense> getExpensesByUser(Long userId) {
        // Updated Repository Call
        return expenseRepository.findByUserId(userId);
    }

    // --- 4. CALCULATE USER BALANCE ---
    public Double getUserBalance(Long userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        
        double totalPaid = 0;     
        double totalConsumed = 0; 

        for (Expense e : expenses) {
            // 1. Add to Total Paid if I am the payer
            if (e.getPaidBy().getId().equals(userId)) {
                totalPaid += e.getTotalAmount();
            }
            
            // 2. Add to Total Consumed if I am in the split
            if (e.getSplits() != null) {
                for (ExpenseSplit split : e.getSplits()) {
                    if (split.getUser().getId().equals(userId)) {
                        // 🔥 FIX: getAmount() -> getAmountOwed()
                        totalConsumed += split.getAmountOwed(); 
                    }
                }
            }
        }
        return totalPaid - totalConsumed;
    }

    // --- 5. GET FRIEND BALANCES (For Settle Up List) ---
    public List<Map<String, Object>> getFriendBalances(Long userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        Map<Long, Double> balanceMap = new HashMap<>();

        for (Expense e : expenses) {
            Long payerId = e.getPaidBy().getId();
            
            if (e.getSplits() != null) {
                for (ExpenseSplit split : e.getSplits()) {
                    Long borrowerId = split.getUser().getId();
                    // 🔥 FIX: getAmount() -> getAmountOwed()
                    Double amount = split.getAmountOwed(); 

                    // Case 1: Maine pay kiya -> Woh mujhe dega (+ve)
                    if (payerId.equals(userId) && !borrowerId.equals(userId)) {
                        balanceMap.put(borrowerId, balanceMap.getOrDefault(borrowerId, 0.0) + amount);
                    }
                    // Case 2: Usne pay kiya -> Main use dunga (-ve)
                    else if (borrowerId.equals(userId) && !payerId.equals(userId)) {
                        balanceMap.put(payerId, balanceMap.getOrDefault(payerId, 0.0) - amount);
                    }
                }
            }
        }

        // Convert Map to List
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map.Entry<Long, Double> entry : balanceMap.entrySet()) {
            Long friendId = entry.getKey();
            Double balance = entry.getValue();

            if (Math.abs(balance) > 0.1) { 
                userRepository.findById(friendId).ifPresent(u -> {
                    Map<String, Object> friendData = new HashMap<>();
                    friendData.put("userId", u.getId());
                    friendData.put("name", u.getName());
                    friendData.put("email", u.getEmail());
                    friendData.put("balance", balance);
                    result.add(friendData);
                });
            }
        }
        return result;
    }

    // --- 6. SETTLE UP ---
    @Transactional
    public Expense settleUp(Long payerId, Long receiverId, Double amount) {
        User payer = userRepository.findById(payerId).orElseThrow(() -> new RuntimeException("Payer not found"));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        Expense settlement = new Expense();
        settlement.setDescription("Settlement");
        settlement.setTotalAmount(amount);
        settlement.setPaidBy(payer); 
        settlement.setGroup(null); 
        settlement.setCategory("Settlement");

        ExpenseSplit split = new ExpenseSplit();
        split.setExpense(settlement);
        split.setUser(receiver);
        // 🔥 FIX: setAmount() -> setAmountOwed()
        split.setAmountOwed(amount);
        
        settlement.setSplits(List.of(split));

        return expenseRepository.save(settlement);
    }

    // --- 7. DELETE & CLEAR ---
    public void deleteExpense(Long id) { expenseRepository.deleteById(id); }
    public void clearAll() { expenseRepository.deleteAll(); }
}