package com.nsu.cse215l.redlolli.redlolli;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import com.nsu.cse215l.redlolli.redlolli.entities.Player;
import com.nsu.cse215l.redlolli.redlolli.map.Maze;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import com.nsu.cse215l.redlolli.redlolli.entities.Entity;
import com.nsu.cse215l.redlolli.redlolli.entities.Item;
import com.nsu.cse215l.redlolli.redlolli.entities.Monster;
import javafx.scene.shape.FillRule;

import java.util.HashSet;
import java.util.Set;

public class HelloApplication extends Application {
    private Stage mainWindow;
    private AnimationTimer gameLoop;
    private boolean isPlaying = false;

    private final Set<KeyCode> activeKeys = new HashSet<>();
    private Player player;
    private Maze maze;
    private Monster paleLuna;

    private List<Entity> entities = new ArrayList<>();
    private List<Item> chests = new ArrayList<>();

    // Level progression
    private int currentLevel = 1;
    private final String[] MAP_FILES = {"/map.csv", "/map2.csv", "/map3.csv"};
    private final String[] ITEM_NAMES = {"Mud", "Shovel", "Rope"};
    private final int[] CHESTS_PER_LEVEL = {3, 4, 5};

    // Screen text for item found — only the main text, button text is separate
    private final String[] ITEM_FOUND_MAIN_TEXT = {"Mud Found", "Shovel Found", "Rope Found"};
    private final String[] ITEM_FOUND_BUTTON_TEXT = {"here.", "use", "now"};

    // Item found overlay — now uses a JavaFX scene overlay with button
    private boolean showingItemFound = false;
    private boolean levelFoundLolli = false;

    // Warning flash when Pale Luna activates
    private int warningFlashTimer = 0;
    private double pulsePhaseHUD = 0;

    @Override
    public void start(Stage stage) {
        this.mainWindow = stage;
        Scene menuScene = createMainMenu();
        mainWindow.setScene(menuScene);
        mainWindow.setTitle("Escape Pale Luna");
        mainWindow.show();
    }

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
        Button exitBtn = createStyledButton("EXIT");

        newGameBtn.setOnAction(e -> startGame(1));
        exitBtn.setOnAction(e -> System.exit(0));

