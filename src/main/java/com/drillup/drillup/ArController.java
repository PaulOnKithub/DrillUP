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

public class ArController {

    @FXML
    private Pane arMainPane;

    @FXML
    private TextField arBatch;

    @FXML
    private TextField arEntry;

    @FXML
    private TextField arSequence;

    @FXML
    private Label glBatch;

    @FXML
    private Label glEntry;

    @FXML
    private ProgressBar progressBar;

    @FXML
    void backToMainUI(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("drillUp.fxml"));
        Stage stage=(Stage) arMainPane.getScene().getWindow();
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    void resetAR(ActionEvent event) {

    }

    @FXML
    void retrieveFromAR(ActionEvent event) {

    }

    @FXML
    void initialize() {

    }

}

