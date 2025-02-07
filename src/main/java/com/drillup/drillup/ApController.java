package com.drillup.drillup;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ApController implements Initializable {

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
    private Button resetButton;

    @FXML
    private Button retrieveButton;

    private Thread thread;


    Database db;

    private final String sourceLedger="AP";

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

    public void setDb(Database db) {
        this.db = db;
    }

    public String buildDrillDownLink(String batch, String entry, String sequence) {
      if (batch.isEmpty() || entry.isEmpty() || sequence.isEmpty()) {
        return "000000";
      } else {
        int BatchLength = batch.length();
        int sequenceLength = sequence.length();
        int entryLength = entry.length();
        int totalLength = BatchLength + sequenceLength + entryLength;
        if(totalLength<=16){

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
          return "111111";
        }
      }

    }

    @FXML
    void backToMainUI(ActionEvent event) {

        if (thread != null) {
            thread.interrupt();
        }

        //if thread is not alive, load the main screen
        if (thread==null || !thread.isAlive()) {
            FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("drillUp.fxml"));
            Stage stage = (Stage) apMainPane.getScene().getWindow();
            try {
                stage.setScene(new Scene(fxmlLoader.load()));
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        if (drillDownLink.isEmpty()) {
            return;
        } else {
            System.out.println(drillDownLink);
            Task<Pair<Integer,Integer>> task = new Task<Pair<Integer,Integer>>() {
                @Override
                protected Pair<Integer,Integer> call() throws Exception {
                    return db.retrieveGLInfo(sourceLedger,Long.parseLong(drillDownLink));
                }
            };

            task.setOnSucceeded(e -> {
                Pair<Integer,Integer> glInfo = task.getValue();
                if (glInfo.getKey()==0) {
                    showNotification("No data found");
                    resetButton.setDisable(false);
                    retrieveButton.setDisable(false);
                    progressBar.setProgress(0);
                    return;
                } else {
                    glBatch.setText(glInfo.getKey().toString());
                    glEntry.setText(glInfo.getValue().toString());
                }
                progressBar.setProgress(0);
                resetButton.setDisable(false);
                retrieveButton.setDisable(false);
            });

            task.setOnRunning(e -> {
                progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                resetButton.setDisable(true);
                retrieveButton.setDisable(true);
            });

            task.setOnFailed(e -> {
                progressBar.setProgress(0);
                showError(task.getException().getMessage());
                resetButton.setDisable(false);
                retrieveButton.setDisable(false);
            });

            thread = new Thread(task);
            thread.start();

        }


    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


    }

    //terminate thread
    //handle case when query returns nothing






}

