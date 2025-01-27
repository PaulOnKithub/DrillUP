package com.drillup.drillup;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

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

    Database db;

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
            e.printStackTrace();
        }

    }

    @FXML
    void reset(ActionEvent event) {

    }

    @FXML
    void retrieveFromDN(ActionEvent event) {

    }

}
