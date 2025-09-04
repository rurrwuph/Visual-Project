package com.example.mindash;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class react_Result2Controller {
    // FXML Bindings
    @FXML
    private Label currentTimeLabel;  // Displays the player's current time
    @FXML private Label bestTimeLabel;  // Displays the best time
    @FXML private Label comparisonLabel;  // Displays the comparison message (improvement, match, or slower)

    private long currentTime;  // The player's current reaction time
    private long bestTime;  // The best reaction time (from previous games or sessions)

    // Set Results
    // This method updates the UI with the current time, best time, and comparison message.
    public void setResults(long currentTime, long bestTime) {
        this.setCurrentTime(currentTime);  // Set the current time
        this.setBestTime(bestTime);  // Set the best time

        // Update the labels with the current time and best time
        currentTimeLabel.setText("Your time: " + currentTime + " ms");
        bestTimeLabel.setText("Best time: " + bestTime + " ms");

        // Compare the current time with the best time and update the comparison label
        if (currentTime < bestTime) {
            comparisonLabel.setText("Congratulations! You improved by " + (bestTime - currentTime) + " ms!");
        } else if (currentTime == bestTime) {
            comparisonLabel.setText("You matched your best time!");
        } else {
            comparisonLabel.setText("You were " + (currentTime - bestTime) + " ms slower than your best.");
        }
    }

    // Play Again
    // This method is triggered when the user chooses to play again. It reloads the test screen (react_test2.fxml).
    @FXML
    private void playAgain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("react_test2.fxml"));
            Stage stage = (Stage) currentTimeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));  // Set the scene to the test screen
        } catch (IOException e) {
            e.printStackTrace();  // Handle any loading errors
        }
    }

    // Back to Menu
    // This method closes the current game window and returns the user to the main menu.
    @FXML
    private void backToMenu() {
        Stage stage = (Stage) currentTimeLabel.getScene().getWindow();
        stage.close();  // Close the current game window
    }

    // Getters and Setters
    // Getter and setter methods to access and modify the current time and best time.

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public long getBestTime() {
        return bestTime;
    }

    public void setBestTime(long bestTime) {
        this.bestTime = bestTime;
    }
}
