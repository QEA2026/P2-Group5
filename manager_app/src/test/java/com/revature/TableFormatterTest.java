package com.revature;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.revature.models.Approval;
import com.revature.models.Expense;

public class TableFormatterTest {

    @Test
    public void printExpensesTableFormatsRowsAndTruncatesLongDescriptions() {
        ArrayList<Expense> expenses = new ArrayList<>();
        expenses.add(new Expense(1, 2, 45.5, "This description is definitely longer than twenty-five characters", "2026-07-01"));

        String output = captureOutput(() -> TableFormatter.printExpensesTable(expenses));

        assertTrue(output.contains("| Exp ID | User ID  | Amount   | Description               | Date       |"));
        assertTrue(output.contains("| 1      | 2        | $45.50"));
        assertTrue(output.contains("This description is defi"));
    }

    @Test
    public void printExpenseAndApprovalPrintsBothObjects() {
        Expense expense = new Expense(1, 2, 45.5, "Taxi to airport", "2026-07-01");
        Approval approval = new Approval(3, 1, "approved", 5, "looks good", "2026-07-02");

        String output = captureOutput(() -> TableFormatter.printExpenseAndApproval(expense, approval));

        assertTrue(output.contains("=== Expense ==="));
        assertTrue(output.contains("=== Approval ==="));
        assertTrue(output.contains("Taxi to airport"));
        assertTrue(output.contains("approved"));
    }

    private String captureOutput(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        try {
            action.run();
        } finally {
            System.setOut(originalOut);
        }

        return outputStream.toString();
    }
}