package com.drillup.drillup;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class SearchController {
    @FXML
    private ListView<?> resultsBox;

    @FXML
    private Label searchLabel1;

    @FXML
    private Label searchLabel2;

    @FXML
    private Label searchLabel3;

    @FXML
    private TextField searchParam1;

    @FXML
    private TextField searchParam2;

    @FXML
    private TextField searchParam3;

    Database db;

    public void setDb(Database db) {
        this.db = db;
    }

    @FXML
    void exitSearch(ActionEvent event) {

    }

    @FXML
    void itemSelected(MouseEvent event) {

    }

    @FXML
    void selectEntry(ActionEvent event) {

    }

    @FXML
    void initialize() {

    }

}
