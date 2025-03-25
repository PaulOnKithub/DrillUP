package com.drillup.drillup;


import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.net.URL;
import java.util.ResourceBundle;

public class ArController implements Initializable {

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
    private Button resetButton;

    @FXML
    private Button retrieveButton;

    Thread thread;

    Database db;

    private final String sourceLedger="AR";

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
            return "";
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
                return "";
            }
        }

    }

    @FXML
    void backToMainUI(ActionEvent event) {
        if (thread!=null) {
            thread.interrupt();
        }

        if(thread==null || !thread.isAlive() ) {
            FXMLLoader fxmlLoader = new FXMLLoader(DrillUp.class.getResource("drillUp.fxml"));
            Stage stage = (Stage) arMainPane.getScene().getWindow();
            try {
                stage.setScene(new Scene(fxmlLoader.load()));
            } catch (Exception e) {
                showError("Error loading main screen" + e.getMessage());
            }
        }

    }

    @FXML
    void resetAR(ActionEvent event) {
        arBatch.clear();
        arEntry.clear();
        arSequence.clear();
        glBatch.setText("");
        glEntry.setText("");


    }

    @FXML
    void retrieveFromAR(ActionEvent event) {
        if (arBatch.getText().isEmpty() || arEntry.getText().isEmpty() || arSequence.getText().isEmpty()) {
            return;
        } else {
            String batch=arBatch.getText().trim();
            String entry=arEntry.getText().trim();
            String sequence=arSequence.getText().trim();

            String drillDownLink=buildDrillDownLink(batch,entry,sequence);
            if (drillDownLink.isEmpty()) {
                return;
            } else {
                System.out.println(drillDownLink);
                Task<Pair<Integer,Integer>>  task = new Task<Pair<Integer,Integer>>() {
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
                        resetButton.setDisable(false);
                        retrieveButton.setDisable(false);
                        progressBar.setProgress(0);
                    }
                });

                task.setOnRunning(e -> {
                    progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                   resetButton.setDisable(true);
                   retrieveButton.setDisable(true);
                });

                task.setOnFailed(e -> {
                    showError(task.getException().getMessage());
                    progressBar.setProgress(0);
                    resetButton.setDisable(false);
                    retrieveButton.setDisable(false);
                });
                thread = new Thread(task);
                thread.start();
            }


        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progressBar.setProgress(0.0);

    }
}

