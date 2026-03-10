package com.nsu.cse215l.redlolli.redlolli.map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Tile {
    public static final int WALL = 1;
    public static final int PATH = 0;

    private int type;
    private double x, y, size;

    public Tile(int type, double x, double y, double size) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public void render(GraphicsContext gc) {
        if (type == WALL) {
            gc.setFill(Color.DARKSLATEGRAY);
            gc.fillRect(x, y, size, size);
        } else {
            // Dark grid floor for the "minimalist" look
            gc.setStroke(Color.web("#1a1a1a"));
            gc.strokeRect(x, y, size, size);
        }
    }

    public int getType() { return type; }
}