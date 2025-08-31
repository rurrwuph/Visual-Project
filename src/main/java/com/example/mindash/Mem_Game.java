package com.example.mindash;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class Mem_Game {

    @FXML private Button patternRecallBtn;
    @FXML private Button associationMatchBtn;
    @FXML private Button timelineScrollBtn;
    @FXML private Label  narrativeLabel;
    @FXML private Button BackButton;


    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    public void initialize() {
        narrativeLabel.setWrapText(true);
//        narrativeLabel.setTextAlignment(center);
    }

    private void switchScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            switch (fxmlFile) {
                case "PatternRecall.fxml" -> {
//                    Mem_PatternRecallController controller = loader.getController();
//                    controller.setUsername(username);
                }
                case "AssociationMatch.fxml" -> {
//                    Mem_AssociationMatchController controller = loader.getController();
//                    controller.setUsername(username);
                }
                case "VerbalMemory.fxml" -> {
//                    TimelineScrollController controller = loader.getController();
//                    controller.setUsername(username);
                }

            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void launchPatternRecall(ActionEvent event) {
        switchScene(event, "PatternRecall.fxml");
    }

    @FXML
    private void launchAssociationMatch(ActionEvent event) {
        switchScene(event, "AssociationMatch.fxml");
    }

    @FXML
    private void launchTimelineScroll(ActionEvent event) {
        switchScene(event, "VerbalMemory.fxml");
    }

    @FXML
    private void goBack(ActionEvent event) { switchScene(event, "Main_menu.fxml"); }

}

