package com.drillup.drillup;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ShippingController {

    @FXML
    private TextField dnNo;

    @FXML
    private Label glBatch;

    @FXML
    private Label glEntry;

    @FXML
    private Pane mainPane;

    @FXML
    private ProgressBar progressBar;

    private final String sourceLedger="OE";

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
    void searchScreen(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("searchForm.fxml"));
        Stage searchStage = new Stage();
        searchStage.initOwner(mainPane.getScene().getWindow());
        try {
            Scene searchScene = new Scene(fxmlLoader.load());
            searchStage.setScene(searchScene);
            searchStage.alwaysOnTopProperty();
            searchStage.initOwner(mainPane.getScene().getWindow());
            searchStage.setResizable(false);
            searchStage.initModality(Modality.APPLICATION_MODAL);
            searchStage.showAndWait();
        }
        catch (IOException e) {
            showError("Error loading search screen" + e.getMessage());
        } catch (Exception e) {
            showError("Error loading search screen" + e.getMessage());
        }

    }

    public void setDb(Database db) {
        this.db = db;
    }

    @FXML
    void backToMainUI(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("drillUp.fxml"));
        Stage stage=(Stage) mainPane.getScene().getWindow();
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
        } catch (Exception e) {
            showError("Error loading main screen" + e.getMessage());
        }

    }

    @FXML
    void reset(ActionEvent event) {

    }

    @FXML
    void retrieveFromDN(ActionEvent event) {

    }

}
