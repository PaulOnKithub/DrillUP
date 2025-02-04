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


    Database db;

    private final String sourceLedger="AP";

    public void setDb(Database db) {
        this.db = db;
    }

    public String buildDrillDownLink(String batch, String entry, String sequence) {
      if (batch.isEmpty() || entry.isEmpty() || sequence.isEmpty()) {
        return "";
      } else {
        var BatchLength = batch.length();
        var sequenceLength = sequence.length();
        var entryLength = entry.length();
        var totalLength = BatchLength + sequenceLength + entryLength;
        if(totalLength<=16){
          //build a 18 character string
            //first character is the sequencelenght
            //second charcter the batch length
            //from the third charcter upto the lenght of the sequence is the sequence
            //from the length of the sequence to the length of the batch is the batch
            //from the batch number up the entry number are leading zeros
            //from the entry number up to the end of the string is the entry number
            StringBuilder drillDownLink = new StringBuilder();
            drillDownLink.append(sequenceLength);
            drillDownLink.append(BatchLength);
            drillDownLink.append(sequence);
            drillDownLink.append(batch);
            int leadingZeros=18-drillDownLink.length()-entryLength;
            for(int i=0;i<leadingZeros;i++){
              drillDownLink.append("0");
            }
            drillDownLink.append(entry);
            return drillDownLink.toString();
        } else {
          return "";
        }
      }

    }

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
    void apSearch(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("searchForm.fxml"));
        Stage searchStage = new Stage();
        try {
            Scene searchScene = new Scene(fxmlLoader.load());
            searchStage.setScene(searchScene);
            searchStage.alwaysOnTopProperty();
            searchStage.initOwner(apMainPane.getScene().getWindow());
            searchStage.setResizable(false);
            searchStage.initModality(Modality.APPLICATION_MODAL);
            searchStage.showAndWait();
        }
        catch (IOException e) {
            System.err.println("Error loading file"+e.getMessage());
        } catch (Exception e) {
            System.err.println("Error loading file"+e.getMessage());
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
        if(apBatch.getText().isEmpty() || apEntry.getText().isEmpty() || apSequence.getText().isEmpty()){
            return;
        }
        String batch=apBatch.getText().trim();
        String entry=apEntry.getText().trim();
        String sequence=apSequence.getText().trim();
        String drillDownLink=buildDrillDownLink(batch,entry,sequence);
        int drillDownNumber=Integer.getInteger(drillDownLink);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Drill Up Link");
        alert.setHeaderText("Drill Up Link");
        alert.setContentText(drillDownLink);


    }

    @FXML
    void initialize() {

    }



}

