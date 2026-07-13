package com.revature.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.revature.models.User;
import com.revature.utils.ConnectionUtil;

public class UserDAO implements UserDAOInterface{
    @Override
    public ArrayList<User> getManagers()
    {
        //instantiate a Connection object so that we can talk to the DB.
        try(Connection conn = ConnectionUtil.getConnection()){

            //A string that will represent our SQL statement
            String sql = "select * from users where role = 'manager';";

            Statement s = conn.createStatement();

            ResultSet rs = s.executeQuery(sql);

            ArrayList<User> managerList = new ArrayList<>();

            while(rs.next()){


                User e = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")

                );
                managerList.add(e);
            }
            return managerList;

        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public User insertUser(User emp) {
        //instantiate a Connection object so that we can talk to the DB.
        try(Connection conn = ConnectionUtil.getConnection()){

            String sql = "insert into users (username, password, role) values (?,?,?);";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1,emp.getUsername());
            ps.setString(2,emp.getPassword());
            ps.setString(3,emp.getRole());
            ps.executeUpdate();

            return emp;

        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User deleteUser(User emp)
    {
        try (Connection conn = ConnectionUtil.getConnection()){
            
            String sql = "delete from users where user_id = ?;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, emp.getUser_id());
            ps.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
        
        return null;
    }
}