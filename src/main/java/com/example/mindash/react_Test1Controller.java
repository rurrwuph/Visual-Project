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



public class Test1Controller  implements Initializable {
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

    private static final String RESOURCE_NAME = "bestTime1.txt"; // placed under src/main/resources
    private static final Path USER_SAVE = Paths.get(System.getProperty("user.home"), ".reaction-time", RESOURCE_NAME);

    @FXML
    public void initialize(URL location, java.util.ResourceBundle resources) {
        loadBestTime();
        initializeGrid();
        startNextTest();
    }

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

    private void startNextTest() {
        if (currentTest >= TOTAL_TESTS) {
            showResults();
            return;
        }

        progressBar.setProgress((double) currentTest / TOTAL_TESTS);
        progressLabel.setText(currentTest + "/" + TOTAL_TESTS);
        statusLabel.setText("Get ready for test " + (currentTest + 1) + " of " + TOTAL_TESTS);

        // Reset all cells to default color
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (node instanceof Rectangle) {
                ((Rectangle) node).setFill(Color.rgb(60, 60, 60));
            }
        }

        // Random delay before showing green box (3-5 seconds)
        int delay = 3000 + random.nextInt(2000);
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

    private void handleGridClick(MouseEvent event) {
        Rectangle clickedRect = (Rectangle) event.getSource();

        if (clickedRect.getFill().equals(Color.LIMEGREEN)) {
            long reactionTime = System.currentTimeMillis() - startTime;
            reactionTimes.add(reactionTime);
            currentTest++;

            clickedRect.setFill(Color.rgb(60, 60, 60));
            statusLabel.setText("Reaction time: " + reactionTime + " ms");

            // Update progress
            progressBar.setProgress((double) currentTest / TOTAL_TESTS);
            progressLabel.setText(currentTest + "/" + TOTAL_TESTS);

            // Brief pause before next test
            PauseTransition pause = new PauseTransition(Duration.millis(1500));
            pause.setOnFinished(e -> startNextTest());
            pause.play();
        }
    }

    private Rectangle getRectangleAt(int col, int row) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return (Rectangle) node;
            }
        }
        return null;
    }

    private void showResults() {
        long total = 0;
        for (long time : reactionTimes) {
            total += time;
        }
        long average = total / TOTAL_TESTS;

        // Compare with best time
        boolean newBest = false;
        if (average < bestTime) {
            newBest = true;
            saveBestTime(average);
        }

        try {
            // Load the result screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("result1.fxml"));
            Parent root = loader.load();
            Result1Controller controller = loader.getController();
            controller.setResults(average, bestTime);

            // Switch to the result screen
            Stage stage = (Stage) gridPane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBestTime() {
        try {
            Long value = null;

            // 1) Prefer user save file if present
            if (Files.exists(USER_SAVE)) {
                String content = Files.readString(USER_SAVE, StandardCharsets.UTF_8).trim();
                if (!content.isEmpty()) value = Long.parseLong(content);
            } else {
                // 2) Fall back to bundled resource on the classpath
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

    private void saveBestTime(long time) {
        try {
            // Save to a user-writable location
            Files.createDirectories(USER_SAVE.getParent());
            Files.writeString(USER_SAVE, String.valueOf(time), StandardCharsets.UTF_8);
            bestTime = time;
            bestTimeLabel.setText("Best time: " + bestTime + " ms");



            try {
                URL url = getClass().getResource("/" + RESOURCE_NAME);
                if (url != null && "file".equalsIgnoreCase(url.getProtocol())) {
                    Path resPath = Paths.get(url.toURI());
                    // resPath typically points to target/classes/bestTime1.txt during IDE runs
                    Files.writeString(resPath, String.valueOf(time), StandardCharsets.UTF_8);
                }
            } catch (URISyntaxException ignore) {
                // Ignore if we can't resolve a writable resource path (e.g., running from a JAR)
            }
            // --- end optional ---

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
