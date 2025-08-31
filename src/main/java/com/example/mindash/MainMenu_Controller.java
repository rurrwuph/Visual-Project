package com.example.mindash;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private Label descriptionLabel; // Reference to the description label
    @FXML
    private Label nameLabel; // Reference to the name label
    @FXML
    private ImageView imageView; // Reference to the ImageView
    @FXML
    private AnchorPane parentPane; // Reference to the parent pane for dynamic placement

    @FXML
    public void initialize() {
        // Set up the event handlers for the buttons
        typingButton.setOnMouseEntered(e -> showTypingContent());
        memoryButton.setOnMouseEntered(e -> showMemoryContent());
        reactionButton.setOnMouseEntered(e -> showReactionContent());

        // Hide description when mouse exits the button area
        typingButton.setOnMouseExited(e -> hideDescription());
        memoryButton.setOnMouseExited(e -> hideDescription());
        reactionButton.setOnMouseExited(e -> hideDescription());
    }

    // Show content dynamically when hovering over the typing button
    private void showTypingContent() {
        nameLabel.setText("Typing Challenge");
        descriptionLabel.setText("Test your typing skills. Challenge your speed and accuracy!");
        imageView.setImage(new Image(getClass().getResourceAsStream("keyboard_switch.png")));
        descriptionLabel.setVisible(true); // Make the label visible
    }

    // Show content dynamically when hovering over the memory button
    private void showMemoryContent() {
        nameLabel.setText("Memory Challenge");
        descriptionLabel.setText("Sharpen your memory with pattern recognition challenges!");
        imageView.setImage(new Image(getClass().getResourceAsStream("love-always-wins.png")));
        descriptionLabel.setVisible(true); // Make the label visible
    }

    // Show content dynamically when hovering over the reaction button
    private void showReactionContent() {
        nameLabel.setText("Reaction Challenge");
        descriptionLabel.setText("Test your reaction time with quick-response games!");
        imageView.setImage(new Image(getClass().getResourceAsStream("lightning.png")));
        descriptionLabel.setVisible(true); // Make the label visible
    }

    // Hide description when mouse exits the button area
    private void hideDescription() {
        descriptionLabel.setVisible(false); // Hide the label when mouse exits
    }

    // Launch corresponding game when button is clicked
    @FXML
    private void launchTyping(ActionEvent event) {
        switchScene("typing-checker.fxml", event);
    }

    @FXML
    private void launchMemory(ActionEvent event) {
        switchScene("MemGame.fxml", event);
    }

    @FXML
    private void launchReaction(ActionEvent event) {
        switchScene("ReactionGame.fxml", event);
    }

    // Helper method to switch scenes
    private void switchScene(String fxmlFile, ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));

            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
