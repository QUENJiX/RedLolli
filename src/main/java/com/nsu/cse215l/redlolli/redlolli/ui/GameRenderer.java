package com.nsu.cse215l.redlolli.redlolli.ui;

import com.nsu.cse215l.redlolli.redlolli.entities.Entity;
import com.nsu.cse215l.redlolli.redlolli.entities.Item;
import com.nsu.cse215l.redlolli.redlolli.entities.Monster;
import com.nsu.cse215l.redlolli.redlolli.entities.Player;
import com.nsu.cse215l.redlolli.redlolli.map.Maze;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Handles all in-game rendering: maze, entities, visual effects (lolli reveal, warning flash),
 * HUD, and escape room indicators.
 */
public class GameRenderer {

    // ========================= CONSTANTS =========================

    private static final double SCREEN_WIDTH = 880;
    private static final double SCREEN_HEIGHT = 730;

    // ========================= MAIN RENDER =========================

    /**
     * Renders a complete game frame: background, maze, entities, effects, and HUD.
     *
     * @param gc               the graphics context
     * @param maze             the current maze
     * @param entities         all entities to render
     * @param paleLuna         the Pale Luna monster (may be null)
     * @param player           the player entity
     * @param warningFlashTimer frames remaining for red flash effect
     * @param revealState      lolli reveal animation state (null if not active)
     * @param level            current game level (1-3)
     * @param chests           list of chests for HUD
     * @param itemNames        item names per level for HUD
     * @param pulsePhase       HUD animation pulse phase
     * @return updated pulse phase value
     */
    public static double render(GraphicsContext gc, Maze maze, List<Entity> entities,
                                 Monster paleLuna, Player player, int warningFlashTimer,
                                 LolliRevealState revealState, int level,
                                 List<Item> chests,
                                 String[] itemNames, double pulsePhase) {
        // Clear screen
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Draw maze and entities
        maze.renderMaze(gc);
        for (Entity e : entities) {
            e.render(gc);
        }

        // Draw Pale Luna's eyes on top of everything
        if (paleLuna != null) {
            paleLuna.renderEyes(gc);
        }

        // Warning flash overlay when chase starts
        if (warningFlashTimer > 0) {
            gc.setFill(Color.rgb(255, 0, 0, 0.15));
            gc.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        // Lolli reveal glow effect
        if (revealState != null && revealState.active) {
            renderLolliReveal(gc, revealState);
        }

        // HUD bar
        pulsePhase = HUDRenderer.drawHUD(gc, level, chests, itemNames, paleLuna, player, pulsePhase);

        // Escape room green bar indicator below HUD
        if (player.isInEscapeRoom()) {
            gc.setFill(Color.rgb(0, 80, 0, 0.25));
            gc.fillRect(0, 50, SCREEN_WIDTH, 4);
        }

        return pulsePhase;
    }

    // ========================= LOLLI REVEAL EFFECT =========================

    /**
     * Renders the golden glow and Red Lolli reveal animation.
     *
     * @param gc    the graphics context
     * @param state the lolli reveal animation state
     */
    private static void renderLolliReveal(GraphicsContext gc, LolliRevealState state) {
        double cx = state.x + 8;
        double cy = state.y + 8;
        double progress = 1.0 - (double) state.timer / state.duration;

        // Expanding golden glow rings
        double glowRadius = 20 + progress * 40;
        double pulse = Math.sin(state.phase) * 0.3 + 0.7;

        gc.setFill(Color.rgb(255, 215, 0, 0.08 * pulse));
        gc.fillOval(cx - glowRadius, cy - glowRadius, glowRadius * 2, glowRadius * 2);
        gc.setFill(Color.rgb(255, 180, 0, 0.15 * pulse));
        gc.fillOval(cx - glowRadius * 0.6, cy - glowRadius * 0.6, glowRadius * 1.2, glowRadius * 1.2);

        // Inner bright glow
        gc.setFill(Color.rgb(255, 230, 100, 0.3 * pulse));
        gc.fillOval(cx - 12, cy - 12, 24, 24);

        // The Red Lolli itself
        double lolliSize = 8 + progress * 4;
        drawRedLolli(gc, cx, cy, lolliSize);

        // "RED LOLLI FOUND!" text fading in
        double textAlpha = Math.min(1.0, progress * 2);
        gc.setFill(Color.rgb(255, 50, 50, textAlpha));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.fillText("RED LOLLI FOUND!", cx - 70, cy - glowRadius - 10);

        // Dim the rest of the screen
        gc.setFill(Color.rgb(0, 0, 0, 0.3 * progress));
        gc.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Redraw lolli and text on top of dimming
        gc.setFill(Color.rgb(255, 215, 0, 0.12 * pulse));
        gc.fillOval(cx - glowRadius * 0.7, cy - glowRadius * 0.7, glowRadius * 1.4, glowRadius * 1.4);
        drawRedLolli(gc, cx, cy, lolliSize);
        gc.setFill(Color.rgb(255, 50, 50, textAlpha));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.fillText("RED LOLLI FOUND!", cx - 70, cy - glowRadius - 10);
    }

    /** Draws a Red Lolli candy at the given center position. */
    private static void drawRedLolli(GraphicsContext gc, double cx, double cy, double size) {
        // Candy head
        gc.setFill(Color.rgb(220, 20, 20));
        gc.fillOval(cx - size / 2, cy - size / 2 - 2, size, size);
        // Shine
        gc.setFill(Color.rgb(255, 100, 100, 0.6));
        gc.fillOval(cx - size / 4, cy - size / 3 - 2, size / 3, size / 3);
        // Stick
        gc.setStroke(Color.rgb(200, 170, 120));
        gc.setLineWidth(2);
        gc.strokeLine(cx, cy + size / 2 - 2, cx, cy + size / 2 + 8);
    }

    // ========================= LOLLI REVEAL STATE =========================

    /**
     * Holds the animation state for the Red Lolli reveal effect.
     * Created when a lolli is found, consumed when the animation ends.
     */
    public static class LolliRevealState {
        public boolean active;
        public int timer;
        public int duration;
        public double x, y;
        public double phase;

        public LolliRevealState(double x, double y, int duration) {
            this.active = true;
            this.timer = duration;
            this.duration = duration;
            this.x = x;
            this.y = y;
            this.phase = 0;
        }
    }
}
