package com.example.mindash;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Main_Menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Set the application icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("icon_.jpeg"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }

        scene.getStylesheets().add(getClass().getResource("MainMenu.css").toExternalForm());
        stage.setTitle("MinDash");
        stage.setScene(scene);
        stage.show();
    }
}
