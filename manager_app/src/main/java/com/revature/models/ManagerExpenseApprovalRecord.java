package com.revature.models;

public class ManagerExpenseApprovalRecord {

    private int approvalId;
    private int expenseId;
    private int employeeId;
    private String employeeUsername;
    private double amount;
    private String description;
    private String expenseDate;
    private String status;
    private Integer reviewerId;
    private String reviewerUsername;
    private String comment;
    private String reviewDate;

    public ManagerExpenseApprovalRecord(
        int approvalId,
        int expenseId,
        int employeeId,
        String employeeUsername,
        double amount,
        String description,
        String expenseDate,
        String status,
        Integer reviewerId,
        String reviewerUsername,
        String comment,
        String reviewDate
    ) {
        this.approvalId = approvalId;
        this.expenseId = expenseId;
        this.employeeId = employeeId;
        this.employeeUsername = employeeUsername;
        this.amount = amount;
        this.description = description;
        this.expenseDate = expenseDate;
        this.status = status;
        this.reviewerId = reviewerId;
        this.reviewerUsername = reviewerUsername;
        this.comment = comment;
        this.reviewDate = reviewDate;
    }

    public int getApprovalId() {
        return approvalId;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeUsername() {
        return employeeUsername;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public String getStatus() {
        return status;
    }

    public Integer getReviewerId() {
        return reviewerId;
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public String getComment() {
        return comment;
    }

    public String getReviewDate() {
        return reviewDate;
    }
}
