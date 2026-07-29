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

    // How many parent directories to check above the working directory before giving up.
    // Bounds the search to the project tree so it can't wander into unrelated ancestor
    // directories (e.g. a coincidental /var/db on macOS) when no project "db" folder is nearby.
    private static final int MAX_ANCESTOR_LOOKUPS = 4;

    static Path resolveDatabasePath() {
        Path currentDirectory = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

        Path cursor = currentDirectory;
        for (int hopsUp = 0; cursor != null && hopsUp <= MAX_ANCESTOR_LOOKUPS; hopsUp++, cursor = cursor.getParent()) {
            Path databaseDirectory = cursor.resolve("db");
            Path databaseFile = databaseDirectory.resolve("expense_manager.db");

            if (Files.exists(databaseFile) || Files.isDirectory(databaseDirectory)) {
                return databaseFile;
            }
        }

        return currentDirectory.resolve("db").resolve("expense_manager.db");
    }
}
