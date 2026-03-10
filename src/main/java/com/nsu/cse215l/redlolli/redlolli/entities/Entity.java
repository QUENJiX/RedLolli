package com.nsu.cse215l.redlolli.redlolli.entities;

import javafx.scene.canvas.GraphicsContext;

public abstract class Entity {
    protected double x, y;
    protected double size;

    public Entity(double x, double y, double size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    // Abstract methods: Every entity MUST have these but implements them differently
    public abstract void update();
    public abstract void render(GraphicsContext gc);

    // Getters and Setters (Encapsulation)
    public double getX() { return x; }
    public double getY() { return y; }
}