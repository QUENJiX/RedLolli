package com.nsu.cse215l.redlolli.redlolli.entities;

import com.nsu.cse215l.redlolli.redlolli.core.Collidable;
import com.nsu.cse215l.redlolli.redlolli.map.Maze;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 * Pale Luna — the demon enemy with a three-state AI:
 * <ul>
 *   <li>IDLE: sleeping for 15 seconds, harmless</li>
 *   <li>CHASING: BFS pathfinding pursuit for 5 seconds, instant kill on contact</li>
 *   <li>WAITING_AT_DOOR: lurking outside escape room for 3 seconds</li>
 * </ul>
 * After each cycle, Pale Luna stays at her current position (does not reset to spawn).
 */
public class Monster extends Entity implements Collidable {

    // ========================= AI STATE =========================

    /** The three possible states of Pale Luna's behavior cycle. */
    public enum State { IDLE, CHASING, WAITING_AT_DOOR }

    private State state = State.IDLE;

    // ========================= CONSTANTS =========================

    private static final double SPEED = 2.0;
    private static final int IDLE_DURATION  = 900;  // 15 seconds at 60 FPS
    private static final int CHASE_DURATION = 300;  // 5 seconds
    private static final int WAIT_DURATION  = 180;  // 3 seconds

    // ========================= TIMERS =========================

    private int idleTimer = 0;
    private int chaseTimer = 0;
    private int waitTimer = 0;

    // ========================= INTERNAL STATE =========================

    private double pulsePhase = 0.0;
    private double spawnX, spawnY;

    // ========================= CONSTRUCTOR =========================

    public Monster(double x, double y) {
        super(x, y, 25.0);
        this.spawnX = x;
        this.spawnY = y;
        this.idleTimer = IDLE_DURATION;
    }

    // ========================= AI UPDATE =========================

    /** Unused parameterless update (required by Entity contract). */
    @Override
    public void update() { }

    /**
     * Main AI tick — handles state transitions, pathfinding chase, and timers.
     *
     * @param playerX            player's X position
     * @param playerY            player's Y position
     * @param playerInEscapeRoom whether the player is in a safe zone
     * @param maze               the maze for pathfinding and collision
     */
    public void update(double playerX, double playerY, boolean playerInEscapeRoom, Maze maze) {
        pulsePhase += 0.1;

        switch (state) {
            case IDLE -> {
                idleTimer--;
                if (idleTimer <= 0) {
                    state = State.CHASING;
                    chaseTimer = CHASE_DURATION;
                }
            }
            case CHASING -> {
                if (playerInEscapeRoom) {
                    state = State.WAITING_AT_DOOR;
                    waitTimer = WAIT_DURATION;
                    break;
                }
                chasePlayer(playerX, playerY, maze);
                chaseTimer--;
                if (chaseTimer <= 0) {
                    returnToIdle();
                }
            }
            case WAITING_AT_DOOR -> {
                waitTimer--;
                if (waitTimer <= 0) {
                    returnToIdle();
                }
            }
        }
    }

    /** Transitions back to IDLE, keeping current position as new origin. */
    private void returnToIdle() {
        this.spawnX = this.x;
        this.spawnY = this.y;
        state = State.IDLE;
        idleTimer = IDLE_DURATION;
    }

    /** Uses BFS pathfinding to chase the player through the maze. */
    private void chasePlayer(double playerX, double playerY, Maze maze) {
        int currentC = (int) ((this.x + size / 2) / Maze.TILE_SIZE);
        int currentR = (int) ((this.y + size / 2 - Maze.Y_OFFSET) / Maze.TILE_SIZE);
        int playerC  = (int) ((playerX + 10) / Maze.TILE_SIZE);
        int playerR  = (int) ((playerY + 10 - Maze.Y_OFFSET) / Maze.TILE_SIZE);

        int[] nextTile = maze.getNextMove(currentR, currentC, playerR, playerC);
        if (nextTile == null) return;

        double targetX, targetY;
        if (nextTile[0] == playerR && nextTile[1] == playerC) {
            targetX = playerX - (size / 2) + 10;
            targetY = playerY - (size / 2) + 10;
        } else {
            targetX = nextTile[1] * Maze.TILE_SIZE + (Maze.TILE_SIZE - size) / 2;
            targetY = nextTile[0] * Maze.TILE_SIZE + Maze.Y_OFFSET + (Maze.TILE_SIZE - size) / 2;
        }

        double stepDX = targetX - this.x;
        double stepDY = targetY - this.y;
        double stepDist = Math.sqrt(stepDX * stepDX + stepDY * stepDY);

        if (stepDist > 0) {
            double moveDist = Math.min(SPEED, stepDist);
            this.x += (stepDX / stepDist) * moveDist;
            this.y += (stepDY / stepDist) * moveDist;
        }
    }

