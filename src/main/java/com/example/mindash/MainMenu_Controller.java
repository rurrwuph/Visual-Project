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

import java.io.IOException;

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
        descriptionLabel.setVisible(true);
        descriptionLabel.setOpacity(1);

        nameLabel.setText("Typing Challenge");
        descriptionLabel.setText("Test and improve your typing speed and accuracy by typing random words and sentences within a set time. Challenge yourself to type faster while reducing errors.");
        descriptionLabel.setWrapText(true);

        imageView.setImage(new Image(getClass().getResourceAsStream("keyboard_switch.png")));
//        descriptionLabel.setVisible(true); // Make the label visible
    }

    // Show content dynamically when hovering over the memory button
    private void showMemoryContent() {
        descriptionLabel.setVisible(true);
        descriptionLabel.setOpacity(1);

        nameLabel.setText("Memory Challenge");
        descriptionLabel.setText("A simple and enjoyable game to strengthen your memory. Watch and remember sequences of words, patterns, match pair of words , providing a rewarding mental workout.");
        descriptionLabel.setWrapText(true);

        imageView.setImage(new Image(getClass().getResourceAsStream("love-always-wins.png")));
//        descriptionLabel.setVisible(true); // Make the label visible
    }

    // Show content dynamically when hovering over the reaction button
    private void showReactionContent() {
        descriptionLabel.setVisible(true);


        nameLabel.setText("Reaction Challenge");
        descriptionLabel.setText("A great way to improve your reaction time and coordination. Respond to on-screen cues at your own pace, helping to rebuild your reflexes and focus during rehabilitation.");
        descriptionLabel.setWrapText(true);
        imageView.setImage(new Image(getClass().getResourceAsStream("lightning.png")));
//        descriptionLabel.setVisible(true); // Make the label visible
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
//        switchScene("MemGame.fxml", event);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MemGame.fxml"));
            Parent root = loader.load();


            Stage stage = (Stage) parentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void launchReaction(ActionEvent event) {
        switchScene("react_menu.fxml", event);
    }

    // Helper method to switch scenes
    private void switchScene(String fxmlFile, ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));

            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            //Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
