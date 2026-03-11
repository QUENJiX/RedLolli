package com.nsu.cse215l.redlolli.redlolli.entities;

import com.nsu.cse215l.redlolli.redlolli.core.Collidable;
import com.nsu.cse215l.redlolli.redlolli.map.Maze;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 * The player character entity.
 * Handles movement with wall collision, and renders a detailed expressive sprite
 * that changes appearance based on chase state and escape room presence.
 */
public class Player extends Entity implements Collidable {

    // ========================= CONSTANTS =========================

    private static final double SPEED = 3.0;

    // ========================= STATE FIELDS =========================

    private boolean isBeingChased = false;
    private boolean isInEscapeRoom = false;

    // ========================= CONSTRUCTOR =========================

    public Player(double x, double y) {
        super(x, y, 20.0);
    }

    // ========================= GAME LOGIC =========================

    @Override
    public void update() {
        // Player update logic is driven by input in HelloApplication
    }

    /** Moves the player in the given direction if no wall collision occurs. */
    public void move(double dx, double dy, Maze maze) {
        double nextX = this.x + (dx * SPEED);
        double nextY = this.y + (dy * SPEED);
        Rectangle2D nextHitbox = new Rectangle2D(nextX, nextY, size, size);

        if (!maze.isWallCollision(nextHitbox)) {
            this.x = nextX;
            this.y = nextY;
        }
    }

    // ========================= RENDERING =========================

    @Override
    public void render(GraphicsContext gc) {
        drawAura(gc);
        drawBody(gc);
        if (!isBeingChased) {
            drawCalmFace(gc);
        } else {
            drawPanicFace(gc);
        }
    }

    /** Draws the soft glow aura around the player (color depends on state). */
    private void drawAura(GraphicsContext gc) {
        if (isInEscapeRoom) {
            gc.setFill(Color.rgb(0, 180, 0, 0.15));
        } else if (isBeingChased) {
            gc.setFill(Color.rgb(255, 0, 0, 0.12));
        } else {
            gc.setFill(Color.rgb(200, 200, 255, 0.1));
        }
        gc.fillOval(x - 4, y - 4, size + 8, size + 8);
    }

    /** Draws the player body circle with outline, fill layers, and rosy cheeks. */
    private void drawBody(GraphicsContext gc) {
        // Outline
        gc.setStroke(isBeingChased ? Color.RED : Color.rgb(100, 80, 120));
        gc.setLineWidth(2);
        gc.strokeOval(x, y, size, size);

        // Body fill with layered gradient effect
        gc.setFill(Color.rgb(240, 235, 230));
        gc.fillOval(x, y, size, size);
        gc.setFill(Color.rgb(255, 250, 245));
        gc.fillOval(x + 2, y + 1, size - 4, size - 3);

        // Rosy cheeks
        gc.setFill(Color.rgb(255, 150, 150, 0.35));
        gc.fillOval(x + 1, y + 10, 5, 3);
        gc.fillOval(x + 14, y + 10, 5, 3);
    }

    /** Draws the calm expression: round eyes with iris detail and a small smile. */
    private void drawCalmFace(GraphicsContext gc) {
        // Eye whites
        gc.setFill(Color.WHITE);
        gc.fillOval(x + 4, y + 4, 5, 7);
        gc.fillOval(x + 11, y + 4, 5, 7);

        // Iris
        gc.setFill(Color.rgb(30, 30, 60));
        gc.fillOval(x + 5, y + 5, 3.5, 5);
        gc.fillOval(x + 12, y + 5, 3.5, 5);

        // Pupil
        gc.setFill(Color.BLACK);
        gc.fillOval(x + 5.5, y + 6, 2, 3);
        gc.fillOval(x + 12.5, y + 6, 2, 3);

        // Catchlight sparkle
        gc.setFill(Color.WHITE);
        gc.fillOval(x + 5.5, y + 5.5, 1.5, 1.5);
        gc.fillOval(x + 12.5, y + 5.5, 1.5, 1.5);

        // Smile
        gc.setStroke(Color.rgb(120, 80, 80));
        gc.setLineWidth(1);
        gc.strokeArc(x + 6, y + 12, 8, 4, 180, 180, ArcType.OPEN);
    }

    /** Draws the panic expression: wide trembling eyes and open shocked mouth. */
    private void drawPanicFace(GraphicsContext gc) {
        double trembleX = (Math.random() * 2) - 1;
        double trembleY = (Math.random() * 2) - 1;

        // Wide eye whites
        gc.setFill(Color.WHITE);
        gc.fillOval(x + 3 + trembleX, y + 3 + trembleY, 6, 8);
        gc.fillOval(x + 11 + trembleX, y + 3 + trembleY, 6, 8);

        // Shrunken pupils
        gc.setFill(Color.BLACK);
        gc.fillOval(x + 5 + trembleX, y + 6 + trembleY, 2.5, 2.5);
        gc.fillOval(x + 13 + trembleX, y + 6 + trembleY, 2.5, 2.5);

        // Fear sparkle
        gc.setFill(Color.rgb(255, 255, 255, 0.8));
        gc.fillOval(x + 5 + trembleX, y + 5.5 + trembleY, 1, 1);
        gc.fillOval(x + 13 + trembleX, y + 5.5 + trembleY, 1, 1);

        // Open mouth (shock)
        gc.setFill(Color.rgb(60, 30, 30));
        gc.fillOval(x + 7.5, y + 13, 5, 4);
    }

    // ========================= COLLISION =========================

    @Override
    public Rectangle2D getHitbox() {
        return new Rectangle2D(x, y, size, size);
    }

    // ========================= GETTERS & SETTERS =========================

    public double getSize() { return size; }

    public void setBeingChased(boolean chased) { this.isBeingChased = chased; }

    public boolean isInEscapeRoom() { return isInEscapeRoom; }

    public void setInEscapeRoom(boolean inEscapeRoom) { this.isInEscapeRoom = inEscapeRoom; }
}