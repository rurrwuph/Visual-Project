package com.example.mindash;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class react_Test1Controller implements Initializable {

    // Initial setup and UI bindings for the test screen
    @FXML
    private GridPane gridPane;
    @FXML private Label statusLabel;
    @FXML private Label progressLabel;
    @FXML private Label bestTimeLabel;
    @FXML private ProgressBar progressBar;

    private final int GRID_SIZE = 12;
    private final int TOTAL_TESTS = 5;
    private int currentTest = 0;
    private long startTime;
    private List<Long> reactionTimes = new ArrayList<>();
    private Random random = new Random();
    private long bestTime = Long.MAX_VALUE;

    private static final String RESOURCE_NAME = "react_bestTime1.txt";
    private static final Path USER_SAVE = Paths.get(System.getProperty("user.home"), ".reaction-time", RESOURCE_NAME);

    // Initialize the controller, load best time, and start the test sequence
    @FXML
    public void initialize(URL location, java.util.ResourceBundle resources) {
        loadBestTime();
        initializeGrid();
        startNextTest();
    }

    // Creates and populates the 12x12 grid with rectangles that the user clicks
    private void initializeGrid() {
        gridPane.getChildren().clear();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Rectangle rect = new Rectangle(35, 35);
                rect.setFill(Color.rgb(60, 60, 60));
                rect.setStroke(Color.rgb(40, 40, 40));
                rect.setArcWidth(10);
                rect.setArcHeight(10);
                rect.setOnMouseClicked(this::handleGridClick);
                GridPane.setMargin(rect, new Insets(2));
                gridPane.add(rect, col, row);
            }
        }
    }

    // Starts the next reaction test, showing a random green box to click
    private void startNextTest() {
        if (currentTest >= TOTAL_TESTS) {
            showResults();
            return;
        }

        progressBar.setProgress((double) currentTest / TOTAL_TESTS);
        progressLabel.setText(currentTest + "/" + TOTAL_TESTS);
        statusLabel.setText("Get ready for test " + (currentTest + 1) + " of " + TOTAL_TESTS);

        // Reset grid color before showing the green box
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (node instanceof Rectangle) {
                ((Rectangle) node).setFill(Color.rgb(60, 60, 60));
            }
        }

        // Set a random delay before displaying the green box
        int delay = 1500 + random.nextInt(2000);
        PauseTransition pause = new PauseTransition(Duration.millis(delay));
        pause.setOnFinished(e -> {
            int targetRow = random.nextInt(GRID_SIZE);
            int targetCol = random.nextInt(GRID_SIZE);

            Rectangle rect = getRectangleAt(targetCol, targetRow);
            if (rect != null) {
                rect.setFill(Color.LIMEGREEN);
                startTime = System.currentTimeMillis();
                statusLabel.setText("Click the green box now!");
            }
        });
        pause.play();
    }

    // Handles grid cell clicks, checking if the green box was clicked and calculating reaction time
    private void handleGridClick(MouseEvent event) {
        Rectangle clickedRect = (Rectangle) event.getSource();

        if (clickedRect.getFill().equals(Color.LIMEGREEN)) {
            long reactionTime = System.currentTimeMillis() - startTime;
            reactionTimes.add(reactionTime);
            currentTest++;

            clickedRect.setFill(Color.rgb(60, 60, 60));
            statusLabel.setText("Reaction time: " + reactionTime + " ms");

            // Update progress bar and label
            progressBar.setProgress((double) currentTest / TOTAL_TESTS);
            progressLabel.setText(currentTest + "/" + TOTAL_TESTS);

            // Brief pause before starting next test
            PauseTransition pause = new PauseTransition(Duration.millis(1500));
            pause.setOnFinished(e -> startNextTest());
            pause.play();
        }
    }

    // Finds the rectangle at a given grid position
    private Rectangle getRectangleAt(int col, int row) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return (Rectangle) node;
            }
        }
        return null;
    }

    // Displays the results at the end of all tests
    private void showResults() {
        long total = 0;
        for (long time : reactionTimes) {
            total += time;
        }
        long average = total / TOTAL_TESTS;

        // Check if there's a new best time
        boolean newBest = false;
        if (average < bestTime) {
            newBest = true;
            saveBestTime(average);
        }

        try {
            // Load and display the results screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("react_result1.fxml"));
            Parent root = loader.load();
            react_Result1Controller controller = loader.getController();
            controller.setResults(average, bestTime);

            // Switch to the results screen
            Stage stage = (Stage) gridPane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loads the best time from the saved file or bundled resource
    private void loadBestTime() {
        try {
            Long value = null;

            if (Files.exists(USER_SAVE)) {
                String content = Files.readString(USER_SAVE, StandardCharsets.UTF_8).trim();
                if (!content.isEmpty()) value = Long.parseLong(content);
            } else {
                try (InputStream in = getClass().getResourceAsStream("/" + RESOURCE_NAME)) {
                    if (in != null) {
                        String content = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
                        if (!content.isEmpty()) value = Long.parseLong(content);
                    }
                }
            }

            if (value != null) {
                bestTime = value;
                bestTimeLabel.setText("Best time: " + bestTime + " ms");
            } else {
                bestTime = Long.MAX_VALUE;
                bestTimeLabel.setText("Best time: -- ms");
            }
        } catch (IOException | NumberFormatException e) {
            bestTime = Long.MAX_VALUE;
            bestTimeLabel.setText("Best time: -- ms");
        }
    }

    // Saves the best time to a user-writable location and updates UI
    private void saveBestTime(long time) {
        try {
            Files.createDirectories(USER_SAVE.getParent());
            Files.writeString(USER_SAVE, String.valueOf(time), StandardCharsets.UTF_8);
            bestTime = time;
            bestTimeLabel.setText("Best time: " + bestTime + " ms");

            try {
                URL url = getClass().getResource("/" + RESOURCE_NAME);
                if (url != null && "file".equalsIgnoreCase(url.getProtocol())) {
                    Path resPath = Paths.get(url.toURI());
                    Files.writeString(resPath, String.valueOf(time), StandardCharsets.UTF_8);
                }
            } catch (URISyntaxException ignore) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