    // ========================= RENDERING: BODY =========================

    @Override
    public void render(GraphicsContext gc) {
        if (state != State.IDLE) {
            drawAura(gc);
        }
        drawHead(gc);
        drawHair(gc);
        drawMouth(gc);
    }

    /** Draws the pulsing dark red aura when Pale Luna is active. */
    private void drawAura(GraphicsContext gc) {
        double pulseOffset = Math.sin(pulsePhase) * 5;
        gc.setFill(Color.rgb(100, 0, 0, 0.35));
        gc.fillOval(x - pulseOffset - 2, y - pulseOffset - 2,
                size + (pulseOffset + 2) * 2, size + (pulseOffset + 2) * 2);
    }

    /** Draws the pale face oval. */
    private void drawHead(GraphicsContext gc) {
        double headX = x + 2, headY = y + 1;
        double headW = size - 4, headH = size - 2;

        Color skinColor = (state == State.IDLE)
                ? Color.rgb(220, 210, 200, 0.5)
                : Color.rgb(240, 230, 220);
        gc.setFill(skinColor);
        gc.fillOval(headX, headY, headW, headH);
    }

    /** Draws the yellow hair: top arc and side strands. */
    private void drawHair(GraphicsContext gc) {
        double headX = x + 2, headY = y + 1;
        double headW = size - 4, headH = size - 2;

        Color hairColor = (state == State.IDLE)
                ? Color.rgb(180, 160, 40, 0.5)
                : Color.rgb(220, 200, 60);
        gc.setFill(hairColor);
        gc.fillArc(headX - 1, headY - 3, headW + 2, headH * 0.6, 0, 180, ArcType.ROUND);
        gc.fillOval(headX - 2, headY + 2, 5, headH - 4);
        gc.fillOval(headX + headW - 3, headY + 2, 5, headH - 4);
    }

    /** Draws the mouth expression based on current state. */
    private void drawMouth(GraphicsContext gc) {
        double headX = x + 2, headY = y + 1;
        double headW = size - 4, headH = size - 2;

        switch (state) {
            case CHASING -> {
                // Wide creepy grin with teeth
                gc.setStroke(Color.rgb(80, 0, 0));
                gc.setLineWidth(1.5);
                gc.strokeArc(headX + 4, headY + headH * 0.55, headW - 8, 6, 180, 180, ArcType.OPEN);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(0.5);
                double mouthY = headY + headH * 0.55 + 3;
                for (int i = 0; i < 4; i++) {
                    double tx = headX + 6 + i * 3;
                    gc.strokeLine(tx, mouthY - 1, tx, mouthY + 1);
                }
            }
            case WAITING_AT_DOOR -> {
                // Thin sinister smile
                gc.setStroke(Color.rgb(100, 0, 0));
                gc.setLineWidth(1);
                gc.strokeArc(headX + 5, headY + headH * 0.58, headW - 10, 4, 180, 180, ArcType.OPEN);
            }
            default -> {
                // Faint closed mouth (idle/sleeping)
                gc.setStroke(Color.rgb(150, 120, 120, 0.4));
                gc.setLineWidth(0.8);
                gc.strokeLine(headX + 7, headY + headH * 0.65, headX + headW - 7, headY + headH * 0.65);
            }
        }
    }

    // ========================= RENDERING: EYES =========================

