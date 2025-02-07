package com.drillup.drillup;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MainController {

    @FXML
    private Pane mainPane;
    Database db;


    void showNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    void apScreen(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("apBatchForm.fxml"));
        Stage stage=(Stage) mainPane.getScene().getWindow();
        stage.setResizable(false);
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
            ApController apController = fxmlLoader.getController();
            apController.setDb(db);
        } catch (Exception e) {
            showError("Error loading AP screen" + e.getMessage());
        }


    }

    @FXML
    void arScreen(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("arBatchForm.fxml"));
        Stage stage=(Stage) mainPane.getScene().getWindow();
        stage.setResizable(false);
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
            ArController arController = fxmlLoader.getController();
            arController.setDb(db);
            stage.show();
        } catch (Exception e) {
           showError("Error loading AR screen" + e.getMessage());
        }

    }

    @FXML
    void oeScreen(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("shippingBatchForm.fxml"));
        Stage stage=(Stage) mainPane.getScene().getWindow();
        stage.setResizable(false);
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
            ShippingController shippingController = fxmlLoader.getController();
            shippingController.setDb(db);
        } catch (Exception e) {
           showError("Error loading shipping screen" + e.getMessage());
        }

    }

    @FXML
    void poScreen(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("grnBatchForm.fxml"));
        Stage stage=(Stage) mainPane.getScene().getWindow();
        stage.setResizable(false);

        try {
            stage.setScene(new Scene(fxmlLoader.load()));
            GrnController grnController = fxmlLoader.getController();
            grnController.setDb(db);
        } catch (Exception e) {
            showError("Error loading GRN screen" + e.getMessage());
        }

    }

    @FXML
    void exit(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
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

        TextField userField = new TextField();
        userField.setPromptText("User");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField databaseField = new TextField();
        databaseField.setPromptText("Database");

        grid.add(new Label("Server Name:"), 0, 0);
        grid.add(serverNameField, 1, 0);
        grid.add(new Label("User:"), 0, 1);
        grid.add(userField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Database:"), 0, 3);
        grid.add(databaseField, 1, 3);

        // Enable/Disable connect button depending on whether all fields are filled.
        Node connectButton = dialog.getDialogPane().lookupButton(connectButtonType);
        connectButton.setDisable(true);

        ChangeListener<String> validationListener = (observable, oldValue, newValue) -> {
            boolean allFieldsFilled = !serverNameField.getText().trim().isEmpty() &&
                    !userField.getText().trim().isEmpty() &&
                    !passwordField.getText().trim().isEmpty() &&
                    !databaseField.getText().trim().isEmpty();
            connectButton.setDisable(!allFieldsFilled);
        };

        serverNameField.textProperty().addListener(validationListener);
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
    void initialize() {
        db = new Database();
        db.connectToDatabase();
        if (!db.isConnected()) {
            drillUpSettings(null);
        }        //show dialog box where user will specify the server name, database name, username and password

    }
}
