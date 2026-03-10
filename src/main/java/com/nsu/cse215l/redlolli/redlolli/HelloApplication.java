package com.nsu.cse215l.redlolli.redlolli;

import com.nsu.cse215l.redlolli.redlolli.entities.Player;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class HelloApplication extends Application {
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private Player player;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        player = new Player(400, 300); // Start in middle

        Scene scene = new Scene(new Group(canvas), 800, 600, Color.BLACK);

        // Track key presses
        scene.setOnKeyPressed(e -> activeKeys.add(e.getCode()));
        scene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));

        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render(gc);
            }
        };

        stage.setScene(scene);
        stage.setTitle("Red Lolli - Engine Test");
        stage.show();
        gameLoop.start();
    }

    private void update() {
        if (activeKeys.contains(KeyCode.W)) player.move(0, -1);
        if (activeKeys.contains(KeyCode.S)) player.move(0, 1);
        if (activeKeys.contains(KeyCode.A)) player.move(-1, 0);
        if (activeKeys.contains(KeyCode.D)) player.move(1, 0);
    }

    private void render(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800, 600); // Clear screen
        player.render(gc); // Polymorphic call
    }

    public static void main(String[] args) {
        launch();
    }
}