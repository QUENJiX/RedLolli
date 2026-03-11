package com.nsu.cse215l.redlolli.redlolli.ui;

import com.nsu.cse215l.redlolli.redlolli.entities.Item;
import com.nsu.cse215l.redlolli.redlolli.entities.Monster;
import com.nsu.cse215l.redlolli.redlolli.entities.Player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Renders the in-game Heads-Up Display (HUD) bar at the top of the screen.
 * Displays level, lolli status, current item, chest count, Pale Luna state, and safe indicator.
 */
public class HUDRenderer {

    // ========================= CONSTANTS =========================

    private static final double HUD_HEIGHT = 50;
    private static final double HUD_WIDTH = 880;
    private static final double MID_Y = 32;

    // ========================= MAIN DRAW METHOD =========================

    /**
     * Draws the full HUD bar at the top of the game screen.
     *
     * @param gc         the graphics context to draw on
     * @param level      current game level (1-3)
     * @param chests     list of all chests in the level
     * @param itemNames  array of item names per level
     * @param paleLuna   the Pale Luna monster (may be null)
     * @param player     the player entity
     * @param pulsePhase current animation pulse phase (for timer/chase effects)
     * @return updated pulse phase value to be stored back by the caller
     */
    public static double drawHUD(GraphicsContext gc, int level, List<Item> chests,
                                  String[] itemNames, Monster paleLuna, Player player,
                                  double pulsePhase) {
        drawBackground(gc);
        drawLevelIndicator(gc, level);
        drawDivider(gc, 100);
        drawLolliStatus(gc, chests);
        drawDivider(gc, 160);
        drawFindItem(gc, level, itemNames);
        drawDivider(gc, 290);
        drawChestCount(gc, chests);
        drawDivider(gc, 410);
        pulsePhase = drawPaleLunaStatus(gc, paleLuna, pulsePhase);
        drawDivider(gc, 790);
        drawSafeIndicator(gc, player);

        return pulsePhase;
    }

    // ========================= HUD SECTIONS =========================

    /** Draws the dark HUD background bar with red bottom border. */
    private static void drawBackground(GraphicsContext gc) {
        gc.setFill(Color.rgb(12, 12, 16));
        gc.fillRect(0, 0, HUD_WIDTH, HUD_HEIGHT);
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(2);
        gc.strokeLine(0, HUD_HEIGHT, HUD_WIDTH, HUD_HEIGHT);
    }

