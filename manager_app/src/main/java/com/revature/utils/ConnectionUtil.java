package com.revature.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        String url = "jdbc:sqlite:" + resolveDatabasePath().toAbsolutePath();

        //This return statement is what returns our actual database Connection object
        return DriverManager.getConnection(url);
    }

    private static Path resolveDatabasePath() {
        Path currentDirectory = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

        for (Path cursor = currentDirectory; cursor != null; cursor = cursor.getParent()) {
            Path databaseDirectory = cursor.resolve("db");
            Path databaseFile = databaseDirectory.resolve("expense_manager.db");

            if (Files.exists(databaseFile) || Files.isDirectory(databaseDirectory)) {
                return databaseFile;
            }
        }

        return currentDirectory.resolve("db").resolve("expense_manager.db");
    }
}
