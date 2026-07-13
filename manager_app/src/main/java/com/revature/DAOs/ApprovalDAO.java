package com.revature.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.revature.models.Approval;
import com.revature.utils.ConnectionUtil;

public class ApprovalDAO implements ApprovalDAOInterface{

    // Get all approvals 
    @Override
    public ArrayList<Approval> getApprovals()
    {
        //instantiate a Connection object so that we can talk to the DB.
        try(Connection conn = ConnectionUtil.getConnection()){

            //A string that will represent our SQL statement
            String sql = "select * from approvals;";

            Statement s = conn.createStatement();

            ResultSet rs = s.executeQuery(sql);

            ArrayList<Approval> approvalList = new ArrayList<>();

            while(rs.next()){

                Approval e = new Approval(
                        rs.getInt("approval_id"),
                        rs.getInt("expense_id"),
                        rs.getString("status"),
                        rs.getInt("reviewer"),
                        rs.getString("comment"),
                        rs.getString("review_date")
                        

                );
                approvalList.add(e);
            }

            if (approvalList.isEmpty()) 
            {
                System.out.println("No approvals found.");
                return approvalList;
            }
            else
            {
                return approvalList;
            }
           

    }

        catch (SQLException e){
                if (e.getMessage().contains("No data found")) {
                    System.out.println("No approvals found.");
                } else {
                    e.printStackTrace();
                }
            }
        return null;
    }
    // get approvals by Manager 
    @Override
    public ArrayList<Approval> getApprovalsByManager(String username) {
        //instantiate a Connection object so that we can talk to the DB.
        try(Connection conn = ConnectionUtil.getConnection()){

            //A string that will represent our SQL statement
            String sql = "select * from approvals where reviewer = (select user_id from users where username = ?);";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            ArrayList<Approval> approvalList = new ArrayList<>();

            while(rs.next()){

                Approval e = new Approval(
                        rs.getInt("approval_id"),
                        rs.getInt("expense_id"),
                        rs.getString("status"),
                        rs.getInt("reviewer"),
                        rs.getString("comment"),
                        rs.getString("review_date")
                        

                );
                approvalList.add(e);
            }
             if (approvalList.isEmpty()) 
            {
                System.out.println("No approvals found.");
                return approvalList;
            }
            else
            {
                return approvalList;
            }

        } catch (SQLException e){
            if (e.getMessage().contains("No data found")) {
                    System.out.println("No approvals found.");
                    return new ArrayList<>(); // Return an empty list if no approvals are found
                } else {
                    e.printStackTrace();
                }
            
            
        }
        return null;

    }

    // get approvals by employee 
    @Override
    public ArrayList<Approval> getApprovalsByEmployee(String username) {
        //instantiate a Connection object so that we can talk to the DB.
        try(Connection conn = ConnectionUtil.getConnection()){

            //A string that will represent our SQL statement
            String sql = "select * from approvals where expense_id IN (select expense_id from expenses where user_id = (select user_id from users where username = ?));";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            ArrayList<Approval> approvalList = new ArrayList<>();

            while(rs.next()){

                Approval a = new Approval(
                        rs.getInt("approval_id"),
                        rs.getInt("expense_id"),
                        rs.getString("status"),
                        rs.getInt("reviewer"),
                        rs.getString("comment"),
                        rs.getString("review_date")
                        

                );
                approvalList.add(a);
            }

             if (approvalList.isEmpty()) 
            {
                System.out.println("No approvals found.");
                return approvalList;
            }
            else
            {
                return approvalList;
            }

        } catch (SQLException e){
            if (e.getMessage().contains("No data found")) {
                System.out.println("No approvals found.");
                return new ArrayList<>(); // Return an empty list if no approvals are found
                } else {
                    e.printStackTrace();
                }
            
        }
        return null; 
    }   

    // get approvals by status 
    @Override
    public ArrayList<Approval> getApprovalsByStatus(String status)
    {
        try(Connection conn = ConnectionUtil.getConnection()){

            //A string that will represent our SQL statement
            String sql = "select * from approvals where status = ?;";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);

            ResultSet rs = ps.executeQuery();

            ArrayList<Approval> approvalList = new ArrayList<>();

            while(rs.next()){

                Approval e = new Approval(
                        rs.getInt("approval_id"),
                        rs.getInt("expense_id"),
                        rs.getString("status"),
                        rs.getInt("reviewer"),
                        rs.getString("comment"),
                        rs.getString("review_date")
                        

                );
                approvalList.add(e);
            }
            
             if (approvalList.isEmpty()) 
            {
                System.out.println("No approvals found.");
                return approvalList;
            }
            else
            {
                return approvalList;
            }

        } catch (SQLException e){
            if (e.getMessage().contains("No data found")) {
                System.out.println("No approvals found.");
                return new ArrayList<>(); // Return an empty list if no approvals are found
            } else {
                e.printStackTrace();
            }
        }
        return null;

    }

    // get approvals by date
    @Override
    public ArrayList<Approval> getApprovalsByDate(String date)
    {
        try(Connection conn = ConnectionUtil.getConnection()){

            //A string that will represent our SQL statement
            String sql = "select * from approvals where review_date = ?;";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, date);

            ResultSet rs = ps.executeQuery();

            ArrayList<Approval> approvalList = new ArrayList<>();

            while(rs.next()){

                Approval e = new Approval(
                        rs.getInt("approval_id"),
                        rs.getInt("expense_id"),
                        rs.getString("status"),
                        rs.getInt("reviewer"),
                        rs.getString("comment"),
                        rs.getString("review_date")
                        

                );
                approvalList.add(e);
            }
            
             if (approvalList.isEmpty()) 
            {
                System.out.println("No approvals found.");
                return approvalList;
            }
            else
            {
                return approvalList;
            }


        } catch (SQLException e){
            if (e.getMessage().contains("No data found")) {
                System.out.println("No approvals found.");
                return new ArrayList<>(); // Return an empty list if no approvals are found
            } else {
                e.printStackTrace();
            }
        }
        return null;

    }

    // add approval to database 
    @Override
    public Approval insertApproval(Approval approval)
    {
        
        try(Connection conn = ConnectionUtil.getConnection()){

            String sql = "insert into approvals (expense_id, status, reviewer, comment, review_date) values (?, ?, ?, ?, ?);";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, approval.getExpense_id());
            ps.setString(2, approval.getStatus());
            ps.setInt(3, approval.getReviewer_id());
            ps.setString(4, approval.getComment());
            ps.setString(5, approval.getReview_date());

            int rowsInserted = ps.executeUpdate();

            if(rowsInserted != 0){
                return approval;
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }


    // update approval 
    @Override
    public Approval updateApproval(Approval approval)
    {
        try(Connection conn = ConnectionUtil.getConnection()){

            String sql = "update approvals set status = ?, reviewer = ?, comment = ?, review_date = ? where approval_id = ?;";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, approval.getStatus());
            ps.setInt(2, approval.getReviewer_id());
            ps.setString(3, approval.getComment());
            ps.setString(4, approval.getReview_date());
            ps.setInt(5, approval.getApproval_id());


            int rowsUpdated = ps.executeUpdate();

            if(rowsUpdated != 0){
                return approval;
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Approval getApprovalByID(int id)
    {
         try (Connection conn = ConnectionUtil.getConnection()) {

            String sql = "select * from approvals where approval_id = ?;";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Approval a = new Approval(

                    rs.getInt("approval_id"),
                    rs.getInt("expense_id"),
                    rs.getString("status"),
                    rs.getInt("reviewer"),
                    rs.getString("comment"),
                    rs.getString("review_date")
                );
                return a;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    

    
}

