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

public class GrnController {

    @FXML
    private Label glBatch;

    @FXML
    private Label glEntry;

    @FXML
    private TextField grnNo;

    @FXML
    private Pane mainPane;

    @FXML
    private ProgressBar progressBar;

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
    void resetGRN(ActionEvent event) {

    }

    @FXML
    void retrieveFromGRN(ActionEvent event) {

    }

}

