package com.drillup.drillup;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Optional;

public class MainController {

    @FXML
    private Pane mainPane;

    @FXML
    void apScreen(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("apBatchForm.fxml"));
        Stage stage=(Stage) mainPane.getScene().getWindow();
        stage.setResizable(false);
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @FXML
    void arScreen(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("arBatchForm.fxml"));
        Stage stage=(Stage) mainPane.getScene().getWindow();
        stage.setResizable(false);
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    void oeScreen(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("shippingBatchForm.fxml"));
        Stage stage=(Stage) mainPane.getScene().getWindow();
        stage.setResizable(false);
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    void poScreen(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("grnBatchForm.fxml"));
        Stage stage=(Stage) mainPane.getScene().getWindow();
        stage.setResizable(false);
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    void exit(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to exit?");
        alert.showAndWait();
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            // ... user chose OK
            Platform.exit();
        } else {
            // ... user chose CANCEL or closed the dialog
        }
    }

    @FXML
    void drillUpSettings(ActionEvent event) {

    }




    @FXML
    void initialize() {
        Database db = new Database();

    }
}
