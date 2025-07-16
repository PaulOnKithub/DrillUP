package com.drillup.drillup;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import javafx.util.Pair;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;

public class MainController {

    @FXML
    private Pane mainPane;
    @FXML
    private ProgressBar progress2;
    Database db;
    String fileLocation="";



    void showNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage alertStage=(Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResource("Icon.png").toString()));
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        Stage alertStage=(Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResource("Icon.png").toString()));
        alert.setTitle("Error Dialog");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    Pair<String, String> parseApArDrillDown(String link){
        // Input validation
        if (link == null || link.length() != 18) {
            return new Pair<>("", "");
        }

        // Check for error codes
        if (link.equals("000000000000000000") || link.equals("111111111111111111")) {
            return new Pair<>("", "");
        }

        try {
            // Extract sequence length (1st character)
            int sequenceLength = Character.getNumericValue(link.charAt(0));

            // Extract batch length (2nd character)
            int batchLength = Character.getNumericValue(link.charAt(1));

            // Validate lengths
            if (sequenceLength < 0 || batchLength < 0 || sequenceLength > 9 || batchLength > 9) {
                return new Pair<>("", "");
            }

            // Extract sequence (starts at position 2)
            int sequenceStart = 2;
            int sequenceEnd = sequenceStart + sequenceLength;
            String sequence = link.substring(sequenceStart, sequenceEnd);

            // Extract batch (starts after sequence)
            int batchStart = sequenceEnd;
            int batchEnd = batchStart + batchLength;
            String batch = link.substring(batchStart, batchEnd);

            // Calculate where entry starts (after leading zeros)
            // Format: [seqLen][batchLen][sequence][batch][leadingZeros][entry]
            // Total length is 18, so: 1 + 1 + seqLen + batchLen + leadingZeros + entryLen = 18
            int currentPosition = 2 + sequenceLength + batchLength;

            // Find the end of leading zeros (look for first non-zero or reach end)
            int entryStart = currentPosition;
            while (entryStart < 18 && link.charAt(entryStart) == '0') {
                entryStart++;
            }

            // Extract entry (from end of leading zeros to end of string)
            String entry = "";
            if (entryStart < 18) {
                entry = link.substring(entryStart);
            }
            return new Pair<>(batch, entry);
        } catch (Exception e) {
            return new Pair<>("", "");
        }
    }

    private void loadAndUpdateLinkAP(String fileLocation, BiConsumer<Integer, Integer> progressCallback) {
        try {
            FileInputStream file = new FileInputStream(new File(fileLocation));
            Workbook workbook = new HSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            Database db = new Database();
            db.connectToDatabase();
            int totalRows = sheet.getLastRowNum();
            DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            for (int i = 1; i <= totalRows; i++) { // Start from the second row
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell batchCell = row.getCell(0); // Assuming batchID is in the first column
                Cell entryCell = row.getCell(1); // Assuming entryID is in the second column

                if (batchCell == null || entryCell == null) continue;

                String batchID = batchCell.getCellType() == CellType.STRING ? batchCell.getStringCellValue() : String.valueOf((int) batchCell.getNumericCellValue());
                String entryID = entryCell.getCellType() == CellType.STRING ? entryCell.getStringCellValue() : String.valueOf((int) entryCell.getNumericCellValue());

               //get a pair of drilllink and source application
                var linkInfo=db.getGLInfo2(batchID,entryID);
                if(linkInfo.getKey()==0L) continue;
                Pair<String, String> apInfo=parseApArDrillDown(String.valueOf(linkInfo.getKey()));
                String vendorNo="NULL";
                Optional<String[]> returndedValuesFromAp=null;
                if (apInfo.getKey() != null && !apInfo.getKey().isEmpty()) {
                    returndedValuesFromAp = db.getApInfoFromGlLink(apInfo, linkInfo.getValue());
                }

                //String[] apInfo2= returndedValuesFromAp.get()!=null ? returndedValuesFromAp.get() : null;
                if (returndedValuesFromAp.isEmpty() || returndedValuesFromAp==null || !returndedValuesFromAp.isPresent()) continue;
                String[] apInfo2= returndedValuesFromAp.get();
                if(apInfo2!=null){
                    row.createCell(2).setCellValue(apInfo2[0]);
                    row.createCell(3).setCellValue(LocalDate.parse(apInfo2[1], customFormatter));
                    row.createCell(4).setCellValue(LocalDate.parse(apInfo2[2],customFormatter));
                    row.createCell(5).setCellValue(apInfo2[3]);
                    row.createCell(6).setCellValue(apInfo2[4]);
                    row.createCell(7).setCellValue(apInfo2[5]);
                }
                // Update the progress bar on the main thread
                int currentRow = i;
                Platform.runLater(() -> progressCallback.accept(currentRow, totalRows));
            }

            FileOutputStream outFile = new FileOutputStream(new File(fileLocation));
            workbook.write(outFile);
            outFile.close();
            workbook.close();
            db.closeConnection();

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            System.out.println("ERROR RETRIEVEING --"+e.getMessage());

        }

    }

    public void loadAndUpdateLinkPO(String fileLocation, BiConsumer<Integer, Integer> progressCallback){
        try {
            FileInputStream file = new FileInputStream(new File(fileLocation));
            Workbook workbook = new HSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            Database db = new Database();
            db.connectToDatabase();
            int totalRows = sheet.getLastRowNum();

            for (int i = 0; i <= totalRows; i++) { // Start from the second row
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell batchCell = row.getCell(0); // Assuming batchID is in the first column
                Cell entryCell = row.getCell(1); // Assuming entryID is in the second column

                if (batchCell == null || entryCell == null) continue;

                //String batchID=String.valueOf(batchCell);
                //String entryID=String.valueOf(entryCell);
                String batchID = batchCell.getCellType() == CellType.STRING ? batchCell.getStringCellValue() : String.valueOf((int) batchCell.getNumericCellValue());
                String entryID = entryCell.getCellType() == CellType.STRING ? entryCell.getStringCellValue() : String.valueOf((int) entryCell.getNumericCellValue());

                Long rctDrill = db.getGLInfo(batchID,entryID);

                Pair<String, String> rcpInfo = db.retrieveFromPO(rctDrill);
                String grnNo = rcpInfo.getKey();
                String invNo = rcpInfo.getValue();

                String[] apInfo=new String[4];
                apInfo= db.retrieveFromAP(invNo);

                // Update the row with new values
                if(rctDrill>0){
                    row.createCell(2).setCellValue(grnNo); // Store Grn in column 3
                    row.createCell(3).setCellValue(invNo); // Store Invoice in column 4
                    row.createCell(4).setCellValue(apInfo[0]); // Store Invoice Batch in column 5
                    row.createCell(5).setCellValue(apInfo[1]); // Store Invoice entry in column 6
                    row.createCell(6).setCellValue(Double.valueOf(apInfo[2]));
                    row.createCell(7).setCellValue(Double.valueOf(apInfo[3]));
                }

                // Update the progress bar on the main thread
                int currentRow = i;
                Platform.runLater(() -> progressCallback.accept(currentRow, totalRows));
            }

            FileOutputStream outFile = new FileOutputStream(new File(fileLocation));
            workbook.write(outFile);
            outFile.close();
            workbook.close();
            db.closeConnection();

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();

        }

    }

    public void loadAndUpdateLinkOE(String fileLocation, BiConsumer<Integer, Integer> progressCallback){
        try {
            FileInputStream file = new FileInputStream(new File(fileLocation));
            Workbook workbook = new HSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            Database db = new Database();
            db.connectToDatabase();
            int totalRows = sheet.getLastRowNum();

            for (int i = 0; i <= totalRows; i++) { // Start from the second row
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell batchCell = row.getCell(0); // Assuming batchID is in the first column
                Cell entryCell = row.getCell(1); // Assuming entryID is in the second column

                if (batchCell == null || entryCell == null) continue;

                String batchID = batchCell.getCellType() == CellType.STRING ? batchCell.getStringCellValue() : String.valueOf((int) batchCell.getNumericCellValue());
                String entryID = entryCell.getCellType() == CellType.STRING ? entryCell.getStringCellValue() : String.valueOf((int) entryCell.getNumericCellValue());

                Long dnDrill = db.getGLInfo(batchID,entryID);

                Pair<String, String> rcpInfo = db.retrieveFromOE(dnDrill);
                String grnNo = rcpInfo.getKey();
                String invNo = rcpInfo.getValue();

                String[] arInfo=new String[4];
                arInfo= db.retrieveFromAR(invNo);


                // Update the row with new values
                if(dnDrill>0 & !(arInfo[2]==null) ){
                    row.createCell(2).setCellValue(grnNo); // Store Grn in column 3
                    row.createCell(3).setCellValue(invNo);
                    row.createCell(4).setCellValue(arInfo[0]); // Store Invoice Batch in column 5
                    row.createCell(5).setCellValue(arInfo[1]); // Store Invoice entry in column 6
                    row.createCell(6).setCellValue(Double.valueOf(arInfo[2]));
                    row.createCell(7).setCellValue(Double.valueOf(arInfo[3]));// Store Invoice in column 4
                }

                // Update the progress bar on the main thread
                int currentRow = i;
                Platform.runLater(() -> progressCallback.accept(currentRow, totalRows));
            }

            FileOutputStream outFile = new FileOutputStream(new File(fileLocation));
            workbook.write(outFile);
            outFile.close();
            workbook.close();
            db.closeConnection();

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();

        }

    }

    private void openExcelFile() {
        File excelFile=new File(fileLocation);
        if (excelFile.exists()) {
            if (Desktop.isDesktopSupported()) {
                new Thread(() -> {
                    try {
                        Desktop.getDesktop().open(excelFile);
                        System.out.println("Excel file opened successfully.");
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle opening error (e.g., show an Alert to the user)
                        // This needs to be run on the JavaFX Application Thread if it modifies UI
                        Platform.runLater(() -> {
                            new Alert(Alert.AlertType.ERROR, "Could not open Excel file.\n" +
                                    "Please ensure Excel is installed and associated with .xlsx files.\n" +
                                    "Error: " + e.getMessage());
                        });
                    }
                }).start();
            }
        }
    }


    @FXML
    void apScreen(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("apBatchForm.fxml"));
        Stage stage=(Stage) mainPane.getScene().getWindow();
        stage.setResizable(false);
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
            ApController apController = fxmlLoader.getController();
            apController.setDb(db);
        } catch (Exception e) {
            showError("Error loading AP screen" + e.getMessage() + e.getCause());

        }


    }

    @FXML
    void arScreen(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("arBatchForm.fxml"));
        Stage stage=(Stage) mainPane.getScene().getWindow();
        stage.setResizable(false);
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
            ArController arController = fxmlLoader.getController();
            arController.setDb(db);
            stage.show();
        } catch (Exception e) {
           showError("Error loading AR screen" + e.getMessage() + e.getCause());
        }

    }

    @FXML
    void oeScreen(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("shippingBatchForm.fxml"));
        Stage stage=(Stage) mainPane.getScene().getWindow();
        stage.setResizable(false);
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
            ShippingController shippingController = fxmlLoader.getController();
            shippingController.setDb(db);
        } catch (Exception e) {
           //showError("Error loading shipping screen" + e.getMessage() + e.getCause());
            e.printStackTrace();
        }

    }

    @FXML
    void poScreen(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("grnBatchForm.fxml"));
        Stage stage=(Stage) mainPane.getScene().getWindow();
        stage.setResizable(false);

        try {
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.getIcons().add(new Image(getClass().getResource("Icon.png").toString()));
            GrnController grnController = fxmlLoader.getController();
            grnController.setDb(db);
        } catch (Exception e) {
            //showError("Error loading GRN screen" + e.getMessage() + e.getCause());
            e.printStackTrace();
        }

    }

    @FXML
    void exit(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Stage alertStage=(Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResource("Icon.png").toString()));
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to exit?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            // ... user chose
            db.closeConnection();
            Platform.exit();
        }
    }

    @FXML
    void drillUpSettings(ActionEvent event) {

        // Create the custom dialog.
        Dialog<Map<String, String>> dialog = new Dialog<>();
        Stage alertStage=(Stage) dialog.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResource("Icon.png").toString()));
        dialog.setTitle("Database Connection Dialog");
        dialog.setHeaderText("Enter Database Connection Parameters");

        // Set the icon (must be included in the project).
        // dialog.setGraphic(new ImageView(this.getClass().getResource("database.png").toString()));

        // Set the button types.
        ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

        // Create the labels and fields for the database parameters.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField serverNameField = new TextField();
        serverNameField.setPromptText("Server Name");

        TextField serverPort= new TextField();
        serverPort.setPromptText("Port");

        TextField userField = new TextField();
        userField.setPromptText("User");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField databaseField = new TextField();
        databaseField.setPromptText("Database");

        grid.add(new Label("Server Name:"), 0, 0);
        grid.add(serverNameField, 1, 0);
        grid.add(new Label("Port :"),0,1);
        grid.add(serverPort,1,1);
        grid.add(new Label("User:"), 0, 2);
        grid.add(userField, 1, 2);
        grid.add(new Label("Password:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Database:"), 0, 4);
        grid.add(databaseField, 1, 4);

        // Enable/Disable connect button depending on whether all fields are filled.
        Node connectButton = dialog.getDialogPane().lookupButton(connectButtonType);
        connectButton.setDisable(true);

        ChangeListener<String> validationListener = (observable, oldValue, newValue) -> {
            boolean allFieldsFilled = !serverNameField.getText().trim().isEmpty() &&
                    !userField.getText().trim().isEmpty() &&
                    !passwordField.getText().trim().isEmpty() &&
                    !serverPort.getText().trim().isEmpty() &&
                    !databaseField.getText().trim().isEmpty();
            connectButton.setDisable(!allFieldsFilled);
        };

        serverNameField.textProperty().addListener(validationListener);
        serverPort.textProperty().addListener(validationListener);
        userField.textProperty().addListener(validationListener);
        passwordField.textProperty().addListener(validationListener);
        databaseField.textProperty().addListener(validationListener);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the server name field by default.
        Platform.runLater(() -> serverNameField.requestFocus());

        // Convert the result to a map of database connection parameters.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                Map<String, String> connectionParams = new HashMap<>();
                connectionParams.put("SERVERNAME", serverNameField.getText());
                connectionParams.put("PORT",serverPort.getText());
                connectionParams.put("USER", userField.getText());
                connectionParams.put("PASSWORD", passwordField.getText());
                connectionParams.put("DATABASE", databaseField.getText());
                return connectionParams;
            }
            return null;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();

        result.ifPresent(connectionParams -> {
            try {
                // Create or overwrite the file "connection.txt".
                File file = new File("connection.txt");
                PrintWriter writer = new PrintWriter(file);

                // Write the connection parameters to the file.
                writer.println("Server Name: " + connectionParams.get("SERVERNAME"));
                writer.println("Port: "+connectionParams.get("PORT"));
                writer.println("User: " + connectionParams.get("USER"));
                writer.println("Password: " + connectionParams.get("PASSWORD"));
                writer.println("Database: " + connectionParams.get("DATABASE"));

                // Close the writer to ensure data is saved.
                writer.close();

                // Optional: Print a success message to the console.
                System.out.println("Connection parameters saved to " + file.getAbsolutePath());
            } catch (IOException e) {
                // Handle any exceptions that might occur during file writing.
                System.err.println("An error occurred while saving connection parameters: " + e.getMessage());
            }
            db = new Database();
            db.connectToDatabase();
            if(db.isConnected()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Connection Successful");
                alert.setHeaderText("Connection Successful");
                alert.setContentText("Connection to the database was successful.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Connection Failed");
                alert.setHeaderText("Connection Failed");
                alert.setContentText("Connection to the database failed.");
                alert.showAndWait();
            }

        });

    }

    @FXML
    void linkRct(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            // Handle the selected file
            fileLocation = selectedFile.getAbsolutePath();
        }else {
            showNotification("You have not selected a file for processing");
            return;
        }
        Task<Void> task=new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadAndUpdateLinkPO(fileLocation, (currentRow, totalRows) -> {
                    updateProgress(currentRow, totalRows);
                });
                return null;
            }
        };

        progress2.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Drilldown retrieval successful");
            alert.setContentText("The drilldown values have been successfully retrieved and updated in the file");
            alert.showAndWait();
            progress2.progressProperty().unbind();
            progress2.setProgress(0);
        });

        task.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Drilldown retrieval failed");
            alert.setContentText("An error occurred while retrieving the drilldown values"+e.toString());
            alert.showAndWait();
            progress2.progressProperty().unbind();
            progress2.setProgress(0);
        });
        System.out.println("Entering thread");
        new Thread(task).start();


    }

    @FXML
    void linkDN(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            // Handle the selected file
            fileLocation = selectedFile.getAbsolutePath();
        }else {
            showNotification("You have not selected a file for processing");
            return;
        }

        Task<Void> task=new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadAndUpdateLinkOE(fileLocation, (currentRow, totalRows) -> {
                    updateProgress(currentRow, totalRows);
                });
                return null;
            }
        };

        progress2.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Drilldown retrieval successful");
            alert.setContentText("The drilldown values have been successfully retrieved and updated in the file");
            alert.showAndWait();
            progress2.progressProperty().unbind();
            progress2.setProgress(0);
        });

        task.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Drilldown retrieval failed");
            alert.setContentText("An error occurred while retrieving the drilldown values"+e.toString());
            System.out.println(task.getException().getMessage());
            System.out.println(task.getException().getCause());
            alert.showAndWait();
            progress2.progressProperty().unbind();
            progress2.setProgress(0);
        });
        System.out.println("Entering thread");
        new Thread(task).start();
    }

    @FXML
    void linkAP(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            // Handle the selected file
            fileLocation = selectedFile.getAbsolutePath();
        }else {
            showNotification("You have not selected a file for processing");
            return;
        }

        Task<Void> task=new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadAndUpdateLinkAP(fileLocation, (currentRow, totalRows) -> {
                    updateProgress(currentRow, totalRows);
                });
                return null;
            }
        };

        progress2.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Drilldown retrieval successful");
            alert.setContentText("The drilldown values have been successfully retrieved and updated in the file");
            alert.showAndWait();
            progress2.progressProperty().unbind();
            progress2.setProgress(0);
            openExcelFile();
        });

        task.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Drilldown retrieval failed");
            alert.setContentText("An error occurred while retrieving the drilldown values"+e.toString());
            System.out.println(task.getException().getMessage());
            System.out.println(task.getException().getCause());
            task.getException().printStackTrace();
            alert.showAndWait();
            progress2.progressProperty().unbind();
            progress2.setProgress(0);
        });
        System.out.println("Entering thread");
        new Thread(task).start();

    }

    @FXML
    void initialize() {
        progress2.setProgress(0);
        db = new Database();
        db.connectToDatabase();
        if (!db.isConnected()) {
            drillUpSettings(null);
        }        //show dialog box where user will specify the server name, database name, username and password

    }
}
