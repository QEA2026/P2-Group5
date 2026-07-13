package com.revature;

import java.util.ArrayList;

import com.revature.models.Approval;
import com.revature.models.Expense;

public class TableFormatter {
    
    public static void printExpensesTable(ArrayList<Expense> expenses) {
        System.out.println("+--------+----------+----------+---------------------------+------------+");
        System.out.println("| Exp ID | User ID  | Amount   | Description               | Date       |");
        System.out.println("+--------+----------+----------+---------------------------+------------+");
        
        for (Expense expense : expenses) {
            System.out.printf("| %-6d | %-8d | $%-7.2f | %-25s | %-10s |\n",
                    expense.getExpense_id(),
                    expense.getUser_id(),
                    expense.getAmount(),
                    truncate(expense.getDescription(), 25),
                    expense.getDate());
        }
        
        System.out.println("+--------+----------+----------+---------------------------+------------+");
    }
    
    public static void printApprovalsTable(ArrayList<Approval> approvals) {
        System.out.println("+----------+----------+-----------+----------+-------------------------------+------------+");
        System.out.println("| App ID   | Exp ID   | Status    | Reviewer | Comment                       | Review Date|");
        System.out.println("+----------+----------+-----------+----------+-------------------------------+------------+");
        
        for (Approval approval : approvals) {
            System.out.printf("| %-8d | %-8d | %-9s | %-8d | %-29s | %-10s |\n",
                    approval.getApproval_id(),
                    approval.getExpense_id(),
                    approval.getStatus(),
                    approval.getReviewer_id(),
                    truncate(approval.getComment(), 29),
                    approval.getReview_date());
        }
        
        System.out.println("+----------+----------+-----------+----------+-------------------------------+------------+");
    }
    
    private static String truncate(String str, int length) {
        return (str != null && str.length() > length) ? 
            str.substring(0, length - 1) : str;
    }

    public static void printExpenseAndApproval(Expense e, Approval a)
    {
        if (e == null) {
            System.out.println("ERROR: Expense not found for approval ID " + a.getApproval_id());
            return;
        }
        
        if (a == null) {
            System.out.println("ERROR: Approval is null");
            return;
        }

       

        System.out.println("=== Expense ===");
        System.out.println(e);
        System.out.println("=== Approval ===");
        System.out.println(a);
        System.out.println();


        
        /* 
        String e_id = String.format("%-10s", "ID: " + e.getExpense_id());
        String e_uid = String.format("%-15s", "User ID: " + e.getUser_id());
        String e_amount = String.format("%-20s", "Amount: " + e_dollar);
        String e_description = String.format("%-30s", "Description: " + e.getDescription());
        String e_date = String.format("%-15s", "Date: " + e.getDate());

        System.out.println("=== EXPENSE ===");
        System.out.println(e_id + e_uid + e_amount + e_description + e_date);
        // System.out.println(e_description + e_date);
        
        String a_id = String.format("%-10s", "ID: " + approval.getApproval_id());
        String a_eid = String.format("%15s", "Expense ID: " + approval.getExpense_id());
        String a_status = String.format("%-25s", "Status: " + approval.getStatus());
        String a_reviewer = String.format("%-20s", "Reviewer: " + approval.getReviewer_id());
        String a_comment = String.format("%-49s", "Comment: " + approval.getComment());
        String a_date = String.format("%-15s", "Date: " + approval.getReview_date());
        
        System.out.println("=== APPROVAL ===");
        System.out.println(a_id + a_eid + a_status + a_reviewer + a_comment + a_date);
*/
        System.out.println();
    }
}
