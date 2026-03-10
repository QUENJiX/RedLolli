package com.nsu.cse215l.redlolli.redlolli.entities;

import com.nsu.cse215l.redlolli.redlolli.core.Collidable;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Item extends Entity implements Collidable {
    private boolean isCollected = false;

    public Item(double x, double y) {
        super(x, y, 15.0); // Slightly smaller than the player
    }

    @Override
    public void update() {
        // We can add a pulsing or floating animation here later
    }

    @Override
    public void render(GraphicsContext gc) {
        if (isCollected) return; // Don't draw if the player already grabbed it

        // Draw the stick
        gc.setFill(Color.WHITE);
        gc.fillRect(x + size/2 - 2, y + size/2, 4, size);

        // Draw the dark red candy
        gc.setFill(Color.DARKRED);
        gc.fillOval(x, y, size, size);
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
}