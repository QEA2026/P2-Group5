package com.revature.DAOs;

import java.util.ArrayList;

import com.revature.models.Approval;

public interface ApprovalDAOInterface {

     
    ArrayList<Approval> getApprovals();

    ArrayList<Approval> getApprovalsByManager(String username);

    ArrayList<Approval> getApprovalsByEmployee(String username);

    ArrayList<Approval> getApprovalsByStatus(String status);

    ArrayList<Approval> getApprovalsByDate(String date);

 // ArrayList<Approval> getApprovalsByCategory(String category);

    Approval insertApproval(Approval approval);

    Approval updateApproval(Approval approval);

    Approval getApprovalByID(int ID);


    
    
}
