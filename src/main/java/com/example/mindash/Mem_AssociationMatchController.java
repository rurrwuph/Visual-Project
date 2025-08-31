package com.example.mindash;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class Mem_AssociationMatchController {

    @FXML private AnchorPane rootPane;
    @FXML private Label statusLabel;
    @FXML private Label attemptsLabel;
    @FXML private Button restartButton;
    @FXML private Button previewButton;

    private GridPane cardsGrid;
    private StackPane overlay;
    private Label countDownLabel;
    private Timeline countDown;

    // Tuning
    private static final int START_ROWS = 2, START_COLS = 2;
    private static final int MAX_ROWS = 5, MAX_COLS = 5;
    private static final boolean PREVIEW_AT_START = true;
    private static final int PREVIEW_BASE_MS = 2000;         // base preview duration
    private static final int PREVIEW_PER_PAIR_MS = 120;      // per-pair add
    private static final int PREVIEW_MAX_MS = 5000;

    // Styles
    private static final String CARD_BASE =
            "-fx-font-size: 20px; -fx-background-color: #3b3b3b; -fx-wrap-text: true; -fx-text-fill: #e6edf3; -fx-background-radius: 12;";
    private static final String CARD_FACEUP =
            "-fx-font-size: 20px; -fx-background-color: #2f81f7; -fx-wrap-text: true;  -fx-text-fill: white; -fx-background-radius: 12;";
    private static final String CARD_MATCHED =
            "-fx-font-size: 20px; -fx-background-color: #1f7a1f; -fx-wrap-text: true; -fx-text-fill: white; -fx-background-radius: 12;";
    private static final String CARD_MISMATCH =
            "-fx-font-size: 20px; -fx-background-color: #8b1d1d; -fx-wrap-text: true; -fx-text-fill: white; -fx-background-radius: 12;";

    // State
    private int rows = START_ROWS, cols = START_COLS;
    private final Map<Button, String> valueByCard = new HashMap<>();
    private Button first = null, second = null;
    private boolean inputLocked = false;
    private int attempts = 0;
    private int matchesFound = 0;

    // Word pool
    private static final List<String> WORD_POOL = new ArrayList<>(Arrays.asList(
            "MONKEY","GORILLA","LEOPARD","CHEETAH","HYENA","HIPPO","RHINO","BUFFALO","BISON","MOOSE","SQUIRREL","OTTER","BADGER","BEAVER","HARE","PIG","BOAR","DONKEY","MULE","YAK","LLAMA","KOALA","KANGAROO","GIRAFFE","ELEPHANT",
            "PENGUIN","PARROT","OWL","FALCON","SPARROW","SWAN","DUCK","GOOSE","PEACOCK","CROW","RAVEN","DOVE","HAWK","HERON","PELICAN","FLAMINGO",
            "OCTOPUS","SEAL","TURTLE","CRAB","SQUID","JELLYFISH","STARFISH","LOBSTER","SEAHORSE","SHRIMP",
            "BUTTERFLY","DRAGONFLY","LADYBUG","BEETLE","MOTH","BEE","ANT","SPIDER","MOSQUITO","FIREFLY",
            "PINEAPPLE","WATERMELON","STRAWBERRY","BLUEBERRY","CHERRY","PEACH","PEAR","KIWI","PAPAYA","GUAVA","LYCHEE","JACKFRUIT","DATE","PLUM","FIG","AVOCADO",
            "TOMATO","POTATO","CARROT","ONION","GARLIC","GINGER","CABBAGE","LETTUCE","BROCCOLI","CAULIFLOWER","CUCUMBER","PUMPKIN","SPINACH","CHILI","PEPPER","CORN","BEAN","PEANUT","ALMOND","WALNUT",
            "SPOON","FORK","KNIFE","PLATE","BOWL","BOTTLE","GLASS","MUG","MIRROR","PILLOW","BLANKET","CURTAIN","DOOR","WINDOW","BRUSH","TOWEL","SOAP","COMB","BUCKET","BASKET","SCISSORS","TAPE","ROPE","STRING",
            "HAMMER","WRENCH","PLIERS","SAW","DRILL","NAIL","BOLT","SCREW","LADDER","SHOVEL","RULER","GLUE",
            "LAPTOP","COMPUTER","TABLET","CAMERA","RADIO","SPEAKER","PRINTER","REMOTE","CHARGER","CABLE","ROUTER","MODEM","ROBOT","DRONE","HEADSET","MICROPHONE",
            "PARK","BEACH","ISLAND","VALLEY","CANYON","DESERT","JUNGLE","FOREST","MOUNTAIN","RIVER","LAKE","OCEAN","HARBOR","PORT","BRIDGE","TUNNEL","AIRPORT","STATION","MARKET","SCHOOL","HOSPITAL","MUSEUM","LIBRARY","CASTLE","TOWER","PALACE","FARM","GARDEN",
            "DOCTOR","NURSE","TEACHER","ENGINEER","CHEF","PILOT","LAWYER","FARMER","ARTIST","WRITER","SINGER","DANCER","DRIVER","ACTOR","BANKER","BARBER","PAINTER","COOK","GUIDE",
            "PIZZA","PASTA","BURGER","SANDWICH","SALAD","SOUP","CURRY","STEAK","NOODLES","WATER","TEA","COFFEE","JUICE","SODA",
            "CIRCLE","SQUARE","TRIANGLE","RECTANGLE","STAR","HEART","RED","BLUE","GREEN","YELLOW","PURPLE","PINK","BROWN","BLACK","WHITE","GRAY",
            "RAIN","SUN","CLOUD","WIND","SNOW","FOG","STORM","THUNDER","LIGHTNING","RAINBOW",
            "MOON","EARTH","MARS","COMET","ASTEROID","ROCKET",
            "BICYCLE","SCOOTER","MOTORCYCLE","TRUCK","BOAT","SHIP","SUBWAY","TRAM","HELICOPTER","FERRY","TAXI",
            "WATCH","CALENDAR","HOURGLASS"
    ));

    @FXML
    public void initialize() {
        setupGridHost();
        restartButton.setOnAction(e -> restartModule());
        setupPreviewButton();
        startNewBoard();
    }

    /* ---------- UI scaffolding ---------- */

    private void setupGridHost() {
        cardsGrid = new GridPane();
        cardsGrid.setHgap(12);
        cardsGrid.setVgap(12);
        cardsGrid.setAlignment(Pos.CENTER);

        AnchorPane.setTopAnchor(cardsGrid, 100.0);
        AnchorPane.setLeftAnchor(cardsGrid, 60.0);
        AnchorPane.setRightAnchor(cardsGrid, 60.0);
        AnchorPane.setBottomAnchor(cardsGrid, 140.0);

        rootPane.getChildren().add(cardsGrid);
    }

    private void ensureOverlay() {
        if (overlay != null) return;
        countDownLabel = new Label();
        countDownLabel.setStyle("-fx-font-size: 64px; -fx-font-weight: 800; -fx-text-fill: white;");
        overlay = new StackPane(countDownLabel);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        overlay.setAlignment(Pos.CENTER);
        overlay.setVisible(false);
        overlay.setManaged(false);
        overlay.setPickOnBounds(true);
        AnchorPane.setTopAnchor(overlay, 0d);
        AnchorPane.setLeftAnchor(overlay, 0d);
        AnchorPane.setRightAnchor(overlay, 0d);
        AnchorPane.setBottomAnchor(overlay, 0d);
        rootPane.getChildren().add(overlay);
    }

    private void showOverlay(String text, String style) {
        ensureOverlay();
        if (countDown != null) { countDown.stop(); countDown = null; }
        countDownLabel.setText(text);
        if (style != null) countDownLabel.setStyle(style);
        overlay.setVisible(true);
        overlay.setManaged(true);
    }

    private void hideOverlay() {
        overlay.setVisible(false);
        overlay.setManaged(false);
    }

    /* ---------- Board lifecycle ---------- */

    private void restartModule() {
        rows = START_ROWS;
        cols = START_COLS;
        attempts = 0;
        attemptsLabel.setText("Attempts: 0");
        statusLabel.setVisible(false);
        startNewBoard();
    }

    private void startNewBoard() {
        resetBoardState();
        ensureEvenCells();
        int pairCount = (rows * cols) / 2;

        // constraints
        cardsGrid.getChildren().clear();
        cardsGrid.getColumnConstraints().clear();
        cardsGrid.getRowConstraints().clear();
        for (int c = 0; c < cols; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / cols);
            cc.setHalignment(HPos.CENTER);
            cardsGrid.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < rows; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / rows);
            rc.setValignment(VPos.CENTER);
            cardsGrid.getRowConstraints().add(rc);
        }

        // deck + cards
        List<String> deck = buildDeck(pairCount);
        populateCards(deck, pairCount);

        if (PREVIEW_AT_START) {
            runPreview(computePreviewMs(pairCount), true);
        }
    }

    private void resetBoardState() {
        statusLabel.setVisible(false);
        setInputLocked(false);
        first = null; second = null;
        matchesFound = 0;
        valueByCard.clear();
    }

    private void ensureEvenCells() {
        if ((rows & 1) == 1 && (cols & 1) == 1) cols++;
        if (cols > MAX_COLS) cols = Math.max(2, MAX_COLS - (MAX_COLS & 1));
    }

    private List<String> buildDeck(int pairCount) {
        List<String> pool = new ArrayList<>(WORD_POOL);
        Collections.shuffle(pool);
        while (pool.size() < pairCount) pool.add("ITEM_" + pool.size()); // safety
        List<String> chosen = new ArrayList<>(pool.subList(0, pairCount));
        List<String> deck = new ArrayList<>(pairCount * 2);
        for (String w : chosen) { deck.add(w); deck.add(w); }
        Collections.shuffle(deck);
        return deck;
    }

    private void populateCards(List<String> deck, int pairCount) {
        int k = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String word = deck.get(k++);
                Button card = new Button("?");
                card.setStyle(CARD_BASE);
                card.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                valueByCard.put(card, word);
                card.setOnAction(e -> onCardClick(card, pairCount));
                cardsGrid.add(card, c, r);
            }
        }
    }

    /* ---------- Gameplay ---------- */

    private void onCardClick(Button card, int pairCount) {
        if (inputLocked || card == first || card.isDisabled()) return;

        revealCard(card);

        if (first == null) {
            first = card;
            return;
        }

        second = card;
        attempts++;
        attemptsLabel.setText("Attempts: " + attempts);

        if (Objects.equals(valueByCard.get(first), valueByCard.get(second))) {
            handleMatch(pairCount);
        } else {
            handleMismatch();
        }
    }

    private void revealCard(Button card) {
        card.setText(valueByCard.get(card));
        card.setStyle(CARD_FACEUP);
    }

    private void handleMatch(int pairCount) {
        first.setDisable(true);
        second.setDisable(true);
        first.setStyle(CARD_MATCHED);
        second.setStyle(CARD_MATCHED);
        first = null; second = null;
        matchesFound++;

        if (matchesFound == pairCount) {
            statusLabel.setText("Round cleared!");
            statusLabel.setStyle("-fx-text-fill: #57d06f;");
            statusLabel.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.millis(900));
            pause.setOnFinished(ev -> advanceOrFinish());
            pause.play();
        }
    }

    private void handleMismatch() {
        setInputLocked(true);
        first.setStyle(CARD_MISMATCH);
        second.setStyle(CARD_MISMATCH);
        PauseTransition pause = new PauseTransition(Duration.millis(850));
        pause.setOnFinished(ev -> {
            flipDown(first);
            flipDown(second);
            first = null; second = null;
            setInputLocked(false);
        });
        pause.play();
    }

    private void flipDown(Button card) {
        card.setText("?");
        card.setStyle(CARD_BASE);
    }

    private void advanceOrFinish() {
        if (nextBoardSize()) {
            setPreviewCountOnBtn(getPreviewCountFromBtn() + 1); // reward
            startNewBoard();
        } else {
            showAndReturn("🎉 Congratulations!\nYou cleared Association Match!");
        }
    }

    private boolean nextBoardSize() {
        int nr = rows, nc = cols + 1;
        if (nc > MAX_COLS) { nr++; nc = 2; }
        if (nr > MAX_ROWS) return false;

        // keep even cell count
        if ((nr & 1) == 1 && (nc & 1) == 1) {
            if (nc + 1 <= MAX_COLS) nc++;
            else if (nr + 1 <= MAX_ROWS) nr++;
            else return false;
        }
        rows = nr; cols = nc;
        return true;
    }

    /* ---------- Preview (auto + manual) ---------- */

    private void setupPreviewButton() {
        if (previewButton == null) return;
        if (previewButton.getText() == null || previewButton.getText().isBlank()) {
            setPreviewCountOnBtn(0);
        }
        updatePreviewButtonUI();
        previewButton.setOnAction(e -> tryManualPreview());
    }

    private boolean canManualPreview() {
        return !inputLocked && getPreviewCountFromBtn() > 0 && matchesFound < (rows * cols) / 2;
    }

    private void tryManualPreview() {
        if (!canManualPreview()) return;
        setPreviewCountOnBtn(getPreviewCountFromBtn() - 1);
        updatePreviewButtonUI();
        int pairCount = (rows * cols) / 2;
        runPreview(Math.min(PREVIEW_MAX_MS, 700 + pairCount * 120), false);
    }

    private int computePreviewMs(int pairCount) {
        return Math.min(PREVIEW_MAX_MS, PREVIEW_BASE_MS + pairCount * PREVIEW_PER_PAIR_MS);
    }

    /**
     * One unified preview:
     *  - locks input
     *  - reveals all unmatched
     *  - either shows a big countdown (showCounter=true) or a simple timed pause
     *  - flips back and unlocks input
     */
    private void runPreview(int ms, boolean showCounter) {
        setInputLocked(true);
        revealAllUnmatched();

        if (showCounter) {
            // show overlay with countdown seconds derived from ms
            int secs = Math.max(1, (int) Math.ceil(ms / 1000.0));
            ensureOverlay();
            overlay.setVisible(true);
            overlay.setManaged(true);
            countDownLabel.setText(Integer.toString(secs));
            if (countDown != null) countDown.stop();
            countDown = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                int next = Integer.parseInt(countDownLabel.getText()) - 1;
                if (next <= 0) {
                    endPreview();
                } else {
                    countDownLabel.setText(Integer.toString(next));
                    // pop anim
                    ScaleTransition pop = new ScaleTransition(Duration.millis(180), countDownLabel);
                    pop.setFromX(0.9); pop.setFromY(0.9);
                    pop.setToX(1.1);  pop.setToY(1.1);
                    pop.setAutoReverse(true);
                    pop.setCycleCount(2);
                    pop.play();
                }
            }));
            countDown.setCycleCount(secs);
            countDown.playFromStart();
        } else {
            // no counter, just pause
            ensureOverlay();
            overlay.setVisible(true);
            overlay.setManaged(true);
            countDownLabel.setText(""); // empty overlay to dim input
            PauseTransition pause = new PauseTransition(Duration.millis(ms));
            pause.setOnFinished(ev -> endPreview());
            pause.play();
        }
    }

    private void endPreview() {
        flipAllUnmatchedDown();
        hideOverlay();
        setInputLocked(false);
        if (countDown != null) { countDown.stop(); countDown = null; }
    }

    private void revealAllUnmatched() {
        valueByCard.keySet().forEach(card -> {
            if (!card.isDisabled()) {
                card.setText(valueByCard.get(card));
                card.setStyle(CARD_FACEUP);
            }
        });
    }

    private void flipAllUnmatchedDown() {
        valueByCard.keySet().forEach(card -> {
            if (!card.isDisabled()) {
                card.setText("?");
                card.setStyle(CARD_BASE);
            }
        });
    }

    /* ---------- Navigation / finish ---------- */

    private void showAndReturn(String msg) {
        setInputLocked(true);
        showOverlay(
                msg,
                "-fx-font-size: 36px; -fx-font-weight: 800; -fx-text-fill: white; -fx-font-family: 'Book Antiqua';"
        );
        PauseTransition pause = new PauseTransition(Duration.seconds(2.2));
        pause.setOnFinished(ev -> navigateTo("MemGame.fxml"));
        pause.play();
    }

    private void navigateTo(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Finished! (Navigation failed)");
            statusLabel.setStyle("-fx-text-fill: #57d06f;");
            statusLabel.setVisible(true);
            hideOverlay();
            setInputLocked(false);
        }
    }

    @FXML
    private void backto() {
        navigateTo("MemGame.fxml");
    }

    /* ---------- Small helpers ---------- */

    private void setInputLocked(boolean lock) {
        inputLocked = lock;
        updatePreviewButtonUI();
    }

    private int getPreviewCountFromBtn() {
        String t = previewButton.getText() == null ? "" : previewButton.getText();
        String digits = t.replaceAll("\\D+", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private void setPreviewCountOnBtn(int n) {
        previewButton.setText(Math.max(0, n) + " Preview \uD83D\uDCA1");
    }

    private void updatePreviewButtonUI() {
        if (previewButton != null) {
            previewButton.setDisable(getPreviewCountFromBtn() <= 0 || inputLocked);
        }
    }
}
