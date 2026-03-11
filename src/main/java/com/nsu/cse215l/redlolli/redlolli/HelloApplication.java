package com.nsu.cse215l.redlolli.redlolli;

// JavaFX imports
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

// Java standard imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// Game package imports
import com.nsu.cse215l.redlolli.redlolli.entities.Entity;
import com.nsu.cse215l.redlolli.redlolli.entities.Item;
import com.nsu.cse215l.redlolli.redlolli.entities.Monster;
import com.nsu.cse215l.redlolli.redlolli.entities.Player;
import com.nsu.cse215l.redlolli.redlolli.map.Maze;
import com.nsu.cse215l.redlolli.redlolli.ui.GameRenderer;
import com.nsu.cse215l.redlolli.redlolli.ui.HowToPlayRenderer;

/**
 * Main game controller for "Escape Pale Luna".
 * Manages game lifecycle, scene transitions, input handling, and game logic.
 * All rendering is delegated to classes in the ui package.
 */
public class HelloApplication extends Application {

    // ========================= CONSTANTS =========================

    /** Map CSV files for each of the 3 levels. */
    private static final String[] MAP_FILES = {"/map.csv", "/map2.csv", "/map3.csv"};

    /** Item names discovered at each level. */
    private static final String[] ITEM_NAMES = {"Mud", "Shovel", "Rope"};

    /** Number of chests per level. */
    private static final int[] CHESTS_PER_LEVEL = {3, 4, 5};

    /** Main headline text shown when a lolli is found on each level. */
    private static final String[] ITEM_FOUND_MAIN_TEXT = {"Mud Found", "Shovel Found", "Rope Found"};

    /** Button label text for the item-found screen on each level. */
    private static final String[] ITEM_FOUND_BUTTON_TEXT = {"here.", "use", "now"};

    /** Duration of lolli reveal animation in frames (2 seconds at 60 FPS). */
    private static final int LOLLI_REVEAL_DURATION = 120;

    // ========================= CORE FIELDS =========================

    private Stage mainWindow;
    private AnimationTimer gameLoop;
    private boolean isPlaying = false;
    private final Set<KeyCode> activeKeys = new HashSet<>();

    // ========================= GAME ENTITIES =========================

    private Player player;
    private Maze maze;
    private Monster paleLuna;
    private List<Entity> entities = new ArrayList<>();
    private List<Item> chests = new ArrayList<>();

    // ========================= LEVEL STATE =========================

    private int currentLevel = 1;
    private boolean showingItemFound = false;
    private boolean levelFoundLolli = false;

    // ========================= VISUAL EFFECTS STATE =========================

    private int warningFlashTimer = 0;
    private double pulsePhaseHUD = 0;

    // Lolli reveal animation
    private GameRenderer.LolliRevealState lolliRevealState = null;

    // ========================= APPLICATION LIFECYCLE =========================

    @Override
    public void start(Stage stage) {
        this.mainWindow = stage;
        mainWindow.setScene(createMainMenu());
        mainWindow.setTitle("Escape Pale Luna");
        mainWindow.show();
    }

    public static void main(String[] args) {
        launch();
    }

    // ========================= MENU SCREENS =========================

    /** Creates the main menu scene with title, subtitle, and navigation buttons. */
    private Scene createMainMenu() {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        Text title = new Text("ESCAPE PALE LUNA");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        title.setFill(Color.DARKRED);

        Text subtitle = new Text("Find the red lollies. Survive the demon.");
        subtitle.setFont(Font.font("Arial", 18));
        subtitle.setFill(Color.GRAY);

        Button newGameBtn = createStyledButton("> NEW GAME");
        Button howToPlayBtn = createStyledButton("HOW TO PLAY");
        Button exitBtn = createStyledButton("EXIT");

        newGameBtn.setOnAction(e -> startGame(1));
        howToPlayBtn.setOnAction(e -> mainWindow.setScene(createHowToPlayScreen()));
        exitBtn.setOnAction(e -> System.exit(0));

        layout.getChildren().addAll(title, subtitle, newGameBtn, howToPlayBtn, exitBtn);
        return new Scene(layout, 880, 730);
    }

