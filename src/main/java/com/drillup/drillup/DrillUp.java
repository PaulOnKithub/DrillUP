package com.drillup.drillup;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DrillUp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("drillUp.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Drill Up");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.getIcons().add(new javafx.scene.image.Image("file:src/main/resources/com/mycrm/mycrm/search Icon.png"));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}