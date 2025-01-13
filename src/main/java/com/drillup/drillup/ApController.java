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

public class ApController {

    @FXML
    private Pane apMainPane;

    @FXML
    private TextField apBatch;

    @FXML
    private TextField apEntry;

    @FXML
    private TextField apSequence;

    @FXML
    private Label glBatch;

    @FXML
    private Label glEntry;

    @FXML
    private ProgressBar progressBar;

    @FXML
    void backToMainUI(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("drillUp.fxml"));
        Stage stage=(Stage) apMainPane.getScene().getWindow();
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    void resetAP(ActionEvent event) {
        apBatch.clear();
        apEntry.clear();
        apSequence.clear();
        glBatch.setText("");
        glEntry.setText("");
    }

    @FXML
    void retrieveFromAP(ActionEvent event) {

    }

    @FXML
    void initialize() {

    }

}

