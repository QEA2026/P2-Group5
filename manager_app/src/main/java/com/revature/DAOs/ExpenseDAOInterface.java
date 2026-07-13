package com.revature.DAOs;

import java.util.ArrayList;

import com.revature.models.Expense;

public interface ExpenseDAOInterface {
    
    ArrayList<Expense> getExpenses();

    ArrayList<Expense> getExpensesByEmployee(String username);

    ArrayList<Expense> getExpensesByStatus(String status);

    ArrayList<Expense> getExpensesByDate(String start_date, String end_date);

    ArrayList<Expense> getExpensesByCategory(String category);

    Expense getExpenseByID(int id);

    

    



    

}
