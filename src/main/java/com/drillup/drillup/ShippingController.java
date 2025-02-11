package com.drillup.drillup;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;

public class ShippingController {

    @FXML
    private TextField dnNo;

    @FXML
    private Label glBatch;

    @FXML
    private Label glEntry;

    @FXML
    private Button searchButton;

    @FXML
    private Pane mainPane;

    @FXML
    private ProgressBar progressBar;

    private final String sourceLedger="OE";

    Database db;

    private String docNumber;

    private long docUniq;

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
            SearchController searchController = fxmlLoader.getController();
            searchController.setParams("OE","Document No","Customer",db);
            searchStage.showAndWait();
            if(!searchStage.isShowing()) {
                Pair<Long, String> result = searchController.getResult();
                if(result!=null) {
                    docUniq = result.getKey();
                    docNumber = result.getValue();
                    dnNo.setText(docNumber);
                }
            }
        }
        catch (IOException e) {
            showError("Error loading search screen" + e.getMessage());
        } catch (Exception e) {
            showError("Error loading search screen" + e.getMessage());
        }

    }

    public void setDb(Database db) {
        this.db = db;
        docUniq=0;
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
        dnNo.setText("");
        glBatch.setText("");
        glEntry.setText("");
        docUniq=0;

    }

    @FXML
    void retrieveFromDN(ActionEvent event) {
        if(docUniq==0) {
            showNotification("Please select a document before proceeding to retrive from GL");
            return;
        }

        Task<Pair<Integer,Integer>> task=new Task<Pair<Integer, Integer>>() {
            @Override
            protected Pair<Integer, Integer> call() throws Exception {
                return db.retrieveGLInfo(sourceLedger,docUniq);
            }
        };

        task.setOnSucceeded(e->{
            Pair<Integer,Integer> glInfo = task.getValue();
            if (glInfo.getKey()==0) {
                showNotification("No data found");
                progressBar.setProgress(0);
                return;
            } else {
                glBatch.setText(glInfo.getKey().toString());
                glEntry.setText(glInfo.getValue().toString());
                progressBar.setProgress(0);
            }
        });

        task.setOnFailed(e->{
            showError("Error retrieving GL info"+task.getException().getMessage());
            progressBar.setProgress(0);
        });

        task.setOnRunning(
                e-> {
                    progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                }
        );

        Thread thread=new Thread(task);
        thread.start();



    }

}
