package com.nsu.cse215l.redlolli.redlolli.entities;

import com.nsu.cse215l.redlolli.redlolli.core.Collidable;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * A treasure chest entity that may contain the Red Lolli.
 * Renders differently when closed vs. opened, and shows a red glow if it held the lolli.
 */
public class Item extends Entity implements Collidable {

    // ========================= FIELDS =========================

    private boolean isCollected = false;
    private final boolean hasLolli;

    // ========================= CONSTRUCTOR =========================

    public Item(double x, double y, boolean hasLolli) {
        super(x, y, 16.0);
        this.hasLolli = hasLolli;
    }

    // ========================= GAME LOGIC =========================

    @Override
    public void update() {
        // Chests are static — no per-frame logic
    }

    /** Marks this chest as opened/collected. */
    public void collect() { this.isCollected = true; }

    // ========================= RENDERING =========================

    @Override
    public void render(GraphicsContext gc) {
        if (isCollected) {
            renderOpenedChest(gc);
        } else {
            renderClosedChest(gc);
        }
    }

    /** Renders an opened chest with lid flipped back and optional red glow for lolli. */
    private void renderOpenedChest(GraphicsContext gc) {
        // Chest body
        gc.setFill(Color.rgb(80, 50, 15));
        gc.fillRect(x, y + 6, size, size - 6);

        // Inner shadow
        gc.setFill(Color.rgb(30, 15, 5));
        gc.fillRect(x + 2, y + 8, size - 4, size - 10);

        // Flipped lid
        gc.setFill(Color.rgb(110, 65, 20));
        gc.fillRect(x - 1, y, size + 2, 5);
        gc.setFill(Color.rgb(90, 55, 18));
        gc.fillRect(x, y - 3, size, 4);

        // Broken lock
        gc.setFill(Color.rgb(120, 100, 20));
        gc.fillOval(x + size / 2 - 1.5, y + 5, 3, 3);

        // Red glow if it contained the lolli
        if (hasLolli) {
            gc.setFill(Color.rgb(255, 0, 0, 0.4));
            gc.fillOval(x + 3, y + 8, size - 6, size - 12);
        }
    }

    /** Renders a closed chest with wood grain, metal bands, lock, and mystery "?" mark. */
    private void renderClosedChest(GraphicsContext gc) {
        // Outer glow shimmer
        gc.setFill(Color.rgb(200, 170, 50, 0.15));
        gc.fillOval(x - 3, y - 3, size + 6, size + 6);

        // Chest body with wood grain
        gc.setFill(Color.rgb(130, 75, 25));
        gc.fillRect(x, y + 5, size, size - 5);
        gc.setFill(Color.rgb(110, 60, 18));
        gc.fillRect(x + 2, y + 8, size - 4, 2);
        gc.fillRect(x + 2, y + 12, size - 4, 1);

        // Metal bands
        gc.setFill(Color.rgb(80, 80, 90));
        gc.fillRect(x, y + 5, size, 2);
        gc.fillRect(x, y + size - 2, size, 2);

        // Chest lid
        gc.setFill(Color.rgb(155, 95, 35));
        gc.fillRect(x - 1, y, size + 2, 6);
        gc.setFill(Color.rgb(170, 110, 45));
        gc.fillRect(x + 1, y + 1, size - 2, 3);

        // Gold lock with keyhole
        gc.setFill(Color.GOLD);
        gc.fillOval(x + size / 2 - 3, y + 5, 6, 6);
        gc.setFill(Color.rgb(80, 50, 10));
        gc.fillOval(x + size / 2 - 1, y + 7, 2, 2);

        // Mystery question mark
        gc.setFill(Color.rgb(255, 215, 0));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.fillText("?", x + size / 2 - 3, y + size - 2);
    }

    // ========================= COLLISION =========================

    @Override
    public Rectangle2D getHitbox() {
        return new Rectangle2D(x, y, size, size);
    }

    // ========================= GETTERS =========================

    public boolean isCollected() { return isCollected; }
    public boolean hasLolli()    { return hasLolli; }
}