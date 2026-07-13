package com.revature.DAOs;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.revature.models.User;
import com.revature.utils.ConnectionUtil;


public class AuthDAO {

    // login using username and password 
    public User login(String username, String password){
        try( Connection conn = ConnectionUtil.getConnection()){
            String sql = "select * from users where username = ? and password = ?;";

            PreparedStatement ps =conn.prepareStatement(sql);

            ps.setString(1,username);
            ps.setString(2,password);

            ResultSet rs = ps.executeQuery();

            //since we're only expecting one record, we can just use an if with rs.next() instead of while
            if(rs.next()){

                User m = new User(
            
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"));

            

                return m; //returning the Employee with the matching username/password
            } else
            {
                return null;
            }

           



        } catch (SQLException e){
            e.printStackTrace();
        }

        return null;
    }

    public User logout(User u)
    {
        u = null;
        return u;
    }
        

    
}