package com.nsu.cse215l.redlolli.redlolli.entities;

import com.nsu.cse215l.redlolli.redlolli.map.Maze;
import com.nsu.cse215l.redlolli.redlolli.core.Collidable;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Monster extends Entity implements Collidable {
    private double speed = 1.2; // Slowed down slightly to give the player a fighting chance
    private double pulsePhase = 0.0;
    private int currentLollis = 0;

    // AI State Variables
    private boolean isChasing = false;
    private int chaseTimer = 0;
    private int cooldownTimer = 0;
    private final double TRIGGER_RANGE = 200.0; // Distance before it spots the player

    public Monster(double x, double y) {
        super(x, y, 25.0);
    }

    // Updated signature to accept lollisCollected
    public void update(double playerX, double playerY, boolean playerIsHidden, int lollisCollected, Maze maze) {
        this.currentLollis = lollisCollected;
        pulsePhase += 0.1;

        if (lollisCollected == 0) return;

        if (cooldownTimer > 0) {
            cooldownTimer--;
            return;
        }

        double dx = playerX - this.x;
        double dy = playerY - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (playerIsHidden) {
            isChasing = false;
            return;
        }

        // TRIGGER LOGIC: Must be in range AND have a clear Line of Sight
        if (!isChasing && distance < TRIGGER_RANGE) {
            // Check if the monster can actually see the player down the hall
            boolean canSeePlayer = maze.hasLineOfSight(this.x + size/2, this.y + size/2, playerX + 10, playerY + 10);

            if (canSeePlayer) {
                isChasing = true;
                if (lollisCollected == 1) chaseTimer = 180;      // 3 seconds
                else if (lollisCollected == 2) chaseTimer = 300; // 5 seconds
                else chaseTimer = 420;                           // 7 seconds
            }
        }

        // CHASE LOGIC: Once triggered, it relentlessly uses BFS to hunt you down
        // Notice we don't check distance or Line of Sight here. It just chases!
        if (isChasing) {
            int currentC = (int) ((this.x + size/2) / Maze.TILE_SIZE);
            int currentR = (int) ((this.y + size/2) / Maze.TILE_SIZE);
            int playerC = (int) ((playerX + 10) / Maze.TILE_SIZE);
            int playerR = (int) ((playerY + 10) / Maze.TILE_SIZE);

            int[] nextTile = maze.getNextMove(currentR, currentC, playerR, playerC);

            if (nextTile != null) {
                double targetX, targetY;

                if (nextTile[0] == playerR && nextTile[1] == playerC) {
                    targetX = playerX - (size/2) + 10; // Aim for player center
                    targetY = playerY - (size/2) + 10;
                } else {
                    targetX = nextTile[1] * Maze.TILE_SIZE + (Maze.TILE_SIZE - size) / 2;
                    targetY = nextTile[0] * Maze.TILE_SIZE + (Maze.TILE_SIZE - size) / 2;
                }

                double stepDX = targetX - this.x;
                double stepDY = targetY - this.y;
                double stepDist = Math.sqrt(stepDX * stepDX + stepDY * stepDY);

                if (stepDist > 0) {
                    double moveDist = Math.min(speed, stepDist);
                    this.x += (stepDX / stepDist) * moveDist;
                    this.y += (stepDY / stepDist) * moveDist;
                }
            }

            chaseTimer--; // Tick down the clock

            // Only stop if the timer runs out
            if (chaseTimer <= 0) {
                isChasing = false;
                cooldownTimer = 180; // Rest for 3 seconds
            }
        }
    }

    @Override
    public void update() { }

    @Override
    public void render(GraphicsContext gc) {
        // If 2 or more lollis have been collected, draw the violent red aura
        if (currentLollis >= 2) {
            double pulseOffset = Math.sin(pulsePhase) * 4; // Massive, aggressive pulse
            gc.setFill(Color.rgb(139, 0, 0, 0.5)); // Semi-transparent blood red
            gc.fillOval(x - pulseOffset, y - pulseOffset, size + (pulseOffset * 2), size + (pulseOffset * 2));
        }

        // Draw the pitch-black core on top of the aura
        gc.setFill(Color.BLACK);
        gc.fillOval(x + 2, y + 2, size - 4, size - 4);
    }

    public void renderEyes(GraphicsContext gc) {
        if (currentLollis == 0) {
            // PHASE 1: IDLE - Dim, small, static eyes. They are sleeping/dormant.
            gc.setFill(Color.rgb(120, 0, 0)); // Very dark red
            gc.fillOval(x + 7, y + 9, 2, 2);
            gc.fillOval(x + 16, y + 9, 2, 2);

        } else if (!isChasing) {
            // PHASE 2: SEARCHING - Brighter, wider, standard pulse.
            gc.setFill(Color.RED);
            double pulseOffset = Math.sin(pulsePhase) * 1.5;
            double currentEyeSize = 4 + pulseOffset;

            gc.fillOval((x + 6) - (pulseOffset / 2), (y + 8) - (pulseOffset / 2), currentEyeSize, currentEyeSize);
            gc.fillOval((x + 15) - (pulseOffset / 2), (y + 8) - (pulseOffset / 2), currentEyeSize, currentEyeSize);

        } else {
            // PHASE 3: CHASING - Aggressive, fast-pulsing, massive eyes.
            gc.setFill(Color.rgb(255, 50, 50)); // Bright, angry red
            double fastPulse = Math.sin(pulsePhase * 2.5) * 2; // Pulses much faster
            double currentEyeSize = 5 + fastPulse;

            // Draw larger, slightly distorted eyes to simulate violent movement
            gc.fillOval((x + 5) - (fastPulse / 2), (y + 7) - (fastPulse / 2), currentEyeSize, currentEyeSize - 1);
            gc.fillOval((x + 14) - (fastPulse / 2), (y + 7) - (fastPulse / 2), currentEyeSize, currentEyeSize - 1);
        }
    }

    public boolean isChasing() {
        return isChasing;
    }

    @Override
    public Rectangle2D getHitbox() {
        return new Rectangle2D(x, y, size, size);
    }
}