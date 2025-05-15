package io.github.palexdev.materialfx.demo.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private final String USER = "root";
    private final String PWD = "";
    private final String URL = "jdbc:mysql://localhost:3306/sportifydb3?zeroDateTimeBehavior=convertToNull";
    private static DbConnection instance;
    private Connection cnx;

    private DbConnection() {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection with retry mechanism
            int maxRetries = 3;
            int retryCount = 0;
            boolean connected = false;

            while (!connected && retryCount < maxRetries) {
                try {
                    cnx = DriverManager.getConnection(URL, USER, PWD);
                    System.out.println("Database connection established successfully!");
                    connected = true;
                } catch (SQLException e) {
                    retryCount++;
                    if (retryCount == maxRetries) {
                        System.err.println("Failed to connect to database after " + maxRetries + " attempts.");
                        System.err.println("Error: " + e.getMessage());
                        throw e;
                    }
                    System.err.println("Connection attempt " + retryCount + " failed. Retrying...");
                    Thread.sleep(1000); // Wait 1 second before retrying
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            System.err.println("Error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Connection retry interrupted.");
            System.err.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection.");
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static DbConnection getInstance() {
        if (instance == null) {
            instance = new DbConnection();
        }
        return instance;
    }

    public Connection getCnx() {
        try {
            // Check if connection is closed or null, if so, create a new one
            if (cnx == null || cnx.isClosed()) {
                instance = new DbConnection();
                return instance.cnx;
            }
            return cnx;
        } catch (SQLException e) {
            System.err.println("Error checking connection status: " + e.getMessage());
            return null;
        }
    }

    public Connection getConnection() {
        return getCnx();
    }
}