    /** Renders Pale Luna's eyes as a separate layer (drawn on top of all entities). */
    public void renderEyes(GraphicsContext gc) {
        double headX = x + 2, headY = y + 1;
        double headW = size - 4, headH = size - 2;
        double eyeY = headY + headH * 0.35;
        double leftEyeX = headX + headW * 0.22;
        double rightEyeX = headX + headW * 0.58;

        switch (state) {
            case IDLE -> drawIdleEyes(gc, leftEyeX, rightEyeX, eyeY);
            case CHASING -> drawChasingEyes(gc, leftEyeX, rightEyeX, eyeY);
            case WAITING_AT_DOOR -> drawWaitingEyes(gc, leftEyeX, rightEyeX, eyeY);
        }
    }

    /** Dim, half-closed red eyes when sleeping. */
    private void drawIdleEyes(GraphicsContext gc, double leftX, double rightX, double eyeY) {
        gc.setFill(Color.rgb(150, 0, 0, 0.5));
        gc.fillOval(leftX, eyeY + 1, 4, 2);
        gc.fillOval(rightX, eyeY + 1, 4, 2);
    }

    /** Intense, fast-pulsing, glowing red eyes during chase. */
    private void drawChasingEyes(GraphicsContext gc, double leftX, double rightX, double eyeY) {
        double fastPulse = Math.sin(pulsePhase * 3) * 1.5;
        double eyeSize = 5 + fastPulse;

        // Outer glow
        gc.setFill(Color.rgb(255, 0, 0, 0.3));
        gc.fillOval(leftX - 3, eyeY - 3, eyeSize + 6, eyeSize + 5);
        gc.fillOval(rightX - 3, eyeY - 3, eyeSize + 6, eyeSize + 5);

        // Dark eye socket
        gc.setFill(Color.rgb(50, 0, 0));
        gc.fillOval(leftX - 1, eyeY - 1, eyeSize + 2, eyeSize);
        gc.fillOval(rightX - 1, eyeY - 1, eyeSize + 2, eyeSize);

        // Bright red iris
        gc.setFill(Color.rgb(255, 20, 20));
        gc.fillOval(leftX, eyeY, eyeSize, eyeSize - 1);
        gc.fillOval(rightX, eyeY, eyeSize, eyeSize - 1);

        // Hot white center
        gc.setFill(Color.rgb(255, 200, 200));
        gc.fillOval(leftX + 1.5, eyeY + 1, 2, 1.5);
        gc.fillOval(rightX + 1.5, eyeY + 1, 2, 1.5);
    }

    /** Glowing red eyes with slow pulse while waiting at door. */
    private void drawWaitingEyes(GraphicsContext gc, double leftX, double rightX, double eyeY) {
        double pulse = Math.sin(pulsePhase * 0.8) * 0.5;
        double eyeSize = 4 + pulse;

        // Eye socket
        gc.setFill(Color.rgb(40, 0, 0));
        gc.fillOval(leftX - 1, eyeY - 1, eyeSize + 2, eyeSize + 1);
        gc.fillOval(rightX - 1, eyeY - 1, eyeSize + 2, eyeSize + 1);

        // Red iris
        gc.setFill(Color.rgb(220, 0, 0));
        gc.fillOval(leftX, eyeY, eyeSize, eyeSize - 1);
        gc.fillOval(rightX, eyeY, eyeSize, eyeSize - 1);

        // Bright pupil center
        gc.setFill(Color.rgb(255, 100, 100));
        gc.fillOval(leftX + 1, eyeY + 0.5, 2, 1.5);
        gc.fillOval(rightX + 1, eyeY + 0.5, 2, 1.5);
    }

    // ========================= COLLISION =========================

    @Override
    public Rectangle2D getHitbox() {
        return new Rectangle2D(x, y, size, size);
    }

    // ========================= STATE QUERIES =========================

    public State getState()      { return state; }
    public boolean isChasing()       { return state == State.CHASING; }
    public boolean isWaitingAtDoor() { return state == State.WAITING_AT_DOOR; }
    public boolean isDangerous()     { return state == State.CHASING || state == State.WAITING_AT_DOOR; }

    // ========================= TIMER GETTERS =========================

    public int getIdleTimer()  { return idleTimer; }
    public int getChaseTimer() { return chaseTimer; }
    public int getWaitTimer()  { return waitTimer; }
}