    /** Creates a styled button with hover effects matching the horror theme. */
    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        String normalStyle = "-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-border-color: darkred; -fx-border-width: 1px; -fx-padding: 10 40;";
        String hoverStyle = "-fx-background-color: #330000; -fx-text-fill: white; -fx-border-color: red; -fx-border-width: 1px; -fx-padding: 10 40;";
        btn.setStyle(normalStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        return btn;
    }

    /** Creates the How to Play screen with scrollable canvas content. */
    private Scene createHowToPlayScreen() {
        double canvasWidth = 880, canvasHeight = 1700;
        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        HowToPlayRenderer.drawContent(canvas.getGraphicsContext2D(), canvasWidth, canvasHeight);

        Button backBtn = createStyledButton("BACK TO MENU");
        backBtn.setOnAction(e -> mainWindow.setScene(createMainMenu()));

        VBox wrapper = new VBox(0);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle("-fx-background-color: #050508;");
        wrapper.getChildren().addAll(canvas, backBtn);
        VBox.setMargin(backBtn, new Insets(10, 0, 30, 0));

        ScrollPane scrollPane = new ScrollPane(wrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #050508; -fx-background-color: #050508;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Ensure scroll starts at the top after layout is complete
        Platform.runLater(() -> scrollPane.setVvalue(0));

        return new Scene(scrollPane, 880, 730);
    }

    // ========================= GAME INITIALIZATION =========================

    /** Initializes and starts a new game at the specified level. */
    private void startGame(int level) {
        currentLevel = level;
        resetGameState();
        loadLevel();
        setupGameScene();
    }

    /** Resets all game state variables for a fresh level start. */
    private void resetGameState() {
        entities.clear();
        chests.clear();
        activeKeys.clear();
        paleLuna = null;
        isPlaying = true;
        showingItemFound = false;
        levelFoundLolli = false;
        warningFlashTimer = 0;
        pulsePhaseHUD = 0;
        lolliRevealState = null;
    }

    /** Loads the maze and spawns all entities for the current level. */
    private void loadLevel() {
        maze = new Maze(MAP_FILES[currentLevel - 1]);

        double spawnX = maze.getPlayerSpawnCol() * Maze.TILE_SIZE + 10;
        double spawnY = maze.getPlayerSpawnRow() * Maze.TILE_SIZE + Maze.Y_OFFSET + 10;
        player = new Player(spawnX, spawnY);
        entities.add(player);

        spawnEntities();
    }

    /** Creates the game canvas, binds input, and starts the game loop. */
    private void setupGameScene() {
        Canvas canvas = new Canvas(880, 730);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Scene gameScene = new Scene(new Group(canvas), 880, 730, Color.BLACK);

        gameScene.setOnKeyPressed(e -> activeKeys.add(e.getCode()));
        gameScene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));

