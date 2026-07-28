package com.revature.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ApprovalUpdateRequestTest {

    @Test
    public void settersUpdateFields() {
        ApprovalUpdateRequest request = new ApprovalUpdateRequest();

        request.setStatus("approved");
        request.setComment("looks good");
        request.setReviewDate("2026-07-28");

        assertEquals("approved", request.getStatus());
        assertEquals("looks good", request.getComment());
        assertEquals("2026-07-28", request.getReviewDate());
    }
}
