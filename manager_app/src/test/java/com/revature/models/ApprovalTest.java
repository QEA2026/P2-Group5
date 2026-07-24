package com.revature.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ApprovalTest {

    @Test
    public void constructorAndGettersReturnValuesPassedIn() {
        Approval approval = new Approval(3, 11, "pending", 5, "needs review", "2026-07-02");

        assertEquals(3, approval.getApproval_id());
        assertEquals(11, approval.getExpense_id());
        assertEquals("pending", approval.getStatus());
        assertEquals(5, approval.getReviewer_id());
        assertEquals("needs review", approval.getComment());
        assertEquals("2026-07-02", approval.getReview_date());
    }

    @Test
    public void settersUpdateMutableFields() {
        Approval approval = new Approval(3, 11, "pending", 5, "needs review", "2026-07-02");

        approval.setComment("approved");
        approval.setStatus("approved");

        assertEquals("approved", approval.getComment());
        assertEquals("approved", approval.getStatus());
    }

    @Test
    public void toStringIncludesCoreFields() {
        Approval approval = new Approval(3, 11, "pending", 5, "needs review", "2026-07-02");

        assertEquals("ID: 3, Expense ID: 11, Status: pending, Reviewer ID: 5, Comment: needs review, Review Date: 2026-07-02", approval.toString());
    }
}