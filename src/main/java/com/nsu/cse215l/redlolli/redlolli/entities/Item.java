package com.nsu.cse215l.redlolli.redlolli.entities;

import com.nsu.cse215l.redlolli.redlolli.core.Collidable;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Item extends Entity implements Collidable {
    private boolean isCollected = false;
    private boolean hasLolli;

    public Item(double x, double y, boolean hasLolli) {
        super(x, y, 16.0);
        this.hasLolli = hasLolli;
    }

    @Override
    public void update() {
    }

    @Override
    public void render(GraphicsContext gc) {
        if (isCollected) {
            // Opened chest graphic — lid flipped open
            // Chest body (darker, emptied)
            gc.setFill(Color.rgb(80, 50, 15));
            gc.fillRect(x, y + 6, size, size - 6);

            // Inner shadow (empty inside)
            gc.setFill(Color.rgb(30, 15, 5));
            gc.fillRect(x + 2, y + 8, size - 4, size - 10);

            // Lid flipped back (angled above)
            gc.setFill(Color.rgb(110, 65, 20));
            gc.fillRect(x - 1, y, size + 2, 5);
            gc.setFill(Color.rgb(90, 55, 18));
            gc.fillRect(x, y - 3, size, 4);

            // Broken lock
            gc.setFill(Color.rgb(120, 100, 20));
            gc.fillOval(x + size / 2 - 1.5, y + 5, 3, 3);

            // If it had the lolli, show a red glow inside
            if (hasLolli) {
                gc.setFill(Color.rgb(255, 0, 0, 0.4));
                gc.fillOval(x + 3, y + 8, size - 6, size - 12);
            }
            return;
        }

        // --- Closed chest ---
        // Outer glow shimmer
        gc.setFill(Color.rgb(200, 170, 50, 0.15));
        gc.fillOval(x - 3, y - 3, size + 6, size + 6);

        // Chest body with wood grain effect
        gc.setFill(Color.rgb(130, 75, 25));
        gc.fillRect(x, y + 5, size, size - 5);
        gc.setFill(Color.rgb(110, 60, 18));
        gc.fillRect(x + 2, y + 8, size - 4, 2); // wood grain line
        gc.fillRect(x + 2, y + 12, size - 4, 1);

        // Metal bands
        gc.setFill(Color.rgb(80, 80, 90));
        gc.fillRect(x, y + 5, size, 2);
        gc.fillRect(x, y + size - 2, size, 2);

        // Chest lid (rounded top)
        gc.setFill(Color.rgb(155, 95, 35));
        gc.fillRect(x - 1, y, size + 2, 6);
        gc.setFill(Color.rgb(170, 110, 45));
        gc.fillRect(x + 1, y + 1, size - 2, 3);

        // Gold lock/clasp with keyhole
        gc.setFill(Color.GOLD);
        gc.fillOval(x + size / 2 - 3, y + 5, 6, 6);
        gc.setFill(Color.rgb(80, 50, 10));
        gc.fillOval(x + size / 2 - 1, y + 7, 2, 2);

        // Mystery question mark
        gc.setFill(Color.rgb(255, 215, 0));
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 10));
        gc.fillText("?", x + size / 2 - 3, y + size - 2);
    }

    @Override
    public Rectangle2D getHitbox() {
        return new Rectangle2D(x, y, size, size);
    }

    public void collect() {
        this.isCollected = true;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public boolean hasLolli() {
        return hasLolli;
    }
}