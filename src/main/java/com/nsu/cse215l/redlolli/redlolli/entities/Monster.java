package com.nsu.cse215l.redlolli.redlolli.entities;

import com.nsu.cse215l.redlolli.redlolli.map.Maze;
import com.nsu.cse215l.redlolli.redlolli.core.Collidable;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class Monster extends Entity implements Collidable {
    private double speed = 2.0;
    private double pulsePhase = 0.0;

    // AI state machine
    public enum State { IDLE, CHASING, WAITING_AT_DOOR }
    private State state = State.IDLE;

    // Timing (in frames at ~60 FPS)
    private int idleTimer = 0;
    private int chaseTimer = 0;
    private int waitTimer = 0;

    private static final int IDLE_DURATION = 900;    // 15 seconds
    private static final int CHASE_DURATION = 300;    // 5 seconds
    private static final int WAIT_DURATION = 180;     // 3 seconds

    private double spawnX, spawnY;

    public Monster(double x, double y) {
        super(x, y, 25.0);
        this.spawnX = x;
        this.spawnY = y;
        this.idleTimer = IDLE_DURATION;
    }

    public void update(double playerX, double playerY, boolean playerInEscapeRoom, Maze maze) {
        pulsePhase += 0.1;

        switch (state) {
            case IDLE:
                idleTimer--;
                if (idleTimer <= 0) {
                    state = State.CHASING;
                    chaseTimer = CHASE_DURATION;
                    speed = 2.0;
                }
                break;

            case CHASING:
                if (playerInEscapeRoom) {
                    // Player reached escape room — switch to waiting at door
                    state = State.WAITING_AT_DOOR;
                    waitTimer = WAIT_DURATION;
                    break;
                }

                // BFS pathfinding chase
                chasePlayer(playerX, playerY, maze);

                chaseTimer--;
                if (chaseTimer <= 0) {
                    // Chase ended, return to idle
                    returnToSpawn();
                    state = State.IDLE;
                    idleTimer = IDLE_DURATION;
                }
                break;

            case WAITING_AT_DOOR:
                waitTimer--;
                if (waitTimer <= 0) {
                    // Done waiting, go back to idle
                    returnToSpawn();
                    state = State.IDLE;
                    idleTimer = IDLE_DURATION;
                }
                break;
        }
    }

    private void chasePlayer(double playerX, double playerY, Maze maze) {
        int currentC = (int) ((this.x + size / 2) / Maze.TILE_SIZE);
        int currentR = (int) ((this.y + size / 2 - Maze.Y_OFFSET) / Maze.TILE_SIZE);
        int playerC = (int) ((playerX + 10) / Maze.TILE_SIZE);
        int playerR = (int) ((playerY + 10 - Maze.Y_OFFSET) / Maze.TILE_SIZE);

        int[] nextTile = maze.getNextMove(currentR, currentC, playerR, playerC);

        if (nextTile != null) {
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
                double moveDist = Math.min(speed, stepDist);
                this.x += (stepDX / stepDist) * moveDist;
                this.y += (stepDY / stepDist) * moveDist;
            }
        }
    }

    private void returnToSpawn() {
        // Current position becomes the new origin
        this.spawnX = this.x;
        this.spawnY = this.y;
    }

    @Override
    public void update() { }

    @Override
    public void render(GraphicsContext gc) {
        double cx = x + size / 2;
        double cy = y + size / 2;

        if (state != State.IDLE) {
            // Dark red aura when active
            double pulseOffset = Math.sin(pulsePhase) * 5;
            gc.setFill(Color.rgb(100, 0, 0, 0.35));
            gc.fillOval(x - pulseOffset - 2, y - pulseOffset - 2, size + (pulseOffset + 2) * 2, size + (pulseOffset + 2) * 2);
        }

        // --- Pale face (head) ---
        double headW = size - 4;
        double headH = size - 2;
        double headX = x + 2;
        double headY = y + 1;

        // Pale skin
        Color skinColor = (state == State.IDLE) ? Color.rgb(220, 210, 200, 0.5) : Color.rgb(240, 230, 220);
        gc.setFill(skinColor);
        gc.fillOval(headX, headY, headW, headH);

        // --- Yellow hair ---
        Color hairColor = (state == State.IDLE) ? Color.rgb(180, 160, 40, 0.5) : Color.rgb(220, 200, 60);
        gc.setFill(hairColor);
        // Hair top
        gc.fillArc(headX - 1, headY - 3, headW + 2, headH * 0.6, 0, 180, javafx.scene.shape.ArcType.ROUND);
        // Hair sides (left strand)
        gc.fillOval(headX - 2, headY + 2, 5, headH - 4);
        // Hair sides (right strand)
        gc.fillOval(headX + headW - 3, headY + 2, 5, headH - 4);

        // --- Mouth ---
        if (state == State.CHASING) {
            // Wide creepy grin
            gc.setStroke(Color.rgb(80, 0, 0));
            gc.setLineWidth(1.5);
            gc.strokeArc(headX + 4, headY + headH * 0.55, headW - 8, 6, 180, 180, javafx.scene.shape.ArcType.OPEN);
            // Teeth hints
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(0.5);
            double mouthY = headY + headH * 0.55 + 3;
            for (int i = 0; i < 4; i++) {
                double tx = headX + 6 + i * 3;
                gc.strokeLine(tx, mouthY - 1, tx, mouthY + 1);
            }
        } else if (state == State.WAITING_AT_DOOR) {
            // Thin sinister smile
            gc.setStroke(Color.rgb(100, 0, 0));
            gc.setLineWidth(1);
            gc.strokeArc(headX + 5, headY + headH * 0.58, headW - 10, 4, 180, 180, javafx.scene.shape.ArcType.OPEN);
        } else {
            // Faint closed mouth
            gc.setStroke(Color.rgb(150, 120, 120, 0.4));
            gc.setLineWidth(0.8);
            gc.strokeLine(headX + 7, headY + headH * 0.65, headX + headW - 7, headY + headH * 0.65);
        }
    }

    public void renderEyes(GraphicsContext gc) {
        double headX = x + 2;
        double headY = y + 1;
        double headW = size - 4;
        double headH = size - 2;

        double eyeY = headY + headH * 0.35;
        double leftEyeX = headX + headW * 0.22;
        double rightEyeX = headX + headW * 0.58;

        if (state == State.IDLE) {
            // Dim, half-closed red eyes
            gc.setFill(Color.rgb(150, 0, 0, 0.5));
            gc.fillOval(leftEyeX, eyeY + 1, 4, 2);
            gc.fillOval(rightEyeX, eyeY + 1, 4, 2);
        } else if (state == State.WAITING_AT_DOOR) {
            // Glowing red eyes, slow pulse
            double pulse = Math.sin(pulsePhase * 0.8) * 0.5;
            double eyeSize = 4 + pulse;

            // Eye whites
            gc.setFill(Color.rgb(40, 0, 0));
            gc.fillOval(leftEyeX - 1, eyeY - 1, eyeSize + 2, eyeSize + 1);
            gc.fillOval(rightEyeX - 1, eyeY - 1, eyeSize + 2, eyeSize + 1);

            // Red iris
            gc.setFill(Color.rgb(220, 0, 0));
            gc.fillOval(leftEyeX, eyeY, eyeSize, eyeSize - 1);
            gc.fillOval(rightEyeX, eyeY, eyeSize, eyeSize - 1);

            // Bright pupil center
            gc.setFill(Color.rgb(255, 100, 100));
            gc.fillOval(leftEyeX + 1, eyeY + 0.5, 2, 1.5);
            gc.fillOval(rightEyeX + 1, eyeY + 0.5, 2, 1.5);
        } else {
            // CHASING — Intense, fast-pulsing, glowing red eyes
            double fastPulse = Math.sin(pulsePhase * 3) * 1.5;
            double eyeSize = 5 + fastPulse;

            // Outer glow
            gc.setFill(Color.rgb(255, 0, 0, 0.3));
            gc.fillOval(leftEyeX - 3, eyeY - 3, eyeSize + 6, eyeSize + 5);
            gc.fillOval(rightEyeX - 3, eyeY - 3, eyeSize + 6, eyeSize + 5);

            // Eye whites (dark)
            gc.setFill(Color.rgb(50, 0, 0));
            gc.fillOval(leftEyeX - 1, eyeY - 1, eyeSize + 2, eyeSize);
            gc.fillOval(rightEyeX - 1, eyeY - 1, eyeSize + 2, eyeSize);

            // Bright red iris
            gc.setFill(Color.rgb(255, 20, 20));
            gc.fillOval(leftEyeX, eyeY, eyeSize, eyeSize - 1);
            gc.fillOval(rightEyeX, eyeY, eyeSize, eyeSize - 1);

            // Hot white center
            gc.setFill(Color.rgb(255, 200, 200));
            gc.fillOval(leftEyeX + 1.5, eyeY + 1, 2, 1.5);
            gc.fillOval(rightEyeX + 1.5, eyeY + 1, 2, 1.5);
        }
    }

    public boolean isChasing() {
        return state == State.CHASING;
    }

    public boolean isWaitingAtDoor() {
        return state == State.WAITING_AT_DOOR;
    }

    public boolean isDangerous() {
        return state == State.CHASING || state == State.WAITING_AT_DOOR;
    }

    public State getState() {
        return state;
    }

    public int getChaseTimer() {
        return chaseTimer;
    }

    public int getIdleTimer() {
        return idleTimer;
    }

    public int getWaitTimer() {
        return waitTimer;
    }

    @Override
    public Rectangle2D getHitbox() {
        return new Rectangle2D(x, y, size, size);
    }
}