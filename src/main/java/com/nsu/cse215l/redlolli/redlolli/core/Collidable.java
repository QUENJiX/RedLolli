package com.nsu.cse215l.redlolli.redlolli.core;

import javafx.geometry.Rectangle2D;

/**
 * Interface for entities that participate in collision detection.
 * Implemented by Player, Monster, and Item.
 */
public interface Collidable {

    /** Returns the axis-aligned bounding box used for collision checks. */
    Rectangle2D getHitbox();
}