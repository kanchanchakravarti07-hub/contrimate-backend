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

    @Transactional
    public Expense saveExpense(Expense expense) {
        if (expense.getSplits() != null) {
            for (ExpenseSplit split : expense.getSplits()) {
                split.setExpense(expense);
            }
        }
        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByGroup(Long groupId) {
        return expenseRepository.findByGroupId(groupId);
    }

    public List<Expense> getExpensesByUser(Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    public Double getUserBalance(Long userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        
        double totalPaid = 0;     
        double totalConsumed = 0; 

        for (Expense e : expenses) {
            if (e.getPaidBy().getId().equals(userId)) {
                totalPaid += e.getTotalAmount();
            }
            
            if (e.getSplits() != null) {
                for (ExpenseSplit split : e.getSplits()) {
                    if (split.getUser().getId().equals(userId)) {
                        totalConsumed += split.getAmountOwed(); 
                    }
                }
            }
        }
        return totalPaid - totalConsumed;
    }

    public List<Map<String, Object>> getFriendBalances(Long userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        Map<Long, Double> balanceMap = new HashMap<>();

        for (Expense e : expenses) {
            Long payerId = e.getPaidBy().getId();
            
            if (e.getSplits() != null) {
                for (ExpenseSplit split : e.getSplits()) {
                    Long borrowerId = split.getUser().getId();
                    Double amount = split.getAmountOwed(); 

                    if (payerId.equals(userId) && !borrowerId.equals(userId)) {
                        balanceMap.put(borrowerId, balanceMap.getOrDefault(borrowerId, 0.0) + amount);
                    }
                    else if (borrowerId.equals(userId) && !payerId.equals(userId)) {
                        balanceMap.put(payerId, balanceMap.getOrDefault(payerId, 0.0) - amount);
                    }
                }
            }
        }

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
        split.setAmountOwed(amount);
        
        settlement.setSplits(List.of(split));

        return expenseRepository.save(settlement);
    }

    public void deleteExpense(Long id) { expenseRepository.deleteById(id); }
    public void clearAll() { expenseRepository.deleteAll(); }
}