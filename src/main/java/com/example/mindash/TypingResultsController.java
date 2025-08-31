package com.example.mindash;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.List;

public class TypingResultsController {

    @FXML
    private Label finalWpmLabel;
    @FXML
    private Label highestWpmLabel;
    @FXML
    private ProgressBar wpmProgressBar;

    @FXML
    private Label finalAccuracyLabel;
    @FXML
    private Label highestAccuracyLabel;
    @FXML
    private ProgressBar accuracyProgressBar;

    @FXML
    private Label finalErrorCountLabel;
    @FXML
    private Label finalBackspaceCountLabel;

    @FXML
    private LineChart<Number, Number> wpmConsistencyChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    @FXML
    private Button closeButton;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Used here to attach the spacebar disabler to the close button.
     */
    @FXML
    public void initialize() {
        // NEW: Disable spacebar for the close button
        addSpacebarDisabler(closeButton);
    }

    // NEW: Helper method to disable spacebar for button clicks
    private void addSpacebarDisabler(Button button) {
        button.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                event.consume(); // Consume the event so it doesn't trigger button's onAction
            }
        });
    }

    /**
     * Sets the final WPM and Accuracy to be displayed on the results screen,
     * along with the highest recorded scores for comparison. It also updates
     * the progress bars and the WPM consistency graph.
     *
     * @param currentWpm The calculated Words Per Minute for the current test.
     * @param currentAccuracy The calculated accuracy percentage for the current test.
     * @param highestWpm The overall highest WPM ever recorded.
     * @param highestAccuracy The overall highest accuracy ever recorded.
     * @param finalErrorCount The total count of errors made in the test.
     * @param finalBackspaceCount The total count of backspaces used in the test.
     * @param wpmHistory A list of WPM values recorded over the test duration.
     * @param timeHistory A list of time points (seconds elapsed) corresponding to wpmHistory.
     */
    public void setResults(double currentWpm, double currentAccuracy, double highestWpm, double highestAccuracy,
                           int finalErrorCount, int finalBackspaceCount,
                           List<Double> wpmHistory, List<Integer> timeHistory) {
        // Display current test results
        finalWpmLabel.setText(String.format("Your WPM: %.0f", currentWpm));
        finalAccuracyLabel.setText(String.format("Your Accuracy: %.1f%%", currentAccuracy));

        // Display highest scores
        highestWpmLabel.setText(String.format("Highest WPM: %.0f", highestWpm));
        highestAccuracyLabel.setText(String.format("Highest Accuracy: %.1f%%", highestAccuracy));

        // Display enhanced statistics
        finalErrorCountLabel.setText("Errors: " + finalErrorCount);
        finalBackspaceCountLabel.setText("Backspaces: " + finalBackspaceCount);

        // Update WPM Progress Bar for comparison
        if (highestWpm > 0) {
            wpmProgressBar.setProgress(Math.min(currentWpm / highestWpm, 1.0));
        } else {
            wpmProgressBar.setProgress(0);
        }

        // Update Accuracy Progress Bar for comparison
        if (highestAccuracy > 0) {
            accuracyProgressBar.setProgress(Math.min(currentAccuracy / highestAccuracy, 1.0));
        } else {
            accuracyProgressBar.setProgress(0);
        }

        // Populate WPM Consistency Chart
        wpmConsistencyChart.getData().clear(); // Clear any old data
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        // Add data points from history
        for (int i = 0; i < wpmHistory.size(); i++) {
            series.getData().add(new XYChart.Data<>(timeHistory.get(i), wpmHistory.get(i)));
        }
        wpmConsistencyChart.getData().add(series);

        // Dynamically adjust Y-axis upper bound if max WPM is very high
        NumberAxis chartYAxis = (NumberAxis) wpmConsistencyChart.getYAxis();
        double maxWpm = wpmHistory.stream().mapToDouble(Double::doubleValue).max().orElse(100.0);
        chartYAxis.setUpperBound(Math.max(maxWpm * 1.2, 100.0));
        chartYAxis.setTickUnit(Math.max(10, (int)(maxWpm / 5)));

        // Ensure X-axis upper bound matches test duration, if not already set by autoRanging=false
        NumberAxis chartXAxis = (NumberAxis) wpmConsistencyChart.getXAxis();
        if (!timeHistory.isEmpty()) {
            chartXAxis.setUpperBound(timeHistory.get(timeHistory.size() - 1));
            chartXAxis.setTickUnit(Math.max(5, (int)(timeHistory.get(timeHistory.size() - 1) / 5)));
        } else {
            chartXAxis.setUpperBound(60);
            chartXAxis.setTickUnit(10);
        }
    }

    /**
     * Handles the action when the "Close" button is clicked.
     * This method closes the current results window (stage).
     */
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) finalWpmLabel.getScene().getWindow();
        stage.close();
    }
}
