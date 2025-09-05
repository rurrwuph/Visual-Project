package com.example.mindash;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class react_Test2Controller implements Initializable {
    @FXML
    private Pane gamePane;
    @FXML private Label statusLabel;
    @FXML private Label bestTimeLabel;
    @FXML private Circle target1, target2, target3, target4, target5;

    private final int TARGET_BALLS = 5;
    private int ballsClicked = 0;
    private long startTime;
    private long bestTime = Long.MAX_VALUE;
    private Random random = new Random();
    private AnimationTimer gameLoop;
    private List<Ball> balls = new ArrayList<>();
    private Circle[] targets;

    // NEW: List to store individual reaction times for each red ball
    private List<Long> reactionTimes = new ArrayList<>();

    @FXML
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        targets = new Circle[]{target1, target2, target3, target4, target5};
        loadBestTime();
        startGame();
    }

    private void startGame() {
        ballsClicked = 0;
        startTime = System.currentTimeMillis();
        updateTargetIndicators();
        statusLabel.setText("Click red balls as they fall! Target: 0/" + TARGET_BALLS);
        gamePane.getChildren().clear();
        balls.clear();
        // NEW: clear previous reaction times
        reactionTimes.clear();

        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double elapsedSeconds = (now - lastUpdate) / 1_000_000_000.0;

                // create new balls occasionally
                if (random.nextInt(100) < 5) {
                    createBall();
                }

                // Update the existing balls
                List<Ball> toRemove = new ArrayList<>();
                for (Ball ball : balls) {
                    ball.y += ball.speed * elapsedSeconds * 60;
                    ball.circle.setCenterY(ball.y);

                    if (ball.y > gamePane.getHeight() + 20) {
                        toRemove.add(ball);
                    }
                }

                // Remove the balls that are out of bounds
                for (Ball ball : toRemove) {
                    gamePane.getChildren().remove(ball.circle);
                    balls.remove(ball);
                }

                if (ballsClicked >= TARGET_BALLS) {
                    this.stop();
                    endGame();
                }

                lastUpdate = now;
            }
        };
        gameLoop.start();
    }

    private void createBall() {
        double x = random.nextInt((int) gamePane.getWidth() - 30) + 15;
        Ball ball = new Ball();
        ball.circle = new Circle(18);
        ball.circle.setCenterX(x);
        ball.circle.setCenterY(0);
        ball.y = 0;

        // NEW: Record creation time for reaction time calculation
        ball.creationTime = System.currentTimeMillis();

        // Random color (red balls are targets)
        if (random.nextInt(2) == 0) {
            ball.circle.setFill(Color.RED);
            ball.circle.setUserData("target");
            ball.circle.setStroke(Color.WHITE);
            ball.circle.setStrokeWidth(2);
        } else {
            Color[] colors = {Color.BLUE, Color.GREEN, Color.YELLOW, Color.PURPLE, Color.CYAN, Color.ORANGE};
            ball.circle.setFill(colors[random.nextInt(colors.length)]);
            ball.circle.setUserData("normal");
        }

        ball.circle.setOnMousePressed(this::handleBallClick);
        ball.speed = 1 + random.nextDouble() * 2;

        gamePane.getChildren().add(ball.circle);
        balls.add(ball);
    }

    private void handleBallClick(MouseEvent event) {
        Circle clickedCircle = (Circle) event.getSource();

        if (clickedCircle != null && "target".equals(clickedCircle.getUserData())) {
            // NEW: Calculate reaction time for this specific ball
            long clickTime = System.currentTimeMillis();

            // Find the ball that was clicked
            for (Ball ball : balls) {
                if (ball.circle == clickedCircle) {
                    long reactionTime = clickTime - ball.creationTime;
                    reactionTimes.add(reactionTime);
                    break;
                }
            }

            gamePane.getChildren().remove(clickedCircle);

            balls.removeIf(ball -> {
                if (ball.circle == clickedCircle) {
                    return true;
                }
                return false;
            });

            ballsClicked++;
            updateTargetIndicators();
            statusLabel.setText("Click red balls as they fall! Target: " + ballsClicked + "/" + TARGET_BALLS);

            event.consume();
        }
    }

    private void updateTargetIndicators() {
        for (int i = 0; i < targets.length; i++) {
            if (i < ballsClicked) {
                targets[i].setFill(Color.RED);
            } else {
                targets[i].setFill(Color.GRAY);
            }
        }
    }

    private void endGame() {
        // NEW: Calculate average reaction time
        long totalReactionTime = 0;
        for (long time : reactionTimes) {
            totalReactionTime += time;
        }
        long averageReactionTime = totalReactionTime / TARGET_BALLS;

        // Compare with best time
        boolean newBest = false;
        if (averageReactionTime < bestTime) {
            newBest = true;
            saveBestTime(averageReactionTime);
        }

        try {
            // Load the result screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("react_result2.fxml"));
            Parent root = loader.load();
            react_Result2Controller controller = loader.getController();
            // NEW: Pass average reaction time instead of total time
            controller.setResults(averageReactionTime, bestTime);

            // Switch to the result screen
            Stage stage = (Stage) gamePane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBestTime() {
        try {
            if (Files.exists(Paths.get("react_bestTime2.txt"))) {
                String content = new String(Files.readAllBytes(Paths.get("react_bestTime2.txt")));
                bestTime = Long.parseLong(content.trim());
                bestTimeLabel.setText("Best time: " + bestTime + " ms");
            }
        } catch (IOException | NumberFormatException e) {
            bestTime = Long.MAX_VALUE;
            bestTimeLabel.setText("Best time: -- ms");
        }
    }

    private void saveBestTime(long time) {
        try {
            Files.write(Paths.get("react_bestTime2.txt"), String.valueOf(time).getBytes());
            bestTime = time;
            bestTimeLabel.setText("Best time: " + bestTime + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Ball {
        Circle circle;
        double y;
        double speed;
        // NEW: Track when each ball was created for reaction time calculation
        long creationTime;
    }
}