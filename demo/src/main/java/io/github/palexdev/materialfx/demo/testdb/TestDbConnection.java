package io.github.palexdev.materialfx.demo.testdb;

import io.github.palexdev.materialfx.demo.utils.DbConnection;

import java.sql.Connection;

public class TestDbConnection {
    public static void main(String[] args) {
        Connection cnx = DbConnection.getInstance().getCnx();

        if (cnx != null) {
            System.out.println("Connection is successful!");
        } else {
            System.out.println("Failed to connect to the database.");
        }
    }
}

