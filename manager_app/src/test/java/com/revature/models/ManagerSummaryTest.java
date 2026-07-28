package com.revature.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ManagerSummaryTest {

    @Test
    public void constructorAndGettersReturnValuesPassedIn() {
        ManagerSummary summary = new ManagerSummary(10, 4, 5, 1);

        assertEquals(10, summary.getTotal());
        assertEquals(4, summary.getPending());
        assertEquals(5, summary.getApproved());
        assertEquals(1, summary.getDenied());
    }
}
