package com.drillup.drillup;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.*;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import javafx.util.Pair;
import kotlin.Triple;

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
            String connectionUrl = String.format("jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false;trustServerCertificate=true",
                    connectionParams.get("SERVER NAME"),
                    connectionParams.get("PORT"),
                    connectionParams.get("DATABASE"));
            DriverManager.registerDriver(new SQLServerDriver());
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

    public long getGLInfo(String batchID,String entryID){
        long glInfo= 0;
        //connectToDatabase();
        if(isConnected) {

            try {
                String sql = "SELECT DRILLDWNLK FROM GLJEH WHERE BATCHID=? AND BTCHENTRY=? ";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(batchID));
                stmt.setInt(2, Integer.parseInt(entryID));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) glInfo = rs.getLong("DRILLDWNLK");
                return glInfo;

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return 0;

    }

    public Pair<Long,String> getGLInfo2(String batchID,String entryID){
        Pair<Long,String> retVal=new Pair<>(0L,"");
        if(isConnected) {

            try {
                String sql = "SELECT DRILLDWNLK,SRCETYPE FROM GLJEH WHERE BATCHID=? AND BTCHENTRY=? ";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(batchID));
                stmt.setInt(2, Integer.parseInt(entryID));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) retVal = new Pair<>(rs.getLong("DRILLDWNLK"), rs.getString("SRCETYPE"));
                return retVal;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return retVal;
    }

    public  Pair<String,String> retrieveFromPO(Long drillDownLink){
        Pair<String,String> poInfo = new Pair<>("","");
        //connectToDatabase();
        if(isConnected) {
            int k=Integer.parseInt(Long.toString(drillDownLink));
            try {
                String sql = "SELECT RCPNUMBER, INVNUMBER FROM PORCPH1 WHERE RCPHSEQ = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, k);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    poInfo = new Pair<>(rs.getString("RCPNUMBER"), rs.getString("INVNUMBER"));
                }else {
                    poInfo = new Pair<>("","");
                }
                return poInfo;

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return poInfo;
    }

    public  Pair<String,String> retrieveFromOE(Long drillDownLink){
        Pair<String,String> oeInfo = new Pair<>("","");
        //connectToDatabase();
        if(isConnected) {
            int k=Integer.parseInt(Long.toString(drillDownLink));

            try {
                String sql = "SELECT SHINUMBER,ORDNUMBER,LASTINVNUM FROM OESHIH WHERE SHIUNIQ=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, k);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    oeInfo = new Pair<>(rs.getString("SHINUMBER"), rs.getString("LASTINVNUM"));
                }else {
                    oeInfo = new Pair<>("","");
                }
                return oeInfo;

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return oeInfo;
    }

    public String[] retrieveFromAP(String invNumber){

        String[] apInfo=new String[4];
        //connectToDatabase();
        if(isConnected) {

            try {
                String sql = "SELECT CNTBTCH, CNTITEM, AMTINVCTOT, EXCHRATEHC FROM APIBH WHERE IDINVC= ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, invNumber);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Double total=rs.getDouble("AMTINVCTOT");
                    int batchNo=rs.getInt("CNTBTCH");
                    int entryNo=rs.getInt("CNTITEM");
                    Double rate=rs.getDouble("EXCHRATEHC");
                    apInfo[0]=String.valueOf(batchNo);
                    apInfo[1]=String.valueOf(entryNo);
                    apInfo[2]=String.valueOf(total);
                    apInfo[3]=String.valueOf(rate);
                }
                return apInfo;

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return apInfo;
    }

    public String[] retrieveFromAR(String invNumber){

        String[] arInfo=new String[4];
        //connectToDatabase();
        if(isConnected) {

            try {
                String sql = "SELECT CNTBTCH, CNTITEM, AMTINVCTOT, EXCHRATEHC FROM ARIBH WHERE IDINVC= ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, invNumber);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Double total=rs.getDouble("AMTINVCTOT");
                    int batchNo=rs.getInt("CNTBTCH");
                    int entryNo=rs.getInt("CNTITEM");
                    Double rate=rs.getDouble("EXCHRATEHC");
                    arInfo[0]=String.valueOf(batchNo);
                    arInfo[1]=String.valueOf(entryNo);
                    arInfo[2]=String.valueOf(total);
                    arInfo[3]=String.valueOf(rate);
                }
                return arInfo;

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return arInfo;
    }

    public Optional<String[]> getApInfoFromGlLink(Pair<String, String> apInfo, String app) {
        String[] apInfoRet = null;

        if(app.equals("IN")|| app.equals("CR") || app.equals("DB")){
            if(isConnected) {
                try {
                    String sql = "SELECT IDVEND,DATEINVC,DATEBUS,AMTINVCTOT,AMTGROSTOT,IDINVC FROM APIBH WHERE CNTBTCH=? AND CNTITEM=?";
                    PreparedStatement statement = conn.prepareStatement(sql);
                    statement.setLong(1, Long.valueOf(apInfo.getKey()));
                    statement.setLong(2, Long.valueOf(apInfo.getValue()));
                    var rs = statement.executeQuery();
                    while (rs.next()) {
                        apInfoRet = new String[]{rs.getString("IDVEND"), rs.getBigDecimal("DATEINVC").toString(),
                                rs.getBigDecimal("DATEBUS").toString(), rs.getBigDecimal("AMTINVCTOT").toString(),
                                rs.getBigDecimal("AMTGROSTOT").toString(),rs.getString("IDINVC")
                        };
                    }
                    return Optional.ofNullable(apInfoRet);
                } catch (Exception e) {
                    System.out.println("ERROR RETRIEVING VENDOR INFO FROM APIBH -- " + e.getMessage());
                }
            }
            }else if ( app.equals("PY") || app.equals("PI")){
                if(isConnected){
                    try{
                        String sql="SELECT IDVEND,DATERMIT,DATEBUS,AMTRMIT,AMTRMITHC,DOCNBR FROM APTCR WHERE CNTBTCH=? AND CNTENTR=?";
                        PreparedStatement statement=conn.prepareStatement(sql);
                        statement.setLong(1,Long.valueOf(apInfo.getKey()));
                        statement.setLong(2,Long.valueOf(apInfo.getValue()));
                        var rs=statement.executeQuery();
                        while (rs.next()){
                            apInfoRet=new String[]{rs.getString("IDVEND"),rs.getBigDecimal("DATERMIT").toString(),
                                    rs.getBigDecimal("DATEBUS").toString(),rs.getBigDecimal("AMTRMIT").toString(),
                                    rs.getBigDecimal("AMTRMITHC").toString(),rs.getString("DOCNBR")
                            };                        }
                        return Optional.ofNullable(apInfoRet);
                    } catch (Exception e) {
                        System.out.println("ERROR RETRIEVING VENDOR INFO FROM APTCR-- "+ e.getMessage());
                    }
                }
            }
        return Optional.ofNullable(apInfoRet);
    }

    public Optional<String[]> getArInfoFromGlLink(Pair<String, String> apInfo, String app) {
        String[] apInfoRet = null;

        if(app.equals("IN")|| app.equals("CR") || app.equals("DB")){
            if(isConnected) {
                try {
                    String sql = "SELECT IDCUST,DATEINVC,DATEBUS,AMTINVCTOT,AMTGROSHC,IDINVC FROM ARIBH WHERE CNTBTCH=? AND CNTITEM=?";
                    PreparedStatement statement = conn.prepareStatement(sql);
                    statement.setLong(1, Long.valueOf(apInfo.getKey()));
                    statement.setLong(2, Long.valueOf(apInfo.getValue()));
                    var rs = statement.executeQuery();
                    while (rs.next()) {
                        apInfoRet = new String[]{rs.getString("IDCUST"), rs.getBigDecimal("DATEINVC").toString(),
                                rs.getBigDecimal("DATEBUS").toString(), rs.getBigDecimal("AMTINVCTOT").toString(),
                                rs.getBigDecimal("AMTGROSHC").toString(),rs.getString("IDINVC")
                        };
                    }
                    return Optional.ofNullable(apInfoRet);
                } catch (Exception e) {
                    System.out.println("ERROR RETRIEVING VENDOR INFO FROM APIBH -- " + e.getMessage());
                }
            }
        }else if ( app.equals("PY") || app.equals("PI")){
            if(isConnected){
                try{
                    String sql="SELECT IDCUST,DATERMIT,DATEBUS,AMTRMIT,AMTRMITHC,DOCNBR FROM ARTCR WHERE CNTBTCH=? AND CNTITEM=?";
                    PreparedStatement statement=conn.prepareStatement(sql);
                    statement.setLong(1,Long.valueOf(apInfo.getKey()));
                    statement.setLong(2,Long.valueOf(apInfo.getValue()));
                    var rs=statement.executeQuery();
                    while (rs.next()){
                        apInfoRet=new String[]{rs.getString("IDCUST"),rs.getBigDecimal("DATERMIT").toString(),
                                rs.getBigDecimal("DATEBUS").toString(),rs.getBigDecimal("AMTRMIT").toString(),
                                rs.getBigDecimal("AMTRMITHC").toString(),rs.getString("DOCNBR")
                        };                        }
                    return Optional.ofNullable(apInfoRet);
                } catch (Exception e) {
                    System.out.println("ERROR RETRIEVING VENDOR INFO FROM APTCR-- "+ e.getMessage());
                }
            }
        }
        return Optional.ofNullable(apInfoRet);
    }


}