    /** Draws the "LEVEL X/3" text. */
    private static void drawLevelIndicator(GraphicsContext gc, int level) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setFill(Color.rgb(180, 180, 200));
        gc.fillText("LEVEL " + level + "/3", 12, MID_Y);
    }

    /** Draws a vertical divider line at the given x position. */
    private static void drawDivider(GraphicsContext gc, double x) {
        gc.setStroke(Color.rgb(50, 20, 20));
        gc.setLineWidth(1);
        gc.strokeLine(x, 8, x, 42);
    }

    /** Draws the Red Lolli icon and found status (0/1 or 1/1). */
    private static void drawLolliStatus(GraphicsContext gc, List<Item> chests) {
        boolean foundLolli = chests.stream().anyMatch(c -> c.isCollected() && c.hasLolli());

        // Lolli icon
        gc.setFill(Color.DARKRED);
        gc.fillOval(112, MID_Y - 9, 8, 8);
        gc.setStroke(Color.rgb(180, 150, 100));
        gc.setLineWidth(1.5);
        gc.strokeLine(116, MID_Y - 1, 116, MID_Y + 5);

        // Count text
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setFill(foundLolli ? Color.LIMEGREEN : Color.rgb(200, 200, 200));
        gc.fillText((foundLolli ? "1" : "0") + "/1", 124, MID_Y);
    }

    /** Draws the "FIND: [item]" indicator. */
    private static void drawFindItem(GraphicsContext gc, int level, String[] itemNames) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setFill(Color.GOLD);
        gc.fillText("FIND: " + itemNames[level - 1], 172, MID_Y);
    }

    /** Draws the "CHESTS: X/Y" counter. */
    private static void drawChestCount(GraphicsContext gc, List<Item> chests) {
        long opened = chests.stream().filter(Item::isCollected).count();
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setFill(Color.rgb(180, 160, 120));
        gc.fillText("CHESTS: " + opened + "/" + chests.size(), 302, MID_Y);
    }

    /** Draws the Pale Luna state indicator (timer bar, chase warning, or door wait). Returns updated pulse phase. */
    private static double drawPaleLunaStatus(GraphicsContext gc, Monster paleLuna, double pulsePhase) {
        if (paleLuna == null) return pulsePhase;

        Monster.State lunaState = paleLuna.getState();
        double barX = 422, barW = 180;
        double barY = MID_Y - 18, barH = 12;

        switch (lunaState) {
            case IDLE -> pulsePhase = drawIdleStatus(gc, paleLuna, barX, barY, barW, barH, pulsePhase);
            case CHASING -> pulsePhase = drawChasingStatus(gc, paleLuna, barX, barY, barW, barH, pulsePhase);
            case WAITING_AT_DOOR -> drawWaitingStatus(gc, paleLuna, barX);
        }

        return pulsePhase;
    }

    /** Draws idle countdown timer bar with color-coded urgency. */
    private static double drawIdleStatus(GraphicsContext gc, Monster paleLuna,
                                          double barX, double barY, double barW, double barH,
                                          double pulsePhase) {
        int secondsLeft = paleLuna.getIdleTimer() / 60;
        Color timerColor;

        if (secondsLeft > 10) {
            timerColor = Color.LIMEGREEN;
        } else if (secondsLeft > 6) {
            timerColor = Color.YELLOW;
        } else if (secondsLeft > 3) {
            timerColor = Color.ORANGE;
        } else {
            double flash = Math.sin(pulsePhase) * 0.5 + 0.5;
            timerColor = Color.rgb(255, (int) (50 * flash), (int) (50 * flash));
        }
        pulsePhase += 0.2;

        // Timer bar background and fill
        gc.setFill(Color.rgb(40, 40, 45));
        gc.fillRect(barX, barY, barW, barH);
        gc.setStroke(Color.rgb(80, 80, 90));
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barW, barH);
        double fillRatio = (double) paleLuna.getIdleTimer() / 900.0;
        gc.setFill(timerColor);
        gc.fillRect(barX + 1, barY + 1, (barW - 2) * fillRatio, barH - 2);

        // Timer text
        gc.setFill(timerColor);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("WAKES IN: " + secondsLeft + "s", barX, MID_Y + 6);

        return pulsePhase;
    }

    /** Draws the flashing red chase warning bar. */
    private static double drawChasingStatus(GraphicsContext gc, Monster paleLuna,
                                             double barX, double barY, double barW, double barH,
                                             double pulsePhase) {
        double chaseFlash = Math.sin(pulsePhase * 2) * 0.5 + 0.5;
        pulsePhase += 0.3;

        gc.setFill(Color.rgb(255, (int) (30 * chaseFlash), (int) (30 * chaseFlash)));
        gc.fillRect(barX, barY, barW, barH);
        gc.setStroke(Color.rgb(120, 0, 0));
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barW, barH);
        double chaseFill = (double) paleLuna.getChaseTimer() / 300.0;
        gc.setFill(Color.rgb(200, 0, 0));
        gc.fillRect(barX + 1, barY + 1, (barW - 2) * chaseFill, barH - 2);

        int chaseSecsLeft = paleLuna.getChaseTimer() / 60;
        gc.setFill(Color.RED);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("!! HUNTING: " + chaseSecsLeft + "s !!", barX, MID_Y + 6);

        return pulsePhase;
    }

    /** Draws the waiting-at-door status text. */
    private static void drawWaitingStatus(GraphicsContext gc, Monster paleLuna, double barX) {
        gc.setFill(Color.ORANGE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        int waitSecs = paleLuna.getWaitTimer() / 60;
        gc.fillText("AT DOOR (" + waitSecs + "s) - STAY!", barX, MID_Y);
    }

    /** Draws the [SAFE] indicator when the player is in an escape room. */
    private static void drawSafeIndicator(GraphicsContext gc, Player player) {
        if (player.isInEscapeRoom()) {
            gc.setFill(Color.LIMEGREEN);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText("[SAFE]", 805, MID_Y);
        }
    }
}
