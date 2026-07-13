package com.revature.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//This class is where we manage and establish our database connection
//
public class ConnectionUtil {

    //This method will eventually return an object of type Connection
    //...which we'll use to interact with our database
    public static Connection getConnection() throws SQLException{

        //first we need to register our SQLite driver
        //this process makes the application aware of what SQL flavor we're using
        try{
            Class.forName("org.sqlite.JDBC"); //searching for the SQLite driver, which we have as a dependency

        }catch (ClassNotFoundException e){
            e.printStackTrace(); //this tells in the console what went wrong
            System.out.println("problem occurred locating driver");

        }

        //use this string in a method that gets connections
        // String url = "jdbc:sqlite:expense_manager.db";
        String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "/db/expense_manager.db";

        //This return statement is what returns our actual database Connection object
        return DriverManager.getConnection(url);
    }
}