        layout.getChildren().addAll(title, subtitle, newGameBtn, exitBtn);
        return new Scene(layout, 880, 730);
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        btn.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-border-color: darkred; -fx-border-width: 1px; -fx-padding: 10 40;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #330000; -fx-text-fill: white; -fx-border-color: red; -fx-border-width: 1px; -fx-padding: 10 40;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-border-color: darkred; -fx-border-width: 1px; -fx-padding: 10 40;"));
        return btn;
    }

    private void startGame(int level) {
        currentLevel = level;
        entities.clear();
        chests.clear();
        activeKeys.clear();
        paleLuna = null;
        isPlaying = true;
        showingItemFound = false;
        levelFoundLolli = false;
        warningFlashTimer = 0;
        pulsePhaseHUD = 0;

        // Load the correct map for this level
        maze = new Maze(MAP_FILES[currentLevel - 1]);

        // Spawn player at designated position
        double spawnX = maze.getPlayerSpawnCol() * Maze.TILE_SIZE + 10;
        double spawnY = maze.getPlayerSpawnRow() * Maze.TILE_SIZE + Maze.Y_OFFSET + 10;
        player = new Player(spawnX, spawnY);
        entities.add(player);

        spawnEntities();

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

    private void spawnEntities() {
        int[][] grid = maze.getMapGrid();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {

                if (grid[row][col] == 2) {
                    // Empty chest
                    Item chest = new Item(col * Maze.TILE_SIZE + 12, row * Maze.TILE_SIZE + Maze.Y_OFFSET + 12, false);
                    chests.add(chest);
                    entities.add(chest);
                    grid[row][col] = 0;
                } else if (grid[row][col] == 3) {
                    // Chest with the red lolli
                    Item chest = new Item(col * Maze.TILE_SIZE + 12, row * Maze.TILE_SIZE + Maze.Y_OFFSET + 12, true);
                    chests.add(chest);
                    entities.add(chest);
                    grid[row][col] = 0;
                } else if (grid[row][col] == 5) {
                    paleLuna = new Monster(col * Maze.TILE_SIZE + 7.5, row * Maze.TILE_SIZE + Maze.Y_OFFSET + 7.5);
                    entities.add(paleLuna);
                    grid[row][col] = 0;
                }
            }
        }
    }

    private void drawFogOfWar(GraphicsContext gc) {
        double playerCX = player.getX() + (player.getSize() / 2);
        double playerCY = player.getY() + (player.getSize() / 2);

        double visionRadius = 150;

        gc.save();
        gc.beginPath();
        gc.rect(0, 0, 880, 730);
        gc.arc(playerCX, playerCY, visionRadius, visionRadius, 0, 360);
        gc.setFillRule(FillRule.EVEN_ODD);
        gc.setFill(Color.BLACK);
        gc.fill();
        gc.restore();
    }

    private void drawHUD(GraphicsContext gc) {
        gc.setFill(Color.rgb(15, 15, 20));
        gc.fillRect(0, 0, 880, 50);

        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(2);
        gc.strokeLine(0, 50, 880, 50);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        // Level indicator
        gc.setFill(Color.rgb(180, 180, 200));
        gc.fillText("LEVEL " + currentLevel + "/3", 15, 20);

        // Red Lolli tracker (0/1 or 1/1)
        boolean foundLolli = chests.stream().anyMatch(c -> c.isCollected() && c.hasLolli());
        // Draw small lolli icon
        gc.setFill(Color.DARKRED);
        gc.fillOval(15, 28, 8, 8);
        gc.setStroke(Color.rgb(180, 150, 100));
        gc.setLineWidth(1.5);
        gc.strokeLine(19, 36, 19, 42);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        if (foundLolli) {
            gc.setFill(Color.LIMEGREEN);
            gc.fillText("RED LOLLI: 1/1", 28, 40);
        } else {
            gc.setFill(Color.rgb(200, 200, 200));
            gc.fillText("RED LOLLI: 0/1", 28, 40);
        }

        // Item needed
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("FIND: " + ITEM_NAMES[currentLevel - 1], 180, 20);

        // Chests opened
        long opened = chests.stream().filter(Item::isCollected).count();
        gc.setFill(Color.rgb(180, 160, 120));
        gc.fillText("CHESTS: " + opened + "/" + chests.size(), 180, 40);

        // --- Pale Luna timer with dynamic colors ---
        if (paleLuna != null) {
            Monster.State lunaState = paleLuna.getState();
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            switch (lunaState) {
                case IDLE:
                    int secondsLeft = paleLuna.getIdleTimer() / 60;
                    // Dynamic color: green > yellow > orange > red as timer decreases
                    Color timerColor;
                    if (secondsLeft > 10) {
                        timerColor = Color.LIMEGREEN;
                    } else if (secondsLeft > 6) {
                        timerColor = Color.YELLOW;
                    } else if (secondsLeft > 3) {
                        timerColor = Color.ORANGE;
                    } else {
                        // Flashing red in last 3 seconds
                        double flash = Math.sin(pulsePhaseHUD) * 0.5 + 0.5;
                        timerColor = Color.rgb(255, (int)(50 * flash), (int)(50 * flash));
                    }
                    pulsePhaseHUD += 0.2;

                    // Timer bar background
                    gc.setFill(Color.rgb(40, 40, 45));
                    gc.fillRect(400, 10, 200, 14);
                    gc.setStroke(Color.rgb(80, 80, 90));
                    gc.setLineWidth(1);
                    gc.strokeRect(400, 10, 200, 14);

                    // Timer bar fill
                    double fillRatio = (double) paleLuna.getIdleTimer() / 900.0;
                    gc.setFill(timerColor);
                    gc.fillRect(401, 11, 198 * fillRatio, 12);

                    gc.setFill(timerColor);
                    gc.fillText("PALE LUNA WAKES IN: " + secondsLeft + "s", 400, 40);
                    break;
                case CHASING:
                    // Chasing: flashing red bar
                    double chaseFlash = Math.sin(pulsePhaseHUD * 2) * 0.5 + 0.5;
                    pulsePhaseHUD += 0.3;
                    gc.setFill(Color.rgb(255, (int)(30 * chaseFlash), (int)(30 * chaseFlash)));
                    gc.fillRect(400, 10, 200, 14);
                    int chaseSecsLeft = paleLuna.getChaseTimer() / 60;
                    double chaseFill = (double) paleLuna.getChaseTimer() / 300.0;
                    gc.setFill(Color.rgb(200, 0, 0));
                    gc.fillRect(401, 11, 198 * chaseFill, 12);

                    gc.setFill(Color.RED);
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 15));
                    gc.fillText("!! HUNTING: " + chaseSecsLeft + "s !!", 420, 40);
                    break;
                case WAITING_AT_DOOR:
                    gc.setFill(Color.ORANGE);
                    gc.fillText("PALE LUNA: watching the door...", 400, 25);
                    int waitSecs = paleLuna.getWaitTimer() / 60;
                    gc.setFill(Color.rgb(255, 165, 0));
                    gc.fillText("DO NOT LEAVE! (" + waitSecs + "s)", 400, 40);
                    break;
            }
        }

        // Escape room icon on far right
        if (player.isInEscapeRoom()) {
            gc.setFill(Color.LIMEGREEN);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.fillText("[SAFE]", 820, 32);
        }
    }

    private void showItemFoundScreen() {
        isPlaying = false;
        if (gameLoop != null) gameLoop.stop();
        showingItemFound = true;

        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        // Creepy flickering title
        Text paleLunaText = new Text("pale luna smiles wide...");
        paleLunaText.setFont(Font.font("Serif", FontWeight.BOLD, 28));
        paleLunaText.setFill(Color.rgb(120, 0, 0));

        // The item name in huge creepy style
        String itemName = ITEM_FOUND_MAIN_TEXT[currentLevel - 1];
        Text mainText = new Text(itemName);
        mainText.setFont(Font.font("Serif", FontWeight.BOLD, 65));
        mainText.setFill(Color.DARKRED);

        // Creepy subtext
        Text subText = new Text("the red lolli transforms...");
        subText.setFont(Font.font("Serif", 20));
        subText.setFill(Color.rgb(100, 100, 100));

        // Item description
        String[] itemDescriptions = {
                "Cold, wet earth clings to your fingers.",
                "Rusted metal. It still cuts deep.",
                "Fraying strands. Strong enough to bind."
        };
        Text descText = new Text(itemDescriptions[currentLevel - 1]);
        descText.setFont(Font.font("Serif", 18));
        descText.setFill(Color.rgb(150, 140, 130));

        Text spacer = new Text("");

        // The button with the level's keyword
        String btnLabel = ITEM_FOUND_BUTTON_TEXT[currentLevel - 1];
        Button continueBtn = new Button(btnLabel);
        continueBtn.setFont(Font.font("Serif", FontWeight.BOLD, 28));
        continueBtn.setStyle("-fx-background-color: #1a0000; -fx-text-fill: #cc0000; -fx-border-color: #660000; -fx-border-width: 2px; -fx-padding: 12 50; -fx-cursor: hand;");
        continueBtn.setOnMouseEntered(e -> continueBtn.setStyle("-fx-background-color: #330000; -fx-text-fill: #ff3333; -fx-border-color: #990000; -fx-border-width: 2px; -fx-padding: 12 50; -fx-cursor: hand;"));
        continueBtn.setOnMouseExited(e -> continueBtn.setStyle("-fx-background-color: #1a0000; -fx-text-fill: #cc0000; -fx-border-color: #660000; -fx-border-width: 2px; -fx-padding: 12 50; -fx-cursor: hand;"));

        continueBtn.setOnAction(e -> {
            showingItemFound = false;
            advanceLevel();
        });

        layout.getChildren().addAll(paleLunaText, mainText, subText, descText, spacer, continueBtn);
        mainWindow.setScene(new Scene(layout, 880, 730));
    }

    private void update() {
        if (showingItemFound) return;

        player.update();

        if (activeKeys.contains(KeyCode.W)) player.move(0, -1, maze);
        if (activeKeys.contains(KeyCode.S)) player.move(0, 1, maze);
        if (activeKeys.contains(KeyCode.A)) player.move(-1, 0, maze);
        if (activeKeys.contains(KeyCode.D)) player.move(1, 0, maze);

        // Check if player is in escape room
        boolean inEscapeRoom = maze.isEscapeRoom(player.getHitbox());
        player.setInEscapeRoom(inEscapeRoom);

        // Check chest collisions
        for (Item chest : chests) {
            if (!chest.isCollected() && player.getHitbox().intersects(chest.getHitbox())) {
                chest.collect();

                if (chest.hasLolli()) {
                    // Found the red lolli! Show the inter-level screen
                    levelFoundLolli = true;
                    showItemFoundScreen();
                    return;
                }
            }
        }

        // Update Pale Luna AI
        if (paleLuna != null) {
            Monster.State prevState = paleLuna.getState();
            paleLuna.update(player.getX(), player.getY(), inEscapeRoom, maze);

            // Flash warning when chase begins
            if (prevState == Monster.State.IDLE && paleLuna.getState() == Monster.State.CHASING) {
                warningFlashTimer = 30; // Half-second red flash
            }

            if (warningFlashTimer > 0) warningFlashTimer--;

            player.setBeingChased(paleLuna.isChasing());

            // Death check: Pale Luna touches player while not in escape room
            if (!inEscapeRoom && paleLuna.isChasing() && player.getHitbox().intersects(paleLuna.getHitbox())) {
                triggerDeath();
                return;
            }

            // Death check: player steps out of escape room while Pale Luna is waiting outside
            if (!inEscapeRoom && paleLuna.isWaitingAtDoor()) {
                // Check if Pale Luna can see the player (line of sight = death)
                boolean canSee = maze.hasLineOfSight(
                        paleLuna.getX() + 12, paleLuna.getY() + 12,
                        player.getX() + 10, player.getY() + 10);
                if (canSee) {
                    triggerDeath();
                    return;
                }
            }
        }
    }

    private void advanceLevel() {
        if (currentLevel >= 3) {
            // All 3 levels complete — victory!
            triggerVictory();
        } else {
            startGame(currentLevel + 1);
        }
    }

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

        Text spacer = new Text("");
        layout.getChildren().add(spacer);

        Button restartBtn = createStyledButton("RESTART FROM LEVEL 1");
        restartBtn.setOnAction(e -> startGame(1));

        Button menuBtn = createStyledButton("MAIN MENU");
        menuBtn.setOnAction(e -> mainWindow.setScene(createMainMenu()));

        layout.getChildren().addAll(restartBtn, menuBtn);
        mainWindow.setScene(new Scene(layout, 880, 730));
    }

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

        Text spacer = new Text("");
        layout.getChildren().add(spacer);

        Button menuBtn = createStyledButton("MAIN MENU");
        menuBtn.setOnAction(e -> mainWindow.setScene(createMainMenu()));
        layout.getChildren().add(menuBtn);

        mainWindow.setScene(new Scene(layout, 880, 730));
    }

    private void render(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 880, 730);

        maze.renderMaze(gc);

        for (Entity e : entities) {
            e.render(gc);
        }

        //drawFogOfWar(gc);

        // Draw Pale Luna's eyes on top of darkness
        if (paleLuna != null) {
            paleLuna.renderEyes(gc);
        }

        // Warning flash when chase starts
        if (warningFlashTimer > 0) {
            gc.setFill(Color.rgb(255, 0, 0, 0.15));
            gc.fillRect(0, 0, 880, 730);
        }

        drawHUD(gc);

        // Escape room indicator — subtle green bar below HUD
        if (player.isInEscapeRoom()) {
            gc.setFill(Color.rgb(0, 80, 0, 0.25));
            gc.fillRect(0, 50, 880, 4);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}