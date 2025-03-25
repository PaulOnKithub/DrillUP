package com.drillup.drillup;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SearchController implements Initializable {
    //@FXML
    //private ListView<OperationsModuleRecord> resultsBox;
    @FXML
    private VBox vBox;

    @FXML
    private Label searchLabel1;

    @FXML
    private Label searchLabel2;

    @FXML
    private TextField searchParam1;

    @FXML
    private TextField searchParam2;

    private Database db;

    private long docUniq;

    private String docNumber;

    String searchID;
    String searchModule;
    String searchCoumn1;
    String searchCoumn2;



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

    void addItems(ArrayList<OperationsModuleRecord> records){
        vBox.setSpacing(2);

        for(OperationsModuleRecord items:records){
            HBox item=new HBox(10);
            item.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 5;");
            item.setUserData(items);
            item.getChildren().add(new Label(items.toString()));
            vBox.getChildren().add(item);
            item.setOnMouseClicked(e->{
                if(e.getClickCount()==2)itemSelected(e);
            });
        }
    }




   public void setParams(String searchModule, String searchCoumn1, String searchCoumn2, Database db) {

        this.searchModule = searchModule;
        this.searchCoumn1 = searchCoumn1;
        this.searchCoumn2 = searchCoumn2;
        this.db = db;

        searchLabel1.setText(searchCoumn1);
        searchLabel2.setText(searchCoumn2);

       ArrayList<OperationsModuleRecord> records;
       try{
           db.connectToDatabase();
           if(db.isConnected()){
               records = db.getOpeartionsModulesrecords(searchModule);
               //resultsBox.getItems().addAll(records);
               addItems(records);

           }
       } catch (SQLException e) {
           showError("Error retrieving records" + e.getMessage());
       }
    }

    @FXML
    void exitSearch(ActionEvent event) {
        Stage stage=(Stage) vBox.getScene().getWindow();
        stage.close();
    }

    @FXML
    void itemSelected(MouseEvent event) {
        if(event.getClickCount()==2) {
            /*OperationsModuleRecord record = resultsBox.getSelectionModel().getSelectedItem();
            if(record!=null){
                docNumber = record.getId();
                docUniq=record.getUniq();

             */
            Node itemClickedOn = (Node) event.getTarget();
            /*
            HBox boxClickedOn = (HBox) itemClickedOn.getParent();
            OperationsModuleRecord item = (OperationsModuleRecord) boxClickedOn.getUserData();
            if (item != null) {
                docNumber = item.getId();
                docUniq = item.getUniq();
            }
             */
            while (itemClickedOn != null && !(itemClickedOn instanceof HBox)) {
                itemClickedOn = itemClickedOn.getParent();
            }

            if (itemClickedOn instanceof HBox) {
                HBox boxClickedOn = (HBox) itemClickedOn;

                // Retrieve the user data from the HBox
                OperationsModuleRecord item = (OperationsModuleRecord) boxClickedOn.getUserData();
                if (item != null) {
                    docNumber = item.getId();
                    docUniq = item.getUniq();

                }
            }
        }
        exitSearch(null);

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {



    }

    public Pair<Long,String> getResult() {
        return new Pair<>(docUniq,docNumber);

    }
}
