package com.nsu.cse215l.redlolli.redlolli.entities;

import com.nsu.cse215l.redlolli.redlolli.core.Collidable;
import com.nsu.cse215l.redlolli.redlolli.map.Maze;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Player extends Entity implements Collidable {
    private double speed = 3.0;
    private double sanity = 100.0;
    private final double MAX_SANITY = 100.0;
    private final double SANITY_DRAIN_RATE = 0.05; // Adjust this to make the game harder/easier
    private boolean isBeingChased = false;

    public Player(double x, double y) {
        super(x, y, 20.0);
    }

    @Override
    public void update() {
    }

    @Override
    public void render(GraphicsContext gc) {
        // Outline & Core
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(3);
        gc.strokeOval(x, y, size, size);

        gc.setFill(Color.WHITE);
        gc.fillOval(x, y, size, size);

        if (!isBeingChased) {
            // PHASE 1: CALM & CUTE - Tall oval eyes with a tiny white gleam
            gc.setFill(Color.rgb(20, 20, 20)); // Pitch black eyes
            gc.fillOval(x + 5, y + 5, 4, 6);  // Left eye (taller)
            gc.fillOval(x + 11, y + 5, 4, 6); // Right eye

            gc.setFill(Color.WHITE); // The cute catchlight / gleam
            gc.fillOval(x + 6, y + 6, 1.5, 1.5);
            gc.fillOval(x + 12, y + 6, 1.5, 1.5);
        } else {
            // PHASE 2: PANIC - The cute eyes widen and violently tremble!
            double trembleX = (Math.random() * 1.5) - 0.75;
            double trembleY = (Math.random() * 1.5) - 0.75;

            gc.setFill(Color.rgb(20, 20, 20));
            gc.fillOval(x + 4 + trembleX, y + 4 + trembleY, 5, 7);
            gc.fillOval(x + 11 + trembleX, y + 4 + trembleY, 5, 7);

            gc.setFill(Color.WHITE); // Trembling gleam
            gc.fillOval(x + 5.5 + trembleX, y + 5 + trembleY, 2, 2);
            gc.fillOval(x + 12.5 + trembleX, y + 5 + trembleY, 2, 2);
        }
    }

    @Override
    public Rectangle2D getHitbox() {
        return new Rectangle2D(x, y, size, size);
    }

    // Updated move method with collision detection
    public void move(double dx, double dy, Maze maze) {
        // Calculate where the player is trying to go
        double nextX = this.x + (dx * speed);
        double nextY = this.y + (dy * speed);

        // Create a temporary hitbox for the future position
        Rectangle2D nextHitbox = new Rectangle2D(nextX, nextY, size, size);

        // Only update actual coordinates if the path is clear
        if (!maze.isWallCollision(nextHitbox)) {
            this.x = nextX;
            this.y = nextY;
        }
    }

    public void dropSanity(double amount) {
        this.sanity -= amount;
        if (this.sanity < 0) {
            this.sanity = 0;
        }
    }

    // Getters and Setters for Sanity
    public double getSanity() {
        return sanity;
    }public double getSize() {
        return size;
    }
    public void setBeingChased(boolean chased) {
        this.isBeingChased = chased;
    }

    public void restoreSanity(double amount) {
        this.sanity += amount;
        if (this.sanity > MAX_SANITY) {
            this.sanity = MAX_SANITY;
        }
    }
}