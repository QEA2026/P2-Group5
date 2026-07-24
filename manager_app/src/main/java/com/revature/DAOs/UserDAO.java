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

    private final Connection injectedConnection;

    public UserDAO() {
        this.injectedConnection = null;
    }

    // Allows tests to inject a mock Connection instead of hitting the real database.
    public UserDAO(Connection injectedConnection) {
        this.injectedConnection = injectedConnection;
    }

    private Connection getConnection() throws SQLException {
        return injectedConnection != null ? injectedConnection : ConnectionUtil.getConnection();
    }

    @Override
    public ArrayList<User> getManagers()
    {
        //instantiate a Connection object so that we can talk to the DB.
        try(Connection conn = getConnection()){

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
        try(Connection conn = getConnection()){

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
    public boolean deleteUser(User emp)
    {
        try (Connection conn = getConnection()){
            
            String sql = "delete from users where user_id = ?;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, emp.getUser_id());
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e){
            e.printStackTrace();
        }
        
        return false;
    }
}