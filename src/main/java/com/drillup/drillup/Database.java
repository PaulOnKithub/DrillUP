package com.drillup.drillup;

import java.sql.*;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;

public class Database {

    private Connection conn;
    private Statement stmt;
    private ResultSet rs;


    public Database() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection("jdbc:sqlserver://INGENUITYPAUL:1433;databaseName=KFHDAT;encrypt=false;trustServerCertificate=true", "sa", "Mahavira");
            stmt = conn.createStatement();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLServerDriver not found.");
            //e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection failed."+ e.getMessage());
        }
    }


}
