package com.example.mindash;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainMenu_Controller {

    @FXML private Button typingButton;
    @FXML private Button memoryButton;
    @FXML private Button reactionButton;
    @FXML private Label descriptionLabel;
    @FXML private Label nameLabel;
    @FXML private ImageView imageView;

    private Button currentActiveButton;

    private final Map<String, GameInfo> gameData = new HashMap<>();

    public void initialize() {
        // Initialize game data
        gameData.put("typing", new GameInfo("Typing Challenge",
                "Test and improve your typing speed and accuracy by typing random words and sentences within a set time. Challenge yourself to type faster while reducing errors.",
                "keyboard_switch.png"));

        gameData.put("memory", new GameInfo("Memory Challenge",
                "A simple and enjoyable game to strengthen your memory. Watch and remember sequences of words, patterns, or match pairs of words, providing a rewarding mental workout.",
                "love-always-wins.png"));

        gameData.put("reaction", new GameInfo("Reaction Challenge",
                "A great way to improve your reaction time and coordination. Respond to on-screen cues at your own pace, helping to rebuild your reflexes and focus during rehabilitation.",
                "lightning.png"));

        // Setup button hover effects, without overwriting onAction
        setupButtonHover(typingButton, "typing");
        setupButtonHover(memoryButton, "memory");
        setupButtonHover(reactionButton, "reaction");

        // Set an initial active button and display its content
        setActiveButton(typingButton);
    }

    private void setupButtonHover(Button button, String gameKey) {
        button.setOnMouseEntered(e -> showGameContent(gameKey));
        // The mouse exited event is not necessary for this UI design
    }

    private void setActiveButton(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active");
        }
        button.getStyleClass().add("active");
        currentActiveButton = button;
        // Also show content for the newly active button
        String gameKey = getGameKeyFromButton(button);
        if (gameKey != null) {
            showGameContent(gameKey);
        }
    }

    private String getGameKeyFromButton(Button button) {
        if (button == typingButton) return "typing";
        if (button == memoryButton) return "memory";
        if (button == reactionButton) return "reaction";
        return null;
    }

    private void showGameContent(String gameKey) {
        GameInfo info = gameData.get(gameKey);
        if (info != null) {
            nameLabel.setText(info.name);
            descriptionLabel.setText(info.description);
            descriptionLabel.setVisible(true);
            descriptionLabel.setWrapText(true);
            try {
                imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(info.imagePath))));
            } catch (Exception e) {
                System.err.println("Failed to load image: " + info.imagePath);
                imageView.setImage(null);
            }
        }
    }

    //
    // Launching the games
    // The onAction from FXML will call these methods.
    //

    @FXML
    private void launchTyping(ActionEvent event) {
        setActiveButton(typingButton);
        switchScene("typing-checker.fxml", event);
    }

    @FXML
    private void launchMemory(ActionEvent event) {
        setActiveButton(memoryButton);
        switchScene("MemGame.fxml", event);
    }

    @FXML
    private void launchReaction(ActionEvent event) {
        setActiveButton(reactionButton);
        switchScene("react_menu.fxml", event);
    }

    // Helper method to switch scenes
    private void switchScene(String fxmlFile, ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = fxmlLoader.load();

            // Get the current Stage from the event source
            Stage stage = new Stage();

            // Create a new Scene with the loaded FXML root
            Scene scene = new Scene(root);
            //scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("MainMenu.css")).toExternalForm());

            // Set the new scene on the current stage
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load FXML file: " + fxmlFile);
            e.printStackTrace();
        }
    }

    private record GameInfo(String name, String description, String imagePath) {}
}