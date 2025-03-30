package com.drillup.drillup;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
        stage.getIcons().add(new Image(getClass().getResource("Icon.png").toString()));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}