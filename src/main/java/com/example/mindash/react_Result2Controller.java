package com.example.mindash;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class Result2Controller {
    @FXML
    private Label currentTimeLabel;
    @FXML private Label bestTimeLabel;
    @FXML private Label comparisonLabel;

    private long currentTime;
    private long bestTime;

    public void setResults(long currentTime, long bestTime) {
        this.setCurrentTime(currentTime);
        this.setBestTime(bestTime);

        currentTimeLabel.setText("Your time: " + currentTime + " ms");
        bestTimeLabel.setText("Best time: " + bestTime + " ms");

        if (currentTime < bestTime) {
            comparisonLabel.setText("Congratulations! You improved by " + (bestTime - currentTime) + " ms!");
        } else if (currentTime == bestTime) {
            comparisonLabel.setText("You matched your best time!");
        } else {
            comparisonLabel.setText("You were " + (currentTime - bestTime) + " ms slower than your best.");
        }
    }

    @FXML
    private void playAgain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("test2.fxml"));
            Stage stage = (Stage) currentTimeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void backToMenu() {
        Stage stage = (Stage) currentTimeLabel.getScene().getWindow();
        stage.close();
    }

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
