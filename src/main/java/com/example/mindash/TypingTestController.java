package com.example.mindash;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TypingTestController {

    // FXML elements injected from Scene Builder
    @FXML
    private TextFlow promptTextFlow;
    @FXML
    private TextArea inputTextArea;
    @FXML
    private Label resultLabel;
    @FXML
    private Button startStopButton;
    @FXML
    private Label timeLabel; // The main typing timer label
    @FXML
    private Spinner<Integer> testDurationSpinner;
    @FXML
    private Button openCustomTextWindowButton;
    @FXML
    private Label countdownLabel; // The 3-2-1 countdown label
    @FXML
    private Button pauseResumeButton;

    // Game state variables
    private String textToType;
    private String customText;
    private long startTime;
    private boolean isTestRunning;
    private boolean isPaused;
    private ScheduledExecutorService timerService;
    private ScheduledExecutorService countdownTimerService;
    private int totalTestTimeSeconds;
    private int timeRemainingSeconds;
    private int countdownSeconds;

    // Cumulative tracking for WPM (correct only) and Accuracy (all typed)
    private int totalCorrectCharactersCount;
    private int totalTypedCharactersCount;
    private int totalErrorCount;
    private int totalBackspaceCount;

    // Data for Consistency Graph (WPM over time)
    private List<Double> wpmHistory;
    private List<Integer> timeHistory;

    // Variables to store highest scores and file path for persistence
    private double highestWPM = 0;
    private double highestAccuracy = 0;
    private static final String HIGH_SCORES_FILE = "highscores.txt";

    // Sample texts for the typing test
    private final List<String> sampleTexts = Arrays.asList(
            "The quick brown fox jumps over the lazy dog.",
            "JavaFX is a software platform for creating and delivering desktop applications.",
            "Programming is thinking, not typing.",
            "The early bird catches the worm.",
            "Technology is best when it brings people together.",
            "Innovation distinguishes between a leader and a follower.",
            "The sun always shines brightest after the rain.",
            "Learning a new skill takes time and consistent effort."
    );

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets up spinner, loads high scores, resets test state, and adds listeners.
     */
    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 300, 60);
        testDurationSpinner.setValueFactory(valueFactory);
        totalTestTimeSeconds = testDurationSpinner.getValue();

        testDurationSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            totalTestTimeSeconds = newValue;
            if (!isTestRunning) {
                timeLabel.setText("Time: " + totalTestTimeSeconds + "s");
            }
        });

        loadHighScores();
        resetTest(); // Sets initial visibility for countdownLabel and timeLabel

        inputTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isTestRunning && !isPaused) {
                if (newValue.length() < oldValue.length()) {
                    totalBackspaceCount++;
                }
                updateResults();
            }
        });
        inputTextArea.caretPositionProperty().addListener((obs, oldValue, newValue) -> {
            if (isTestRunning && !isPaused) {
                applyWordHighlighting(newValue.intValue());
            }
        });

        Platform.runLater(() -> inputTextArea.requestFocus());

        addSpacebarDisabler(startStopButton);
        addSpacebarDisabler(openCustomTextWindowButton);
        addSpacebarDisabler(pauseResumeButton);
    }

    private void addSpacebarDisabler(Button button) {
        button.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                event.consume();
            }
        });
    }

    @FXML
    private void startStopButtonAction() {
        String buttonText = startStopButton.getText();
        if ("Start".equals(buttonText)) {
            startCountdown();
        } else if ("Reset".equals(buttonText) || "Cancel".equals(buttonText)) {
            resetTest();
        }
    }

    @FXML
    private void pauseResumeAction() {
        if (isTestRunning) {
            if (isPaused) {
                resumeTest();
            } else {
                pauseTest();
            }
        }
    }

    /**
     * Initiates a 3-second visual countdown before the main typing test starts.
     * Disables controls during countdown and displays the countdown label.
     */
    private void startCountdown() {
        // Disable relevant UI controls during the countdown
        startStopButton.setText("Cancel"); // Allow the button to be clicked to cancel
        openCustomTextWindowButton.setDisable(true);
        testDurationSpinner.setDisable(true);
        inputTextArea.setDisable(true);
        pauseResumeButton.setVisible(false);
        pauseResumeButton.setManaged(false); // Ensure pause button is not in layout during countdown

        timeLabel.setVisible(false); // HIDE the typing timer
        timeLabel.setManaged(false); // Remove typing timer from layout

        countdownLabel.setVisible(true); // SHOW the countdown label
        countdownLabel.setManaged(true); // Make countdown label participate in layout
        countdownSeconds = 4;
        countdownLabel.setText(String.valueOf(countdownSeconds));

        countdownTimerService = Executors.newSingleThreadScheduledExecutor();
        countdownTimerService.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            countdownSeconds--;
            if (countdownSeconds > 0) {
                countdownLabel.setText(String.valueOf(countdownSeconds));
            } else {
                countdownLabel.setText("GO!");
                countdownTimerService.shutdownNow();
                startActualTest();
            }
        }), 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Contains the core logic to start the typing test once the countdown is finished.
     * Initializes test state, sets up timer, and prepares UI for typing.
     */
    private void startActualTest() {
        isTestRunning = true;
        isPaused = false;
        inputTextArea.setDisable(false);
        inputTextArea.clear();
        resultLabel.setText("WPM: 0 | Accuracy: 0%");
        startStopButton.setText("Reset");

        countdownLabel.setVisible(false); // HIDE the countdown label
        countdownLabel.setManaged(false); // Remove countdown label from layout

        timeLabel.setVisible(true); // SHOW the typing timer
        timeLabel.setManaged(true); // Make typing timer participate in layout

        pauseResumeButton.setText("Pause");
        pauseResumeButton.setDisable(false);
        pauseResumeButton.setVisible(true);
        pauseResumeButton.setManaged(true); // Make pause button participate in layout

        totalCorrectCharactersCount = 0;
        totalTypedCharactersCount = 0;
        totalErrorCount = 0;
        totalBackspaceCount = 0;

        wpmHistory = new ArrayList<>();
        timeHistory = new ArrayList<>();

        if (customText != null && !customText.isEmpty()) {
            textToType = customText;
        } else {
            generateNewSampleText();
        }
        initializePromptTextFlow(textToType);
        applyWordHighlighting(0);

        startTime = System.currentTimeMillis();
        timeRemainingSeconds = totalTestTimeSeconds;
        timeLabel.setText("Time: " + timeRemainingSeconds + "s");

        timerService = Executors.newSingleThreadScheduledExecutor();
        timerService.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            if (isTestRunning && !isPaused) {
                timeRemainingSeconds--;
                timeLabel.setText("Time: " + timeRemainingSeconds + "s");

                long elapsedTime = System.currentTimeMillis() - startTime;
                double minutes = elapsedTime / 60000.0;

                int currentTyped = totalTypedCharactersCount + inputTextArea.getText().length();
                int currentCorrect = totalCorrectCharactersCount + getCurrentSegmentCorrectChars();

                double currentWpmForHistory = (minutes > 0 && currentCorrect > 0) ? (currentCorrect / 5.0) / minutes : 0;

                wpmHistory.add(currentWpmForHistory);
                timeHistory.add(totalTestTimeSeconds - timeRemainingSeconds);

                updateResults();

                if (timeRemainingSeconds <= 0) {
                    finishTest();
                }
            }
        }), 0, 1, TimeUnit.SECONDS);

        inputTextArea.requestFocus();
    }

    private int getCurrentSegmentCorrectChars() {
        String typedText = inputTextArea.getText();
        int currentSegmentCorrectChars = 0;
        for (int i = 0; i < typedText.length() && i < textToType.length(); i++) {
            if (typedText.charAt(i) == textToType.charAt(i)) {
                currentSegmentCorrectChars++;
            }
        }
        return currentSegmentCorrectChars;
    }

    private void pauseTest() {
        isPaused = true;
        pauseResumeButton.setText("Resume");
        inputTextArea.setDisable(true);
        timeLabel.setText("Paused");
    }

    private void resumeTest() {
        isPaused = false;
        pauseResumeButton.setText("Pause");
        inputTextArea.setDisable(false);
        startTime = System.currentTimeMillis() - (totalTestTimeSeconds - timeRemainingSeconds) * 1000;
        timeLabel.setText("Time: " + timeRemainingSeconds + "s");
        inputTextArea.requestFocus();
    }

    private void generateNewSampleText() {
        Random random = new Random();
        String newText = sampleTexts.get(random.nextInt(sampleTexts.size()));
        while (sampleTexts.size() > 1 && newText.equals(textToType)) {
            newText = sampleTexts.get(random.nextInt(sampleTexts.size()));
        }
        textToType = newText;
    }

    private void initializePromptTextFlow(String text) {
        promptTextFlow.getChildren().clear();
        for (char c : text.toCharArray()) {
            Text charText = new Text(String.valueOf(c));
            charText.setFill(Color.BLUE);
            promptTextFlow.getChildren().add(charText);
        }
    }

    private void resetDefaultPromptText() {
        promptTextFlow.getChildren().clear();
        Text initialText = new Text("Click Start or 'Enter Custom Text' to begin.");
        initialText.setFill(Color.WHITE);
        promptTextFlow.getChildren().add(initialText);
    }

    /**
     * Resets the entire typing test application to its initial idle state.
     * Clears data, disables controls, and stops timers.
     */
    private void resetTest() {
        isTestRunning = false;
        isPaused = false;
        inputTextArea.setDisable(true);
        inputTextArea.clear();

        resetDefaultPromptText();

        resultLabel.setText("WPM: 0 | Accuracy: 0%");
        startStopButton.setText("Start");
        startStopButton.setDisable(false);

        testDurationSpinner.setDisable(false);
        openCustomTextWindowButton.setDisable(false);
        pauseResumeButton.setVisible(false);
        pauseResumeButton.setManaged(false); // Ensure pause button is not in layout

        countdownLabel.setVisible(false);
        countdownLabel.setManaged(false); // HIDE countdownLabel and remove from layout

        timeLabel.setVisible(true); // SHOW typing timer (initial state)
        timeLabel.setManaged(true); // Make typing timer participate in layout

        customText = null;

        timeLabel.setText("Time: " + totalTestTimeSeconds + "s");

        if (timerService != null && !timerService.isShutdown()) {
            timerService.shutdownNow();
            timerService = null;
        }
        if (countdownTimerService != null && !countdownTimerService.isShutdown()) {
            countdownTimerService.shutdownNow();
            countdownTimerService = null;
        }

        textToType = "";
        totalCorrectCharactersCount = 0;
        totalTypedCharactersCount = 0;
        totalErrorCount = 0;
        totalBackspaceCount = 0;

        if (wpmHistory != null) wpmHistory.clear();
        if (timeHistory != null) timeHistory.clear();
    }

    private void applyWordHighlighting(int caretPosition) {
        for (int i = 0; i < promptTextFlow.getChildren().size(); i++) {
            Text charText = (Text) promptTextFlow.getChildren().get(i);
            charText.getStyleClass().remove("current-word-highlight");
        }

        if (!textToType.isEmpty() && caretPosition >= 0 && caretPosition <= textToType.length()) {
            int wordStartIndex = caretPosition;
            while (wordStartIndex > 0 && !Character.isWhitespace(textToType.charAt(wordStartIndex - 1))) {
                wordStartIndex--;
            }

            int wordEndIndex = caretPosition;
            while (wordEndIndex < textToType.length() && !Character.isWhitespace(textToType.charAt(wordEndIndex))) {
                wordEndIndex++;
            }

            for (int i = wordStartIndex; i < wordEndIndex; i++) {
                if (i < promptTextFlow.getChildren().size()) {
                    Text charText = (Text) promptTextFlow.getChildren().get(i);
                    charText.getStyleClass().add("current-word-highlight");
                }
            }
        }
    }

    private void updateResults() {
        Platform.runLater(() -> {
            if (!isTestRunning || isPaused) {
                return;
            }

            String typedText = inputTextArea.getText();
            int currentSegmentAttemptedChars = typedText.length();
            int currentSegmentCorrectChars = 0;
            int currentSegmentErrorChars = 0;

            for (int i = 0; i < textToType.length(); i++) {
                Text charText = (Text) promptTextFlow.getChildren().get(i);
                charText.getStyleClass().remove("current-word-highlight");

                if (i < currentSegmentAttemptedChars) {
                    if (typedText.charAt(i) == textToType.charAt(i)) {
                        charText.setFill(Color.GREEN);
                        currentSegmentCorrectChars++;
                    } else {
                        charText.setFill(Color.RED);
                        currentSegmentErrorChars++;
                    }
                } else {
                    charText.setFill(Color.BLUE);
                }
            }
            applyWordHighlighting(inputTextArea.getCaretPosition());

            if (customText == null) {
                if (currentSegmentAttemptedChars >= textToType.length() && textToType.length() > 0) {
                    totalTypedCharactersCount += currentSegmentAttemptedChars;
                    totalCorrectCharactersCount += currentSegmentCorrectChars;
                    totalErrorCount += currentSegmentErrorChars;

                    generateNewSampleText();
                    initializePromptTextFlow(textToType);
                    inputTextArea.clear();
                    currentSegmentAttemptedChars = 0;
                    currentSegmentCorrectChars = 0;
                    currentSegmentErrorChars = 0;
                }
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            double minutes = elapsedTime / 60000.0;

            int cumulativeAttemptedChars = totalTypedCharactersCount + currentSegmentAttemptedChars;
            int cumulativeCorrectChars = totalCorrectCharactersCount + currentSegmentCorrectChars;

            double wpm = (minutes > 0 && cumulativeCorrectChars > 0) ? (cumulativeCorrectChars / 5.0) / minutes : 0;

            double accuracy = (cumulativeAttemptedChars > 0) ? ((double) cumulativeCorrectChars / cumulativeAttemptedChars) * 100 : 0;

            resultLabel.setText(String.format("WPM: %.0f | Accuracy: %.1f%%", wpm, accuracy));
        });
    }

    private void finishTest() {
        isTestRunning = false;
        isPaused = false;
        inputTextArea.setDisable(true);
        startStopButton.setText("Start Again");

        testDurationSpinner.setDisable(false);
        openCustomTextWindowButton.setDisable(false);
        pauseResumeButton.setVisible(false);
        pauseResumeButton.setManaged(false); // Ensure pause button is not in layout

        if (timerService != null && !timerService.isShutdown()) {
            timerService.shutdownNow();
            timerService = null;
        }
        if (countdownTimerService != null && !countdownTimerService.isShutdown()) {
            countdownTimerService.shutdownNow();
            countdownTimerService = null;
        }

        String typedText = inputTextArea.getText();
        int typedCharsInLastSegment = typedText.length();
        int correctCharsInLastSegment = 0;
        int errorCharsInLastSegment = 0;

        for (int i = 0; i < typedCharsInLastSegment && i < textToType.length(); i++) {
            if (typedText.charAt(i) == textToType.charAt(i)) {
                correctCharsInLastSegment++;
            } else {
                errorCharsInLastSegment++;
            }
        }

        totalTypedCharactersCount += typedCharsInLastSegment;
        totalCorrectCharactersCount += correctCharsInLastSegment;
        totalErrorCount += errorCharsInLastSegment;

        long elapsedTime = totalTestTimeSeconds * 1000;
        double minutes = elapsedTime / 60000.0;

        double finalWpm = (minutes > 0 && totalCorrectCharactersCount > 0) ?
                (totalCorrectCharactersCount / 5.0) / minutes : 0;

        double finalAccuracy = (totalTypedCharactersCount > 0) ?
                ((double) totalCorrectCharactersCount / totalTypedCharactersCount) * 100 : 0;

        if (finalWpm > highestWPM) {
            highestWPM = finalWpm;
            saveHighScores();
        }
        if (finalAccuracy > highestAccuracy) {
            highestAccuracy = finalAccuracy;
            saveHighScores();
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("typing-results-view.fxml"));
            Stage resultsStage = new Stage();
            resultsStage.setTitle("Typing Test Results");
            Scene scene = new Scene(fxmlLoader.load());

            URL cssUrl = getClass().getResource("styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Error: 'styles.css' not found for results view. Make sure it's in src/main/resources/com/example/mindash/");
            }
            resultsStage.setScene(scene);
            resultsStage.initModality(Modality.APPLICATION_MODAL);
            resultsStage.setResizable(false);

            TypingResultsController typingResultsController = fxmlLoader.getController();
            typingResultsController.setResults(finalWpm, finalAccuracy, highestWPM, highestAccuracy,
                    totalErrorCount, totalBackspaceCount,
                    wpmHistory, timeHistory);

            resultsStage.showAndWait();
            resetTest();

        } catch (IOException e) {
            System.err.println("Failed to load results-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void shutdownTimerService() {
        if (timerService != null && !timerService.isShutdown()) {
            timerService.shutdownNow();
            System.out.println("Timer service (main) shut down.");
        }
        if (countdownTimerService != null && !countdownTimerService.isShutdown()) {
            countdownTimerService.shutdownNow();
            System.out.println("Timer service (countdown) shut down.");
        }
    }

    private void loadHighScores() {
        Path filePath = Paths.get(HIGH_SCORES_FILE);
        if (Files.exists(filePath)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                String wpmLine = reader.readLine();
                String accuracyLine = reader.readLine();

                if (wpmLine != null && accuracyLine != null) {
                    highestWPM = Double.parseDouble(wpmLine);
                    highestAccuracy = Double.parseDouble(accuracyLine);
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error loading high scores: " + e.getMessage());
                highestWPM = 0;
                highestAccuracy = 0;
            }
        }
    }

    private void saveHighScores() {
        try (FileWriter writer = new FileWriter(HIGH_SCORES_FILE)) {
            writer.write(String.format("%.2f\n", highestWPM));
            writer.write(String.format("%.2f\n", highestAccuracy));
        } catch (IOException e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }


    /**
     * Handles the action to open the custom text input window.
     */
    @FXML
    private void openCustomTextWindowAction() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("typing-custom-text-view.fxml"));
            Stage customTextStage = new Stage();
            customTextStage.setTitle("Enter Custom Text");
            Scene scene = new Scene(fxmlLoader.load());

            // Link CSS to the custom text window as well
            URL cssUrl = getClass().getResource("styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Error: 'styles.css' not found for custom text view. Make sure it's in src/main/resources/com/example/mindash/");
            }

            customTextStage.setScene(scene);
            customTextStage.initModality(Modality.APPLICATION_MODAL); // Make it a modal window
            customTextStage.setResizable(false);

            TypingCustomTextController typingCustomTextController = fxmlLoader.getController();
            typingCustomTextController.setExistingText(customText); // Pass existing custom text to pre-fill

            customTextStage.showAndWait(); // Show the window and wait for it to be closed

            // After the window is closed, retrieve the custom text
            String newCustomText = typingCustomTextController.getCustomText();
            if (newCustomText != null && !newCustomText.isEmpty()) {
                customText = newCustomText;
                promptTextFlow.getChildren().clear();
                Text textIndication = new Text("Custom text loaded. Click Start to begin!");
                textIndication.setFill(Color.DARKGREEN);
                promptTextFlow.getChildren().add(textIndication);
                startStopButton.setDisable(false); // Enable start button if custom text is set
            } else {
                // If user didn't apply text or cleared it, revert to default message
                if (customText != null && customText.isEmpty()) { // Check if it was explicitly cleared
                    resetDefaultPromptText();
                    customText = null; // Clear customText if it was reset
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load custom-text-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
