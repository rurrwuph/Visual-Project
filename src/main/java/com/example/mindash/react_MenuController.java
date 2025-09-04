package com.example.mindash;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class react_MenuController {
    //grid reaction
    @FXML
    private void startTest1(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("react_test1.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Test 1: Grid Reaction");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //falling balls
    @FXML
    private void startTest2(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("react_test2.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Test 2: Falling Balls");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
