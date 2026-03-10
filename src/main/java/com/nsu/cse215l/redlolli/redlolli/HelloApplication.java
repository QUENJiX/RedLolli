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
import com.nsu.cse215l.redlolli.redlolli.map.Maze;
import javafx.scene.shape.FillRule;
import com.nsu.cse215l.redlolli.redlolli.entities.Monster;

import java.util.HashSet;
import java.util.Set;

public class HelloApplication extends Application {
    private Stage mainWindow;
    private AnimationTimer gameLoop;
    private boolean isPlaying = false;
    private long survivalFrames = 0; // Tracks time for the Game Over screen

    private final Set<KeyCode> activeKeys = new HashSet<>();
    private Player player;
    private Maze maze;

    private List<Entity> entities = new ArrayList<>();
    private List<Item> lollis = new ArrayList<>();
    private List<Monster> monsters = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        this.mainWindow = stage;

        // Boot directly into the Main Menu
        Scene menuScene = createMainMenu();

        mainWindow.setScene(menuScene);
        mainWindow.setTitle("Red Lolli");
        mainWindow.show();
    }

    private Scene createMainMenu() {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        Text title = new Text("RED LOLLI");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        title.setFill(Color.DARKRED);

        Button newGameBtn = createStyledButton("> NEW GAME");
        Button loadGameBtn = createStyledButton("LOAD GAME");
        Button highScoresBtn = createStyledButton("HIGH SCORES");
        Button exitBtn = createStyledButton("EXIT");

        // Wire up the buttons
        newGameBtn.setOnAction(e -> startNewGame());
        exitBtn.setOnAction(e -> System.exit(0));

        layout.getChildren().addAll(title, newGameBtn, loadGameBtn, highScoresBtn, exitBtn);
        return new Scene(layout, 800, 600);
    }

    // Helper method to keep your minimalist, dark aesthetic [cite: 68]
    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        btn.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-border-color: darkred; -fx-border-width: 1px; -fx-padding: 10 40;");

        // Simple hover effect
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #330000; -fx-text-fill: white; -fx-border-color: red; -fx-border-width: 1px; -fx-padding: 10 40;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-border-color: darkred; -fx-border-width: 1px; -fx-padding: 10 40;"));
        return btn;
    }

    private void startNewGame() {
        // 1. Reset everything for a fresh run
        entities.clear();
        lollis.clear();
        monsters.clear();
        activeKeys.clear();
        survivalFrames = 0;
        isPlaying = true;

        // 2. Initialize the game objects
        maze = new Maze("/map.csv");
        player = new Player(60, 60);
        entities.add(player);
        spawnEntities();

        // 3. Setup the Canvas and Scene
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Scene gameScene = new Scene(new Group(canvas), 800, 600, Color.BLACK);

        gameScene.setOnKeyPressed(e -> activeKeys.add(e.getCode()));
        gameScene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));

        // 4. Start the game loop
        if (gameLoop != null) gameLoop.stop();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isPlaying) {
                    survivalFrames++; // Count frames for the survival timer
                    update();
                    render(gc);
                }
            }
        };
        gameLoop.start();

        // 5. Swap the window to the active game
        mainWindow.setScene(gameScene);
    }

    private void spawnEntities() {
        int[][] grid = maze.getMapGrid();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {

                if (grid[row][col] == 2) {
                    Item lolli = new Item(col * Maze.TILE_SIZE + 12, row * Maze.TILE_SIZE + 12);
                    lollis.add(lolli);
                    entities.add(lolli); // Allows polymorphic render() [cite: 28]
                    grid[row][col] = 0;
                }

                else if (grid[row][col] == 4) {
                    Monster stalker = new Monster(col * Maze.TILE_SIZE + 7.5, row * Maze.TILE_SIZE + 7.5);

                    // We add it to 'monsters' so the update() loop can run its AI math
                    monsters.add(stalker);

                    // We add it to 'entities' so the render() loop draws its body [cite: 28, 33]
                    entities.add(stalker);

                    grid[row][col] = 0;
                }
            }
        }
    }

    private void drawFogOfWar(GraphicsContext gc) {
        // Find the center of the player
        double playerCX = player.getX() + (player.getSize() / 2);
        double playerCY = player.getY() + (player.getSize() / 2);

        // Calculate the dynamically shrinking radius [cite: 17]
        // Max radius = 180px, Min radius = 40px (when Sanity is 0)
        double visionRadius = 40 + (player.getSanity() / 100.0) * 140;

        gc.save(); // Save the current graphics state

        gc.beginPath();

        // 1. Draw the outer bounds (covering the whole 800x600 screen)
        gc.rect(0, 0, 800, 600);

        // 2. Draw the inner cutout circle around the player
        // arc(centerX, centerY, radiusX, radiusY, startAngle, arcExtent)
        gc.arc(playerCX, playerCY, visionRadius, visionRadius, 0, 360);

        // 3. Apply the Even-Odd rule to cut the hole, and fill the rest with black
        gc.setFillRule(FillRule.EVEN_ODD);
        gc.setFill(Color.BLACK);
        gc.fill();

        gc.restore(); // Restore state so we don't accidentally apply Even-Odd to other things
    }

    private void drawHUD(GraphicsContext gc) {
        // 1. HUD Background & Border
        gc.setFill(Color.rgb(25, 30, 35)); // Dark slate background
        gc.fillRect(0, 0, 800, 50); // Covers the top 50 pixels

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeLine(0, 50, 800, 50); // White separator line

        // 2. Setup Font
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));

        // 3. Sanity Bar
        gc.fillText("SANITY:", 20, 32);

        gc.setStroke(Color.GRAY);
        gc.strokeRect(100, 15, 150, 20); // Bar outline

        double currentSanity = player.getSanity();
        if (currentSanity > 40) {
            gc.setFill(Color.LIMEGREEN); // Calm State
        } else {
            gc.setFill(Color.DARKRED);   // Panic State
        }
        // Fill the bar proportionally based on sanity (out of 100)
        gc.fillRect(101, 16, (currentSanity / 100.0) * 148, 18);

        // 4. Lollis Counter
        long collected = lollis.stream().filter(Item::isCollected).count();
        gc.setFill(Color.WHITE);
        gc.fillText("LOLLIS: [" + collected + "/3]", 300, 32);

        // 5. Dynamic Status Text
        if (currentSanity > 40) {
            gc.setFill(Color.LIGHTGRAY);
            gc.fillText("STATUS: CALM", 550, 32);
        } else {
            gc.setFill(Color.DARKRED);
            gc.fillText("STATUS: CRITICAL", 550, 32);
        }
    }

    private void update() {
        player.update();
        //1. Calculate how many lollis we have collected
        int collectedCount = (int) lollis.stream().filter(Item::isCollected).count();
        boolean isHidden = maze.isHidingSpot(player.getHitbox());
        boolean anyMonsterChasing = false;

        if (activeKeys.contains(KeyCode.W)) player.move(0, -1, maze);
        if (activeKeys.contains(KeyCode.S)) player.move(0, 1, maze);
        if (activeKeys.contains(KeyCode.A)) player.move(-1, 0, maze);
        if (activeKeys.contains(KeyCode.D)) player.move(1, 0, maze);

        // Check for collisions between player and uncollected lollis
        for (Item lolli : lollis) {
            if (!lolli.isCollected() && player.getHitbox().intersects(lolli.getHitbox())) {
                lolli.collect();

                // Drop sanity by 30 points per Lolli collected
                player.dropSanity(30.0);
                System.out.println("Cursed Sugar! Vision shrinking. Sanity: " + player.getSanity());
            }
        }


        //2. Pass the count to the monsters
        for (Monster m : monsters) {
            m.update(player.getX(), player.getY(), isHidden, collectedCount, maze);

            // Check if THIS specific monster is actively chasing the player
            if (m.isChasing()) {
                anyMonsterChasing = true;
            }

            if (!isHidden && player.getHitbox().intersects(m.getHitbox())) {
                // Remove the System.out.println and call the new method
                triggerGameOver();
            }
        }
        // Pass the final result to the player so they can update their expression!
        player.setBeingChased(anyMonsterChasing);
    }

    private void triggerGameOver() {
        isPlaying = false;
        gameLoop.stop();

        // Calculate stats
        int collectedCount = (int) lollis.stream().filter(Item::isCollected).count();
        int secondsSurvived = (int) (survivalFrames / 60); // Assuming 60 FPS
        String timeString = String.format("%02d:%02d", secondsSurvived / 60, secondsSurvived % 60);

        // Build the Game Over layout
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        Text title = new Text("YOU DIED");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 80));
        title.setFill(Color.DARKRED);

        Text loreSubtitle = new Text("The sugar rush ended. The shadows consumed you.");
        loreSubtitle.setFont(Font.font("Arial", 20));
        loreSubtitle.setFill(Color.LIGHTGRAY);

        Text stats = new Text("Lollis Collected: " + collectedCount + "/3   |   Time Survived: " + timeString);
        stats.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        stats.setFill(Color.WHITE);

        Button restartBtn = createStyledButton("RESTART CURRENT MAZE");
        restartBtn.setOnAction(e -> startNewGame());

        layout.getChildren().addAll(title, loreSubtitle, stats, new Text(""), restartBtn);

        // Swap the window to the death screen
        mainWindow.setScene(new Scene(layout, 800, 600));
    }

    private void render(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800, 600);

        maze.renderMaze(gc);

        for (Entity e : entities) {
            e.render(gc); // Draws Player, Items, and the Monster's BODY
        }

        //drawFogOfWar(gc); // Draws the pitch black mask

        // Requirement 3: Draw the glowing eyes ON TOP of the darkness
        for (Monster m : monsters) {
            m.renderEyes(gc);
        }

        drawHUD(gc); // HUD goes on the very top layer
    }

    public static void main(String[] args) {
        launch();
    }
}