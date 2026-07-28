package com.revature.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ManagerExpenseApprovalRecordTest {

    @Test
    public void constructorAndGettersReturnValuesPassedIn() {
        ManagerExpenseApprovalRecord record = new ManagerExpenseApprovalRecord(
            1, 10, 20, "jdoe", 45.50, "Taxi", "2026-07-01",
            "pending", 5, "mgr1", "needs review", "2026-07-02"
        );

        assertEquals(1, record.getApprovalId());
        assertEquals(10, record.getExpenseId());
        assertEquals(20, record.getEmployeeId());
        assertEquals("jdoe", record.getEmployeeUsername());
        assertEquals(45.50, record.getAmount());
        assertEquals("Taxi", record.getDescription());
        assertEquals("2026-07-01", record.getExpenseDate());
        assertEquals("pending", record.getStatus());
        assertEquals(5, record.getReviewerId());
        assertEquals("mgr1", record.getReviewerUsername());
        assertEquals("needs review", record.getComment());
        assertEquals("2026-07-02", record.getReviewDate());
    }

    @Test
    public void reviewerIdCanBeNullForUnreviewedRecords() {
        ManagerExpenseApprovalRecord record = new ManagerExpenseApprovalRecord(
            1, 10, 20, "jdoe", 45.50, "Taxi", "2026-07-01",
            "pending", null, null, null, null
        );

        assertNull(record.getReviewerId());
        assertNull(record.getReviewerUsername());
    }
}
