package com.revature.models;

public class Approval {

    private int approval_id;
    private int expense_id;
    private String status;
    private int reviewer_id;
    private String comment;
    private String review_date;


    public Approval(int approval_id, int expense_id, String status, int reviewer_id, String comment, String review_date) {
        this.approval_id = approval_id;
        this.expense_id = expense_id;
        this.status = status;
        this.reviewer_id = reviewer_id;
        this.comment = comment;
        this.review_date = review_date;
    }

    public int getApproval_id() {
        return this.approval_id;
    }

    public int getExpense_id() {
        return this.expense_id;
    }

    public String getStatus() {
        return this.status;
    }

    public int getReviewer_id() {
        return this.reviewer_id;
    }

    public String getComment() {
        return this.comment;
    }

    public String getReview_date() {
        return this.review_date;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return ("ID: " + this.approval_id + ", Expense ID: " + this.expense_id + ", Status: " + this.status + ", Reviewer ID: " + this.reviewer_id + ", Comment: " + this.comment
            + ", Review Date: " + this.review_date
         );
    }
}