        if (gameLoop != null) gameLoop.stop();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isPlaying) {
                    update();
                    render(gc);
                }
            }
        };
        gameLoop.start();
        mainWindow.setScene(gameScene);
    }

    /** Spawns Pale Luna and chests from the map grid, randomly assigning the lolli. */
    private void spawnEntities() {
        int[][] grid = maze.getMapGrid();
        List<int[]> chestPositions = new ArrayList<>();

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == 2 || grid[row][col] == 3) {
                    chestPositions.add(new int[]{row, col});
                    grid[row][col] = 0;
                } else if (grid[row][col] == 5) {
                    paleLuna = new Monster(col * Maze.TILE_SIZE + 7.5, row * Maze.TILE_SIZE + Maze.Y_OFFSET + 7.5);
                    entities.add(paleLuna);
                    grid[row][col] = 0;
                }
            }
        }

        // Randomly pick one chest to hold the Red Lolli
        int lolliIndex = new Random().nextInt(chestPositions.size());
        for (int i = 0; i < chestPositions.size(); i++) {
            int[] pos = chestPositions.get(i);
            Item chest = new Item(pos[1] * Maze.TILE_SIZE + 12, pos[0] * Maze.TILE_SIZE + Maze.Y_OFFSET + 12, i == lolliIndex);
            chests.add(chest);
            entities.add(chest);
        }
    }

    // ========================= GAME LOOP =========================

    /** Main update tick: handles input, physics, AI, collisions, and state transitions. */
    private void update() {
        if (showingItemFound) return;

        // Lolli reveal animation freeze — tick timer, block gameplay
        if (lolliRevealState != null && lolliRevealState.active) {
            lolliRevealState.timer--;
            lolliRevealState.phase += 0.15;
            if (lolliRevealState.timer <= 0) {
                lolliRevealState.active = false;
                showItemFoundScreen();
            }
            return;
        }

        // Player movement
        player.update();
        if (activeKeys.contains(KeyCode.W)) player.move(0, -1, maze);
        if (activeKeys.contains(KeyCode.S)) player.move(0, 1, maze);
        if (activeKeys.contains(KeyCode.A)) player.move(-1, 0, maze);
        if (activeKeys.contains(KeyCode.D)) player.move(1, 0, maze);

        // Escape room check
        boolean inEscapeRoom = maze.isEscapeRoom(player.getHitbox());
        player.setInEscapeRoom(inEscapeRoom);

        // Chest collisions
        checkChestCollisions();

        // Pale Luna AI
        updatePaleLuna(inEscapeRoom);
    }

    /** Checks player collision with chests and triggers lolli reveal if found. */
    private void checkChestCollisions() {
        for (Item chest : chests) {
            if (!chest.isCollected() && player.getHitbox().intersects(chest.getHitbox())) {
                chest.collect();
                if (chest.hasLolli()) {
                    levelFoundLolli = true;
                    lolliRevealState = new GameRenderer.LolliRevealState(
                            chest.getX(), chest.getY(), LOLLI_REVEAL_DURATION);
                    return;
                }
            }
        }
    }

    /** Updates Pale Luna's AI state machine and checks for death conditions. */
    private void updatePaleLuna(boolean inEscapeRoom) {
        if (paleLuna == null) return;

        Monster.State prevState = paleLuna.getState();
        paleLuna.update(player.getX(), player.getY(), inEscapeRoom, maze);

        // Flash warning when chase begins
        if (prevState == Monster.State.IDLE && paleLuna.getState() == Monster.State.CHASING) {
            warningFlashTimer = 30;
        }
        if (warningFlashTimer > 0) warningFlashTimer--;

        player.setBeingChased(paleLuna.isChasing());

        // Death: touched by chasing Pale Luna outside escape room
        if (!inEscapeRoom && paleLuna.isChasing()
                && player.getHitbox().intersects(paleLuna.getHitbox())) {
            triggerDeath();
            return;
        }

        // Death: left escape room while Pale Luna is waiting at door (within line of sight)
        if (!inEscapeRoom && paleLuna.isWaitingAtDoor()) {
            boolean canSee = maze.hasLineOfSight(
                    paleLuna.getX() + 12, paleLuna.getY() + 12,
                    player.getX() + 10, player.getY() + 10);
            if (canSee) {
                triggerDeath();
            }
        }
    }

    /** Delegates all rendering to GameRenderer. */
    private void render(GraphicsContext gc) {
        pulsePhaseHUD = GameRenderer.render(gc, maze, entities, paleLuna, player,
                warningFlashTimer, lolliRevealState, currentLevel, chests, ITEM_NAMES, pulsePhaseHUD);
    }

    // ========================= LEVEL TRANSITIONS =========================

    /** Advances to the next level or triggers victory if all levels complete. */
    private void advanceLevel() {
        if (currentLevel >= 3) {
            triggerVictory();
        } else {
            startGame(currentLevel + 1);
        }
    }

    /** Shows the item-found inter-level screen with creepy text and continue button. */
    private void showItemFoundScreen() {
        isPlaying = false;
        if (gameLoop != null) gameLoop.stop();
        showingItemFound = true;

        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        Text paleLunaText = new Text("pale luna smiles wide...");
        paleLunaText.setFont(Font.font("Serif", FontWeight.BOLD, 28));
        paleLunaText.setFill(Color.rgb(120, 0, 0));

        Text mainText = new Text(ITEM_FOUND_MAIN_TEXT[currentLevel - 1]);
        mainText.setFont(Font.font("Serif", FontWeight.BOLD, 65));
        mainText.setFill(Color.DARKRED);

        Text subText = new Text("the red lolli transforms...");
        subText.setFont(Font.font("Serif", 20));
        subText.setFill(Color.rgb(100, 100, 100));

        String[] itemDescriptions = {
                "Cold, wet earth clings to your fingers.",
                "Rusted metal. It still cuts deep.",
                "Fraying strands. Strong enough to bind."
        };
        Text descText = new Text(itemDescriptions[currentLevel - 1]);
        descText.setFont(Font.font("Serif", 18));
        descText.setFill(Color.rgb(150, 140, 130));

        Button continueBtn = new Button(ITEM_FOUND_BUTTON_TEXT[currentLevel - 1]);
        continueBtn.setFont(Font.font("Serif", FontWeight.BOLD, 28));
        String normalStyle = "-fx-background-color: #1a0000; -fx-text-fill: #cc0000; -fx-border-color: #660000; -fx-border-width: 2px; -fx-padding: 12 50; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #330000; -fx-text-fill: #ff3333; -fx-border-color: #990000; -fx-border-width: 2px; -fx-padding: 12 50; -fx-cursor: hand;";
        continueBtn.setStyle(normalStyle);
        continueBtn.setOnMouseEntered(e -> continueBtn.setStyle(hoverStyle));
        continueBtn.setOnMouseExited(e -> continueBtn.setStyle(normalStyle));
        continueBtn.setOnAction(e -> {
            showingItemFound = false;
            advanceLevel();
        });

        layout.getChildren().addAll(paleLunaText, mainText, subText, descText, new Text(""), continueBtn);
        mainWindow.setScene(new Scene(layout, 880, 730));
    }

    // ========================= GAME OVER SCREENS =========================

    /** Shows the death screen with creepy text and restart/menu options (permadeath). */
    private void triggerDeath() {
        isPlaying = false;
        if (gameLoop != null) gameLoop.stop();

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        String[] deathLines = {
                "pale luna smiles wide",
                "there is no escape",
                "pale luna smiles wide",
                "no more lollies to take",
                "pale luna smiles wide",
                "now you are dead"
        };

        Text title = new Text("YOU DIED");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 70));
        title.setFill(Color.DARKRED);
        layout.getChildren().add(title);

        for (String line : deathLines) {
            Text t = new Text(line);
            t.setFont(Font.font("Arial", 20));
            t.setFill(Color.LIGHTGRAY);
            layout.getChildren().add(t);
        }

        layout.getChildren().add(new Text(""));

        Button restartBtn = createStyledButton("RESTART FROM LEVEL 1");
        restartBtn.setOnAction(e -> startGame(1));

        Button menuBtn = createStyledButton("MAIN MENU");
        menuBtn.setOnAction(e -> mainWindow.setScene(createMainMenu()));

        layout.getChildren().addAll(restartBtn, menuBtn);
        mainWindow.setScene(new Scene(layout, 880, 730));
    }

    /** Shows the victory screen when all 3 levels are completed. */
    private void triggerVictory() {
        isPlaying = false;
        if (gameLoop != null) gameLoop.stop();

        VBox layout = new VBox(16);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        String[] victoryLines = {
                "pale luna smiles wide",
                "the ground is soft",
                "pale luna smiles wide",
                "there is a hole",
                "pale luna smiles wide",
                "tie her up with rope",
                "",
                "congratulations! you have escaped from pale luna"
        };

        Text title = new Text("YOU ESCAPED");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        title.setFill(Color.LIMEGREEN);
        layout.getChildren().add(title);

        for (String line : victoryLines) {
            Text t = new Text(line);
            if (line.startsWith("congratulations")) {
                t.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                t.setFill(Color.GOLD);
            } else {
                t.setFont(Font.font("Arial", 20));
                t.setFill(Color.LIGHTGRAY);
            }
            layout.getChildren().add(t);
        }

        layout.getChildren().add(new Text(""));

        Button menuBtn = createStyledButton("MAIN MENU");
        menuBtn.setOnAction(e -> mainWindow.setScene(createMainMenu()));
        layout.getChildren().add(menuBtn);

        mainWindow.setScene(new Scene(layout, 880, 730));
    }
}
