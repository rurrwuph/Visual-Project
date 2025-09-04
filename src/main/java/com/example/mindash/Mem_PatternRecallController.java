package com.example.mindash;

import javafx.animation.FillTransition;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;

public class Mem_PatternRecallController {

    @FXML private GridPane patternGrid;
    @FXML private Button submitButton;
    @FXML private Label gameOverLabel;
    @FXML private ImageView heart1;
    @FXML private ImageView heart2;
    @FXML private ImageView heart3;
    @FXML private ImageView heart4;
    @FXML private ImageView heart5;
    @FXML private Label roundLabel;
    @FXML private StackPane gameOverlay;

    private final Random rng = new Random();

    private int rows = 2;
    private int cols = 2;
    private final int CellSize = 80;
    private final int maxGridSize = 7;
    private int hearts = 5;
    private int round = 0;
    private boolean gameOver = false;

    private final int perCellMs = 80;
    private final int maxHideTimeMs = 4500;
    private final int baseTimeMs = 1000;

    private List<Rectangle> highlight = new ArrayList<>();
    private Set<String> patternCoords = new HashSet<>();
    private Set<String> selectedCoords = new HashSet<>();

    private static final Color CELL_BASE = Color.web("#3A3650");
    private static final Color CELL_HIGHLIGHT = Color.web("#9E8CFF");
    private static final Color CELL_CORRECT = Color.web("#34D0C3");
    private static final Color CELL_WRONG = Color.web("#FF6E9A");

    private Rectangle[][] grid;

    @FXML
    public void initialize() {
        patternGrid.setHgap(10);
        patternGrid.setVgap(10);
        patternGrid.setAlignment(Pos.CENTER);
        updateRoundLabel();
        loadnewPattern();
    }

    private void loadnewPattern() {
        Platform.runLater(() -> {
            clearGrid();

            grid = new Rectangle[rows][cols];
            List<String> allCoords = new ArrayList<>();

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    Rectangle cell = new Rectangle(CellSize, CellSize, CELL_BASE);
                    cell.setArcWidth(10);
                    cell.setArcHeight(10);

                    String coordKey = i + "," + j;
                    allCoords.add(coordKey);

                    int finalI = i;
                    int finalJ = j;

                    cell.setDisable(true);

                    cell.setOnMouseClicked(event -> {
                        String key = finalI + "," + finalJ;
                        if (selectedCoords.contains(key)) return;

                        if (patternCoords.contains(key)) {
                            highlightCell(cell, CELL_CORRECT);
                            selectedCoords.add(key);
                        } else {
                            highlightCell(cell, CELL_WRONG);
                            cell.setDisable(true);
                            hearts--;
                            updateHearts();

                            if (hearts <= 0) {
                                gameOver = true;
                                showTextGoBack("Game Over");
                            }
                        }
                    });

                    grid[i][j] = cell;
                    patternGrid.add(cell, j, i);
                }
            }

            Collections.shuffle(allCoords);

            int total = rows * cols;

            int minCells = Math.max(2, (int) Math.ceil(0.20 * total));
            int maxCells = Math.max(minCells, (int) Math.floor(0.60 * total));

            double muP = 0.40;
            double sdP = 0.35;

            int patternSize;
            while (true) {
                double p = muP + sdP * rng.nextGaussian();
                p = Math.max((double) minCells / total, Math.min((double) maxCells / total, p));
                patternSize = (int) Math.round(p * total);
                if (patternSize >= minCells && patternSize <= maxCells) break;
            }

            patternSize = Math.min(patternSize, 10);

            Collections.shuffle(allCoords, rng);
            patternCoords.addAll(allCoords.subList(0, Math.min(patternSize, allCoords.size())));

            for (String coord : patternCoords) {
                String[] split = coord.split(",");
                int i = Integer.parseInt(split[0]);
                int j = Integer.parseInt(split[1]);
                grid[i][j].setFill(CELL_HIGHLIGHT);
                highlight.add(grid[i][j]);
            }

            int hideTime = Math.min(maxHideTimeMs, baseTimeMs + (patternSize * perCellMs));

            new Thread(() -> {
                try {
                    Thread.sleep(hideTime);
                } catch (InterruptedException ignored) {}

                Platform.runLater(() -> {
                    for (Rectangle rect : highlight) {
                        rect.setFill(CELL_BASE);
                    }
                    disableAllCells(false);
                });
            }).start();
        });
    }

    @FXML
    private void handleSubmit() {
        if (gameOver) return;

        boolean correct = selectedCoords.containsAll(patternCoords);

        if (correct) {
            if (rows < maxGridSize) {
                increasingOrder();
                updateRoundLabel();
            } else {
                gameOver = true;
                showTextGoBack(" Pattern Modulation Complete!");
            }
        } else {
            hearts--;
            updateHearts();
            if (hearts <= 0) {
                gameOver = true;
                showTextGoBack("Game Over!");
            }
            if (!gameOver) increasingOrder();
        }
    }

    private void updateHearts() {
        ImageView[] heartsArray = {heart5, heart4, heart3, heart2, heart1};
        for (int i = 0; i < heartsArray.length; i++) {
            heartsArray[i].setVisible(i < hearts);
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MemGame.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) patternGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void increasingOrder() {
        round++;
        System.out.println(round);

        if (round % 3 == 0) {
            rows = Math.min(rows + 1, maxGridSize);
            cols = Math.min(cols + 1, maxGridSize);
        } else {
            if (cols < maxGridSize) cols++;
            else if (rows < maxGridSize) {
                rows++;
                cols = 2;
            }
        }
        loadnewPattern();
    }

    private void disableAllCells(boolean disable) {
        if (grid == null) return;
        for (Rectangle[] row : grid) {
            for (Rectangle cell : row) {
                cell.setDisable(disable);
            }
        }
    }

    private void showTextGoBack(String message) {
        gameOverLabel.setText(message);
        gameOverlay.setVisible(true);
        gameOverlay.setManaged(true);
        submitButton.setDisable(true);
        disableAllCells(true);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {}

            Platform.runLater(() -> {
                gameOverlay.setVisible(false);
                gameOverlay.setManaged(false);
                goBack();
            });
        }).start();
    }

    private void clearGrid() {
        patternGrid.getChildren().clear();
        highlight.clear();
        patternCoords.clear();
        selectedCoords.clear();
    }

    private void updateRoundLabel() {
        if (roundLabel != null)
            roundLabel.setText("Round " + Math.max(1, round + 1));
    }

    private void highlightCell(Rectangle cell, Color color) {
        // Make sure the initial fill is a color
        Color currentColor = (Color) cell.getFill();
        FillTransition fillTransition = new FillTransition(Duration.millis(300), cell, currentColor, color);
        fillTransition.play();
    }
}
