package com.nsu.cse215l.redlolli.redlolli.entities;

import javafx.scene.canvas.GraphicsContext;

/**
 * Abstract base class for all game entities (Player, Monster, Item).
 * Provides position, size, and enforces update/render contract.
 */
public abstract class Entity {

    // ========================= FIELDS =========================

    protected double x, y;
    protected double size;

    // ========================= CONSTRUCTOR =========================

    public Entity(double x, double y, double size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    // ========================= ABSTRACT METHODS =========================

    /** Called each frame to update entity logic. */
    public abstract void update();

    /** Called each frame to render the entity on screen. */
    public abstract void render(GraphicsContext gc);

    // ========================= GETTERS =========================

    public double getX() { return x; }
    public double getY() { return y; }
}