package com.revature.models;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ExpenseTest {

    @Test
    public void constructorAndGettersReturnValuesPassedIn() {
        Expense expense = new Expense(1, 2, 45.50, "Taxi to airport", "2026-07-01");

        assertEquals(1, expense.getExpense_id());
        assertEquals(2, expense.getUser_id());
        assertEquals(45.50, expense.getAmount());
        assertEquals("Taxi to airport", expense.getDescription());
        assertEquals("2026-07-01", expense.getDate());
    }

    @Test
    public void setDescriptionUpdatesDescription() {
        Expense expense = new Expense(1, 2, 50.00, "", "2026-07-01");

        String setDescription = "set description";
        expense.setDescription(setDescription);

        assertEquals(expense.getDescription(), setDescription);
    }

    @Test
    public void toStringFormatsAmountAsCurrency() {
        Expense expense = new Expense(1, 2, 45.5, "Taxi to airport", "2026-07-01");

        assertTrue(expense.toString().contains("$ 45.50"));
    }

    @Test
    public void toStringHandlesNullDescription() {
        Expense expense = new Expense(1, 2, 45.5, null, "2026-07-01");

        assertDoesNotThrow(expense::toString);
        assertTrue(expense.toString().contains("Description: null"));
    }
}
