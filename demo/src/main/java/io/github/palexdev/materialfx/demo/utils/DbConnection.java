package io.github.palexdev.materialfx.demo.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DbConnection {
    private final String USER = "root";
    private final String PWD = "";
    private final String URL = "jdbc:mysql://localhost:3306/sporifydb";
    //1st STEP
    public static DbConnection instance;

    private Connection cnx;
    //2ND STEP
    private DbConnection(){
        try {
            cnx = DriverManager.getConnection(URL, USER, PWD);
            System.out.println("Connection Etablie !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    //3RD STEP
    public static DbConnection getInstance(){
        if (instance == null) instance = new DbConnection();
        return instance;
    }

    public Connection getCnx(){
        return cnx;
    }
}
