package com.example.mindash;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class Mem_VerbalMem {
    @FXML private AnchorPane rootPane;
    @FXML private Label wordLabel;
    @FXML private Label scoreLabel;
    @FXML private Label overlayMsg;
    @FXML private Button seenButton;
    @FXML private Button newButton;
    @FXML private ImageView backIcon;
    @FXML private ImageView  heart1;
    @FXML private ImageView  heart2;
    @FXML private ImageView heart3;
    @FXML private StackPane overlay;

    private final Random rng = new Random();
    private final Set<String> seenWords = new HashSet<>();
    private Deque<String> newDeck = new ArrayDeque<>();

    private String currentWord = "";
    private boolean currentWasSeen = false;
    private boolean markAsSeen = false;

    private int lives = 3;
    private int score = 0;

    private static final int MAX_WORDS = 100;

    private static final List <String> L4 = new ArrayList<>();
    private static final List <String> L5_6 = new ArrayList<>();
    private static final List <String> L7_9 = new ArrayList<>();
    private static boolean BUCKET_READY =false;
    private static int i4 =0, i5_6 = 0, i7_9 =0;

    private static final List<String> WORD_POOL = Arrays.asList(

            // — 4 letters (kept small; distinctive) —
            "AURA","BRIM","CLAD","CRUX","CUSP","DAWN","DUSK","ETCH","FLUX","GLOW","GRIT","HAZE","HUSH",
            "KNIT","LURK","MINT","MIRE","MOLT","ONYX","OPAL","PITH","RIFT","RIME","RUNE","SILT","SKEW","SMOG","SNUG","VIAL",

            // — 5 letters (core difficulty) —
            "ACRID","AMBLE","APRON","ARROW","BASIL","BEVEL","BISON","BLAZE","BLEAK","BLEND","BLUFF","BLUNT","BRACE","BRINE",
            "BRISK","BROOK","BURLY","CACHE","CADET","CARGO","CARVE","CAULK","CHAFE","CHANT","CHASM","CHIRP","CHOIR","CHOKE",
            "CHORD","CIVIC","CLASH","CLEAT","CLERK","CLINK","CLOAK","CLOVE","COAST","CORAL","CORPS","CRANE","CRANK","CRASH",
            "CRAVE","CREDO","CREST","CRISP","CROSS","CUMIN","CURIO","CURVE","CYCLE","DECAY","DELTA","DETOX","DIARY","DRAFT",
            "DRAIN","DRAPE","DREAD","DREGS","DRIFT","DROLL","DROOP","DWELL","EERIE","ELUDE","EMBER","ENACT","ENSUE","EPOCH",
            "EQUAL","EQUIP","ETHIC","FAINT","FEIGN","FERRY","FIBER","FJORD","FLAIR","FLESH","FLICK","FLINT","FLORA","FLOUR",
            "FLUKE","FLUME","FLUSH","FORGE","FORTE","FRAIL","FRAUD","FROND","FROTH","GAUZE","GAUNT","GHOST","GLADE","GLARE",
            "GLAZE","GLEAM","GLEAN","GLINT","GLOAT","GLOBE","GLOOM","GNASH","GNOME","GOUGE","GRAIL","GRAIN","GRASP","GRATE",
            "GRAVE","GREED","GREET","GRIEF","GRIND","GRIPE","GROAN","GROVE","GRUFF","GRIME","GUILE","GUSTO","HALVE","HARSH",
            "HAVEN","HAZEL","HEATH","HEFTY","HELIX","HERON","HINGE","HONEY","HORDE","HUMID","HUSKY","HYDRA","IDEAL","IDIOM",
            "IDYLL","IGLOO","IMBUE","IMPEL","IMPLY","INERT","INGOT","IRATE","IRONY","ISLET","ITCHY","IVORY","JELLY","JERKY",
            "JETTY","JEWEL","JOUST","JUMBO","KARMA","KNACK","KNOCK","LADEN","LAPSE","LARCH","LASSO","LATCH","LAYER","LEAFY",
            "LEVER","LILAC","LIMBO","LIVID","LOAMY","LOFTY","LOGIC","LOOPY","LOTUS","LUCID","LURCH","LYRIC","MAGMA","MAIZE",
            "MANGY","MAPLE","MARCH","MARSH","MAXIM","MERCY","MERIT","METAL","MIRTH","MOGUL","MOIST","MORAL","MORPH","MOSSY",
            "MOTIF","MOTTO","MOUND","MOURN","MUCUS","MULCH","MURAL","MURKY","MUSKY","MUTED","NADIR","NAVEL","NEIGH","NERVE",
            "NIECE","NINJA","NOBLE","NOVEL","NUDGE","NURSE","NYLON","OASIS","OCTET","ODDLY","ODIUM","OMBRE","ONION","OPERA",
            "OPINE","OPTIC","ORBIT","ORGAN","OTTER","OVERT","OVINE","OXIDE","OZONE","PANIC","PARSE","PATCH","PAUSE","PECAN",
            "PEDAL","PERCH","PESKY","PHASE","PHONY","PIXEL","PLAIT","PLATE","PLEAT","PLIER","PLUCK","PLUME","PLUSH","POISE",
            "POLAR","POLKA","PRISM","PROBE","PRONG","PROSE","PROWL","PRUNE","PSALM","PULSE","PUPIL","PUREE","PYGMY","QUAIL",
            "QUAKE","QUART","QUASH","QUEER","QUELL","QUERY","QUEST","QUICK","QUILL","QUILT","QUOTA","QUOTE","RADAR","RADON",
            "RAZOR","REACT","RELIC","RENAL","REPAY","RHINO","RHYME","RIGID","RIPEN","RIVAL","RIVET","ROGUE","ROOST","ROUGE",
            "ROUND","ROVER","RUSTY","SALVE","SAUCE","SCALD","SCARF","SCENE","SCION","SCOUR","SCOUT","SCOWL","SCRAP","SCREW",
            "SEPIA","SERUM","SHADE","SHAFT","SHARD","SHARK","SHARP","SHEAR","SHEEN","SHELF","SHELL","SHIFT","SHINE","SHIRE",
            "SHIRK","SHOCK","SHONE","SHORE","SHORN","SHRUB","SIEGE","SINEW","SIREN","SKEIN","SKULL","SLANT","SLATE","SLEEK",
            "SLEET","SLICE","SLICK","SLING","SLOPE","SMEAR","SMELT","SMIRK","SMITE","SMOKE","SMOTE","SNARE","SNEER","SNIDE",
            "SNIFF","SNIPE","SNUFF","SOOTY","SPEAR","SPELL","SPICE","SPIKE","SPINE","SPINY","SPLIT","SPORE","SPRIG","SPURT",
            "SQUAD","SQUAT","STACK","STAIR","STAKE","STALL","STARE","STARK","STEAD","STEEL","STEER","STERN","STILT","STOIC",
            "STOKE","STONE","STOOP","STORM","STOUT","STOVE","STRAP","STREW","STRIP","STUCK","STUDY","STUMP","STUNG","STUNT",
            "SUAVE","SULLY","SURLY","SWAMI","SWASH","SWATH","SWEEP","SWEPT","SWIRL","SWORD","SYRUP","TACKY","TANGY","TAPER",
            "TAROT","TAUNT","TEPID","TIGHT","TILDE","TIMID","TINNY","TITAN","TOAST","TONIC","TOPAZ","TORCH","TORSO","TORUS",
            "TOXIC","TRACE","TRAIT","TRAMP","TRAWL","TREAD","TRIAD","TRICE","TRITE","TROVE","TRUCE","TRUNK","TRUSS","TURBO",
            "TWEAK","TWINE","TWIRL","ULCER","ULTRA","UMBRA","UNLIT","UNSET","UNTIE","UPEND","UPSET","URBAN","UTTER","VAGUE",
            "VALET","VALOR","VALVE","VAPOR","VAULT","VEGAN","VENOM","VERGE","VERSE","VIGIL","VILLA","VIRAL","VIRTU","VIVID",
            "VOCAL","VOTER","VOUCH","VOWEL","WAFER","WAGER","WAIST","WALTZ","WARTY","WEARY","WEDGE","WEIRD","WHARF","WHEAT",
            "WHIFF","WHIRL","WIDEN","WIELD","WISPY","WITTY","WOOZY","WRATH","WREAK","WRECK","WREST","WRING","WRIST","WRYLY",
            "YACHT","YEARN","YIELD","ZESTY","ZONAL",

            // — 6–8 letters (rarer/longer; harder) —
            "ABACUS","ABRIDGE","ACCRUAL","ACRIMON","ALCHEMY","AMBERGRIS","ANVILLY"
    );

    @FXML
    public void initialize() {
        scoreLabel.setText("Score: 0");
        wordLabel.setAlignment(Pos.CENTER);

        bucketBuilt();

        newButton.setOnAction(e->handleAnswer(false));
        seenButton.setOnAction(e->handleAnswer(true));

        refillnewDeck();
        nextWord();
    }

    private void handleAnswer(boolean pressedSeen) {
//
        boolean correct = (pressedSeen == currentWasSeen);

        if(markAsSeen) {
            seenWords.add(currentWord);
            markAsSeen = false;
        }

        if(correct) {
            score++;
            scoreLabel.setText("Score: " +score);
            flashWord("#3CCB6C");
        }

        else {
            loseLife();
            flashWord("#E06363");
            if(lives<=0) {
                endGame();
                return;
            }
        }

        PauseTransition pause = new PauseTransition(Duration.millis(450));
        pause.setOnFinished(event -> nextWord());
        pause.play();
    }

    private void nextWord() {
        // prob of showing a previously seen word
        double perSeen = Math.min(0.25 + 0.02 * seenWords.size(), 0.70);

        if(!seenWords.isEmpty() && rng.nextDouble() <perSeen) {
//            rand
            int index = rng.nextInt(seenWords.size());
            Iterator <String> it = seenWords.iterator();

            for(int i=0; i<index; i++)
                it.next();

            currentWord = it.next();
            currentWasSeen = true;
            markAsSeen = false;
        }

        else {
            if(newDeck.isEmpty()) refillnewDeck();
            currentWord = newDeck.pollFirst();
            currentWasSeen = false;
            markAsSeen = true;
        }

        wordLabel.setText(currentWord);
        wordLabel.setTextFill(Paint.valueOf("#E6EDF3"));
    }

    private void flashWord(String hex) {
        wordLabel.setTextFill(Paint.valueOf(hex));
    }

    private void loseLife() {
        lives--;
        if(lives<3) heart3.setVisible(false);
        if(lives<2) heart2.setVisible(false);
        if(lives<1) heart1.setVisible(false);

    }

    private void endGame() {
        setButtonEnabled(false);
        showOverlay("Game Over!\nScore: " + score, true, 2000, this::goBackToMenu);
    }

    private void setButtonEnabled(boolean enabled) {
        newButton.setDisable(!enabled);
        seenButton.setDisable(!enabled);
    }

    private void showOverlay(String txt, boolean lockInout, int durationMs, Runnable after) {
        setButtonEnabled(false);
        if(overlay!= null && overlayMsg != null) {
            overlayMsg.setText(txt);
            overlayMsg.setAlignment(Pos.CENTER);
            overlayMsg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            StackPane.setAlignment(overlayMsg, Pos.CENTER);

            overlay.setVisible(true);
            overlay.setManaged(true);
        }

        PauseTransition p = new PauseTransition(Duration.millis(durationMs));

        p.setOnFinished(e-> {
            if(overlay!= null) {
                overlay.setVisible(false);
                overlay.setManaged(false);
            }
            if(after != null ) after.run();
        });
        p.play();
    }

    private void goBackToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MemGame.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        goBackToMenu();
    }


    private void bucketBuilt() {
        if(BUCKET_READY) return;
        for(String w: WORD_POOL) {
            int len = w.length();
            if(len==4) L4.add(w);
            else if(len<=6) L5_6.add(w);
            else if(len<=9) L7_9.add(w);
        }

        Collections.shuffle(L4);
        Collections.shuffle(L5_6);
        Collections.shuffle(L7_9);
        BUCKET_READY = true;

    }

    private void refillnewDeck() {
        // Build bucket copies excluding words the player has already seen
        List<String> b4 = new ArrayList<>();
        for (String w : L4) if (!seenWords.contains(w)) b4.add(w);
        List<String> b56 = new ArrayList<>();
        for (String w : L5_6) if (!seenWords.contains(w)) b56.add(w);
        List<String> b79 = new ArrayList<>();
        for (String w : L7_9) if (!seenWords.contains(w)) b79.add(w);

        // If  somehow exhausted everything, reset seen and try again
        if (b4.isEmpty() && b56.isEmpty() && b79.isEmpty()) {
            seenWords.clear();
            b4 = new ArrayList<>(L4);
            b56 = new ArrayList<>(L5_6);
            b79 = new ArrayList<>(L7_9);
        }

        // t in [0..1] ~ difficulty progression
        double t = Math.min(1.0, seenWords.size() / 40.0);

        // Start: more short/medium; Later: shift to medium/long
        double w4   = 0.55 * (1.0 - t) + 0.20 * t;  // 55% -> 20%
        double w56  = 0.35 * (1.0 - t) + 0.50 * t;  // 35% -> 50%
        double w79  = 0.10 * (1.0 - t) + 0.30 * t;  // 10% -> 30%

        // Generate a small batch into newDeck (keeps memory fresh and fast)
        int remaining = b4.size() + b56.size() + b79.size();
        int batchSize = Math.min(40, remaining);

        List<String> batch = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            // adjust weights if some buckets empty
            double a4  = b4.isEmpty()  ? 0.0 : w4;
            double a56 = b56.isEmpty() ? 0.0 : w56;
            double a79 = b79.isEmpty() ? 0.0 : w79;
            double sum = a4 + a56 + a79;
            if (sum <= 0.0) break;

            double x = rng.nextDouble() * sum;
            List<String> pick;
            if (x < a4) pick = b4;
            else if (x < a4 + a56) pick = b56;
            else pick = b79;

            // pick random from chosen bucket, remove it
            int idx = rng.nextInt(pick.size());
            batch.add(pick.remove(idx));
        }

        newDeck = new ArrayDeque<>(batch);

    }
}
