package com.revature.models;

public class ManagerSummary {

    private int total;
    private int pending;
    private int approved;
    private int denied;

    public ManagerSummary(int total, int pending, int approved, int denied) {
        this.total = total;
        this.pending = pending;
        this.approved = approved;
        this.denied = denied;
    }

    public int getTotal() {
        return total;
    }

    public int getPending() {
        return pending;
    }

    public int getApproved() {
        return approved;
    }

    public int getDenied() {
        return denied;
    }
}
