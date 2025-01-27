package com.drillup.drillup;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;

public class Database {

    private Connection conn;
    private Boolean isConnected;

    public Database() {
        isConnected = false;

    }

    public Connection getConn() {
        return conn;
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Boolean isConnected() {
        return isConnected;
    }

    public void connectToDatabase(){
        try {
            File file = new File("connection.txt");
            if (!file.exists()) {
                throw new FileNotFoundException("connection.txt not found");
            }

            System.out.println("Reading connection parameters from file");
            Scanner scanner = new Scanner(file);
            Map<String, String> connectionParams = new HashMap<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(": ");
                if (parts.length == 2) {
                    connectionParams.put(parts[0].toUpperCase(), parts[1]);
                }
            }
            scanner.close();
            String connectionUrl = String.format("jdbc:sqlserver://%s:1433;databaseName=%s;encrypt=false;trustServerCertificate=true",
                    connectionParams.get("SERVER NAME"),
                    connectionParams.get("DATABASE"));
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            conn = DriverManager.getConnection(connectionUrl,connectionParams.get("USER"),connectionParams.get("PASSWORD"));
            if(conn != null) {
                isConnected = true;
                System.out.println("Connected to the database");
            }
        } catch (SQLException | FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }

}
