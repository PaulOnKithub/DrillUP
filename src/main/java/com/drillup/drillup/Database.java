package com.drillup.drillup;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import javafx.util.Pair;

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

    public  Pair<Integer,Integer> retrieveGLInfo(String sourceLedger, Long drillDownLink){
        Pair<Integer,Integer> glInfo = new Pair<>(0,0);
        if(isConnected) {

            try {
                String sql = "SELECT BATCHID, BTCHENTRY FROM GLJEH WHERE SRCELEDGER= ? AND DRILLDWNLK = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, sourceLedger);
                stmt.setLong(2, drillDownLink);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    glInfo = new Pair<>(rs.getInt("BATCHID"), rs.getInt("BTCHENTRY"));
                }else {
                    glInfo = new Pair<>(0,0);
                }

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return glInfo;
    }

    public ArrayList<OperationsModuleRecord> getOpeartionsModulesrecords (String sourceLedger) throws SQLException {
        ArrayList<OperationsModuleRecord> operationsModuleRecords = new ArrayList<>();
        String sql="";
        if(isConnected) {
            try {
                if (sourceLedger == "OE") {
                    sql = "SELECT * FROM OESHIH";
                }else if(sourceLedger == "PO") {
                    sql = "SELECT * FROM PORCPH1";
                }

                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    OperationsModuleRecord operationsModuleRecord;
                    if (sourceLedger == "OE") {

                        operationsModuleRecord = new OperationsModuleRecord(rs.getLong("SHIUNIQ"), rs.getString("SHINUMBER"), rs.getString("BILNAME"),rs.getFloat("SHINETWTX"));
                        operationsModuleRecords.add(operationsModuleRecord);
                    } else if (sourceLedger == "PO") {
                        operationsModuleRecord = new OperationsModuleRecord(rs.getLong("RCPHSEQ"),rs.getString("RCPNUMBER"), rs.getString("VDNAME"),rs.getFloat("DOCTOTAL"));
                        operationsModuleRecords.add(operationsModuleRecord);
                    }

                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw e;
            }
        }
        return operationsModuleRecords;

    }

}
