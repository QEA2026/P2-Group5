package com.revature.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.revature.models.Expense;
import com.revature.utils.ConnectionUtil;

public class ExpenseDAO implements ExpenseDAOInterface{

    // get expenses
    @Override
    public ArrayList<Expense> getExpenses() 
    {
        try (Connection conn = ConnectionUtil.getConnection()) {

            String sql = "select * from expenses;";
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery(sql);
            ArrayList<Expense> expenseList = new ArrayList<>();

            while (rs.next()) {
                Expense e = new Expense(

                    rs.getInt("expense_id"),
                    rs.getInt("user_id"),
                    rs.getDouble("amount"),
                    rs.getString("exp_description"),
                    rs.getString("date")
                );
                expenseList.add(e);
            }

            if (expenseList.isEmpty()) 
            {
                System.out.println("No approvals found.");
                return expenseList;
            }
            else
            {
                return expenseList;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // get expenses by employee
    @Override
    public ArrayList<Expense> getExpensesByEmployee(String username)
    {
        try (Connection conn = ConnectionUtil.getConnection()) {

            String sql = "select * from expenses where user_id = (select user_id from users where username = ?);";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            ArrayList<Expense> expenseList = new ArrayList<>();

            while (rs.next()) {
                Expense e = new Expense(

                    rs.getInt("expense_id"),
                    rs.getInt("user_id"),
                    rs.getDouble("amount"),
                    rs.getString("exp_description"),
                    rs.getString("date")
                );
                expenseList.add(e);
            }

            if (expenseList.isEmpty()) 
            {
                System.out.println("No approvals found.");
                return expenseList;
            }
            else
            {
                return expenseList;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // get expenses by status 
    @Override
    public ArrayList<Expense> getExpensesByStatus(String status)
    {
        try (Connection conn = ConnectionUtil.getConnection()) {

            String sql = "select * from expenses where expense_id in (select expense_id from approvals where status = ?);";
            
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            ArrayList<Expense> expenseList = new ArrayList<>();

            while (rs.next()) {
                Expense e = new Expense(

                    rs.getInt("expense_id"),
                    rs.getInt("user_id"),
                    rs.getDouble("amount"),
                    rs.getString("exp_description"),
                    rs.getString("date")
                );
                expenseList.add(e);
            }



            if (expenseList.isEmpty()) 
                {
                    System.out.println("No approvals found.");
                    return expenseList;
                }
            else
                {
                    return expenseList;
                }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // get expenses by date
    @Override
    public ArrayList<Expense> getExpensesByDate(String start_date, String end_date)
    {
        try (Connection conn = ConnectionUtil.getConnection()) {

            String sql = "select * from expenses where date between ? and ?;";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, start_date);
            ps.setString(2, end_date);
            ResultSet rs = ps.executeQuery();
            ArrayList<Expense> expenseList = new ArrayList<>();

            while (rs.next()) {
                Expense e = new Expense(

                    rs.getInt("expense_id"),
                    rs.getInt("user_id"),
                    rs.getDouble("amount"),
                    rs.getString("exp_description"),
                    rs.getString("date")
                );
                expenseList.add(e);
            }

            if (expenseList.isEmpty()) 
            {
                System.out.println("No approvals found.");
                return expenseList;
            }
            else
            {
                return expenseList;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // get expenses by category 
     // get expenses by status 
    @Override
    public ArrayList<Expense> getExpensesByCategory(String category)
    {
        try (Connection conn = ConnectionUtil.getConnection()) {

            String sql = "select * from expenses where exp_description like ?;";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + category + "%");
            ResultSet rs = ps.executeQuery();
            ArrayList<Expense> expenseList = new ArrayList<>();

            while (rs.next()) {
                Expense e = new Expense(

                    rs.getInt("expense_id"),
                    rs.getInt("user_id"),
                    rs.getDouble("amount"),
                    rs.getString("exp_description"),
                    rs.getString("date")
                );
                expenseList.add(e);
            }

            if (expenseList.isEmpty()) 
            {
                System.out.println("No approvals found.");
                return expenseList;
            }
            else
            {
                return expenseList;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // get expense by id 
    @Override
    public Expense getExpenseByID(int id)
    {
        try (Connection conn = ConnectionUtil.getConnection()) {

            String sql = "select * from expenses where expense_id = ?;";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Expense e = new Expense(

                    rs.getInt("expense_id"),
                    rs.getInt("user_id"),
                    rs.getDouble("amount"),
                    rs.getString("exp_description"),
                    rs.getString("date")
                );
                return e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }






    
    
}
