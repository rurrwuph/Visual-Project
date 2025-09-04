package com.example.mindash;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class react_Result1Controller {
    @FXML
    private Label currentTimeLabel;
    @FXML private Label bestTimeLabel;
    @FXML private Label comparisonLabel;

    private long currentTime;
    private long bestTime;

    // Set the current and best times and update the UI accordingly
    public void setResults(long currentTime, long bestTime) {
        this.setCurrentTime(currentTime);
        this.setBestTime(bestTime);

        // Update the UI labels to show the current and best times
        currentTimeLabel.setText("Your average time: " + currentTime + " ms");
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

    // Restart the game by loading the test screen again
    @FXML
    private void playAgain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("react_test1.fxml"));
            Stage stage = (Stage) currentTimeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));  // Change the scene to the test screen
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Close the current window and return to the menu
    @FXML
    private void backToMenu() {
        Stage stage = (Stage) currentTimeLabel.getScene().getWindow();
        stage.close();  // Close the current stage (game window)
    }

    // Getter and setter methods for the current and best times
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
