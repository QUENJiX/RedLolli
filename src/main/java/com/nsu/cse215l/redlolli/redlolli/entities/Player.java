package com.nsu.cse215l.redlolli.redlolli.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Player extends Entity {
    private double speed = 3.0;

    public Player(double x, double y) {
        super(x, y, 20.0); // Size is 20 pixels
    }

    @Override
    public void update() {
        // We will add keyboard logic here next
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillOval(x, y, size, size); // Draw the player as a circle
    }

    public void move(double dx, double dy) {
        this.x += dx * speed;
        this.y += dy * speed;
    }
}