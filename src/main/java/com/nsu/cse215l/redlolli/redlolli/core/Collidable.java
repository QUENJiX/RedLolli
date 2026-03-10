package com.nsu.cse215l.redlolli.redlolli.core;

import javafx.geometry.Rectangle2D;

public interface Collidable {
    // Returns the bounding box for collision detection
    Rectangle2D getHitbox();
}