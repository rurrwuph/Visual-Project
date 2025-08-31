package com.example.mindash;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainMenu_Controller {
    @FXML
    private Button typingButton;
    @FXML
    private Button memoryButton;
    @FXML
    private Button reactionButton;
    @FXML
    private AnchorPane parentPane; // Reference to the parent pane for dynamic placement

    private Label descriptionLabel;

    @FXML
    public void initialize() {
        // Create and style the description label dynamically
        descriptionLabel = new Label("Hover over a button to see the description");
        descriptionLabel.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.7); -fx-font-size: 16px; -fx-padding: 10;");
        descriptionLabel.setVisible(false); // Initially hidden

        // Set the description label's position to the right side
        AnchorPane.setTopAnchor(descriptionLabel, 100.0); // Adjust the vertical position as needed
        AnchorPane.setRightAnchor(descriptionLabel, 20.0); // Anchoring to the right side
        parentPane.getChildren().add(descriptionLabel); // Add it to the scene

        // Set up the event handlers for the buttons
        typingButton.setOnMouseEntered(e -> showDescription("Test your typing skills. Challenge your speed and accuracy!"));
        memoryButton.setOnMouseEntered(e -> showDescription("Sharpen your memory with pattern recognition challenges!"));
        reactionButton.setOnMouseEntered(e -> showDescription("Test your reaction time with quick-response games!"));

        // Hide description when mouse exits the button area
        typingButton.setOnMouseExited(e -> hideDescription());
        memoryButton.setOnMouseExited(e -> hideDescription());
        reactionButton.setOnMouseExited(e -> hideDescription());
    }

    // Show description dynamically when hovering over a button
    private void showDescription(String description) {
        descriptionLabel.setText(description);
        descriptionLabel.setVisible(true);
    }

    // Hide description when mouse exits the button area
    private void hideDescription() {
        descriptionLabel.setVisible(false);
    }

    // Launch corresponding game when button is clicked
    @FXML
    private void launchTyping(ActionEvent event) {
        switchScene("TypingGame.fxml", event);
    }

    @FXML
    private void launchMemory(ActionEvent event) {
        switchScene("MemoryGame.fxml", event);
    }

    @FXML
    private void launchReaction(ActionEvent event) {
        switchScene("ReactionGame.fxml", event);
    }

    // Helper method to switch scenes
    private void switchScene(String fxmlFile, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
