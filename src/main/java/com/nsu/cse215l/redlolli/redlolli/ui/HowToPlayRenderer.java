package com.nsu.cse215l.redlolli.redlolli.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Renders the "How to Play" screen content onto a Canvas.
 * Contains all section drawing methods for the instructional screen,
 * including objective, controls, chests, Pale Luna, escape rooms, HUD guide, and tips.
 */
public class HowToPlayRenderer {

    // ========================= MAIN ENTRY POINT =========================

    /**
     * Draws the entire How to Play screen content onto the given GraphicsContext.
     *
     * @param gc     the graphics context to draw on
     * @param width  canvas width
     * @param height canvas height
     */
    public static void drawContent(GraphicsContext gc, double width, double height) {
        drawBackground(gc, width, height);

        double y = 0;
        y = drawTitleHeader(gc, width, y);
        y = drawObjectiveSection(gc, width, y);
        y = drawControlsSection(gc, width, y);
        y = drawChestsSection(gc, width, y);
        y = drawPaleLunaSection(gc, width, y);
        y = drawEscapeRoomsSection(gc, width, y);
        y = drawHudGuideSection(gc, width, y);
        drawTipsSection(gc, width, y);
    }

    // ========================= BACKGROUND =========================

    /** Draws the full-height gradient background with subtle red side stripes. */
    private static void drawBackground(GraphicsContext gc, double W, double H) {
        for (int y = 0; y < (int) H; y++) {
            double t = (double) y / H;
            int r = (int) (5 + t * 10);
            int g = (int) (5 + t * 5);
            int b = (int) (8 + t * 15);
            gc.setFill(Color.rgb(r, g, b));
            gc.fillRect(0, y, W, 1);
        }

        // Subtle vertical red stripe accents on sides
        gc.setFill(Color.rgb(60, 0, 0, 0.08));
        gc.fillRect(0, 0, 4, H);
        gc.fillRect(W - 4, 0, 4, H);
    }

    // ========================= TITLE HEADER =========================

    /** Draws the "HOW TO PLAY" title with dark red gradient and glowing underline. */
    private static double drawTitleHeader(GraphicsContext gc, double W, double y) {
        double headerH = 100;

        // Dark red gradient header
        for (int i = 0; i < (int) headerH; i++) {
            double t = (double) i / headerH;
            gc.setFill(Color.rgb((int) (40 * t), 0, 0, 0.6));
            gc.fillRect(0, y + i, W, 1);
        }

        gc.setFont(Font.font("Serif", FontWeight.BOLD, 48));
        gc.setFill(Color.rgb(180, 20, 20));
        gc.fillText("HOW TO PLAY", W / 2 - 155, y + 55);

        // Glowing underline
        gc.setFill(Color.rgb(120, 0, 0, 0.8));
        gc.fillRect(W / 2 - 160, y + 68, 320, 2);
        gc.setFill(Color.rgb(200, 0, 0, 0.3));
        gc.fillRect(W / 2 - 170, y + 67, 340, 4);

        gc.setFont(Font.font("Serif", 16));
        gc.setFill(Color.rgb(120, 110, 100));
        gc.fillText("Survive the maze. Find the lolli. Escape Pale Luna.", W / 2 - 180, y + 90);

        return y + headerH + 15;
    }

    // ========================= SECTION 1: OBJECTIVE =========================

    /** Draws the objective section explaining the game goal and Red Lolli visual. */
    private static double drawObjectiveSection(GraphicsContext gc, double W, double y) {
        double cardX = 40, cardW = W - 80, cardH = 180;
        drawSectionCard(gc, cardX, y, cardW, cardH);
        drawSectionNumber(gc, cardX + 15, y + 15, 1);
        drawSectionTitle(gc, cardX + 55, y + 38, "YOUR OBJECTIVE");

        drawBodyLines(gc, cardX + 30, y + 70, new String[]{
                "Escape Pale Luna across 3 increasingly difficult maze levels.",
                "In each level, find the RED LOLLI hidden inside one of the chests.",
                "The lolli transforms into a cursed item. Collect it to advance.",
                "Survive all 3 levels to win. Die once and it's over."
        });

        // Red Lolli visual with glow
        double lx = cardX + cardW - 120, ly = y + 50;
        gc.setFill(Color.rgb(200, 0, 0, 0.1));
        gc.fillOval(lx - 15, ly - 15, 70, 70);
        gc.setFill(Color.rgb(255, 0, 0, 0.15));
        gc.fillOval(lx, ly, 40, 40);
        gc.setFill(Color.rgb(220, 20, 20));
        gc.fillOval(lx + 8, ly + 5, 24, 24);
        gc.setFill(Color.rgb(255, 80, 80, 0.5));
        gc.fillOval(lx + 13, ly + 8, 9, 9);
        gc.setStroke(Color.rgb(200, 170, 120));
        gc.setLineWidth(2.5);
        gc.strokeLine(lx + 20, ly + 29, lx + 20, ly + 52);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 12));
        gc.setFill(Color.GOLD);
        gc.fillText("Red Lolli", lx + 3, ly + 66);

        return y + cardH + 20;
    }

    // ========================= SECTION 2: CONTROLS =========================

    /** Draws the controls section with 3D WASD keycap visuals. */
    private static double drawControlsSection(GraphicsContext gc, double W, double y) {
        double cardX = 40, cardW = W - 80, cardH = 155;
        drawSectionCard(gc, cardX, y, cardW, cardH);
        drawSectionNumber(gc, cardX + 15, y + 15, 2);
        drawSectionTitle(gc, cardX + 55, y + 38, "CONTROLS");

        double kbX = cardX + 80, kbY = y + 60;
        double keyS = 42, gap = 6;

        drawKey(gc, kbX + keyS + gap, kbY, keyS, "W");                      // W key
        drawKey(gc, kbX, kbY + keyS + gap, keyS, "A");                      // A key
        drawKey(gc, kbX + keyS + gap, kbY + keyS + gap, keyS, "S");         // S key
        drawKey(gc, kbX + 2 * (keyS + gap), kbY + keyS + gap, keyS, "D");   // D key

        // Direction labels
        drawBodyLines(gc, cardX + 320, y + 80, new String[]{
                "W  -  Move Up",
                "A  -  Move Left",
                "S  -  Move Down",
                "D  -  Move Right"
        });

        return y + cardH + 20;
    }

    /** Draws a single 3D-style keyboard key. */
    private static void drawKey(GraphicsContext gc, double x, double y, double size, String key) {
        // Shadow
        gc.setFill(Color.rgb(10, 8, 12));
        gc.fillRect(x + 2, y + 3, size, size);
        // Key face
        gc.setFill(Color.rgb(28, 24, 32));
        gc.fillRect(x, y, size, size);
        // Top highlight
        gc.setFill(Color.rgb(50, 45, 55));
        gc.fillRect(x, y, size, 3);
        // Border
        gc.setStroke(Color.rgb(80, 50, 60));
        gc.setLineWidth(1.5);
        gc.strokeRect(x, y, size, size);
        // Letter
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setFill(Color.rgb(220, 200, 200));
        gc.fillText(key, x + size / 2 - 7, y + size / 2 + 7);
    }

    // ========================= SECTION 3: CHESTS =========================

    /** Draws the chests section with closed, empty, and lolli chest visuals. */
    private static double drawChestsSection(GraphicsContext gc, double W, double y) {
        double cardX = 40, cardW = W - 80, cardH = 230;
        drawSectionCard(gc, cardX, y, cardW, cardH);
        drawSectionNumber(gc, cardX + 15, y + 15, 3);
        drawSectionTitle(gc, cardX + 55, y + 38, "TREASURE CHESTS");

        drawBodyLines(gc, cardX + 30, y + 65, new String[]{
                "Each level has multiple chests scattered through the maze.",
                "The Red Lolli is randomly placed in ONE chest each run.",
                "Open a chest by walking into it. Empty ones stay open."
        });

        // Three chest state visuals
        double boxW = 180, boxH = 90;
        double startX = cardX + 50, boxY = y + 125;
        String[] chestLabels = {"CLOSED", "EMPTY", "RED LOLLI!"};
        Color[] accentColors = {Color.rgb(180, 150, 60), Color.rgb(100, 80, 60), Color.rgb(200, 30, 30)};

        for (int i = 0; i < 3; i++) {
            double bx = startX + i * (boxW + 30);
            drawChestStateBox(gc, bx, boxY, boxW, boxH, i, chestLabels[i], accentColors[i]);
        }

        return y + cardH + 20;
    }

    /** Draws one of the three chest state boxes (closed, empty, lolli). */
    private static void drawChestStateBox(GraphicsContext gc, double bx, double boxY,
                                           double boxW, double boxH, int type,
                                           String label, Color accent) {
        // Box background and border
        gc.setFill(Color.rgb(20, 18, 24));
        gc.fillRect(bx, boxY, boxW, boxH);
        gc.setStroke(accent.deriveColor(0, 1, 1, 0.5));
        gc.setLineWidth(1);
        gc.strokeRect(bx, boxY, boxW, boxH);
        gc.setFill(accent);
        gc.fillRect(bx, boxY, boxW, 2);

        double cx = bx + boxW / 2, cy = boxY + 35;

        switch (type) {
            case 0 -> drawClosedChest(gc, cx, cy);
            case 1 -> drawEmptyChest(gc, cx, cy);
            case 2 -> drawLolliChest(gc, cx, cy);
        }

        // Label
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setFill(accent);
        gc.fillText(label, cx - label.length() * 3.5, boxY + boxH - 8);
    }

    /** Draws a closed chest icon. */
    private static void drawClosedChest(GraphicsContext gc, double cx, double cy) {
        gc.setFill(Color.rgb(139, 90, 40));
        gc.fillRect(cx - 18, cy - 5, 36, 24);
        gc.setFill(Color.rgb(170, 120, 55));
        gc.fillRect(cx - 18, cy - 14, 36, 12);
        gc.setStroke(Color.rgb(100, 65, 15));
        gc.setLineWidth(1.5);
        gc.strokeRect(cx - 18, cy - 14, 36, 36);
        gc.setStroke(Color.rgb(160, 140, 80));
        gc.setLineWidth(1);
        gc.strokeLine(cx - 18, cy + 5, cx + 18, cy + 5);
        gc.setFill(Color.GOLD);
        gc.fillOval(cx - 5, cy + 1, 10, 10);
        gc.setFill(Color.rgb(100, 70, 10));
        gc.fillRect(cx - 2, cy + 5, 4, 4);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("?", cx - 4, cy - 3);
    }

    /** Draws an open empty chest icon. */
    private static void drawEmptyChest(GraphicsContext gc, double cx, double cy) {
        gc.setFill(Color.rgb(80, 50, 15));
        gc.fillRect(cx - 18, cy - 2, 36, 22);
        gc.setFill(Color.rgb(25, 12, 5));
        gc.fillRect(cx - 14, cy + 2, 28, 14);
        gc.setFill(Color.rgb(120, 85, 35));
        gc.fillRect(cx - 20, cy - 15, 40, 14);
        gc.setFill(Color.rgb(80, 70, 40));
        gc.fillRect(cx + 14, cy - 6, 4, 8);
    }

    /** Draws an open chest containing the Red Lolli. */
    private static void drawLolliChest(GraphicsContext gc, double cx, double cy) {
        gc.setFill(Color.rgb(80, 50, 15));
        gc.fillRect(cx - 18, cy - 2, 36, 22);
        gc.setFill(Color.rgb(40, 8, 8));
        gc.fillRect(cx - 14, cy + 2, 28, 14);
        gc.setFill(Color.rgb(120, 85, 35));
        gc.fillRect(cx - 20, cy - 15, 40, 14);
        gc.setFill(Color.rgb(200, 0, 0, 0.2));
        gc.fillOval(cx - 20, cy - 15, 40, 40);
        gc.setFill(Color.rgb(220, 20, 20));
        gc.fillOval(cx - 7, cy - 10, 14, 14);
        gc.setFill(Color.rgb(255, 100, 100, 0.5));
        gc.fillOval(cx - 3, cy - 8, 5, 5);
    }

    // ========================= SECTION 4: PALE LUNA =========================

    /** Draws the Pale Luna section with three AI state cards and face visuals. */
    private static double drawPaleLunaSection(GraphicsContext gc, double W, double y) {
        double cardX = 40, cardW = W - 80, cardH = 280;
        drawSectionCard(gc, cardX, y, cardW, cardH);
        drawSectionNumber(gc, cardX + 15, y + 15, 4);
        drawSectionTitle(gc, cardX + 55, y + 38, "PALE LUNA - THE DEMON");

        drawBodyLines(gc, cardX + 30, y + 65, new String[]{
                "Pale Luna is a demon girl with yellow hair and glowing red eyes.",
                "She hunts you through the maze using intelligent pathfinding.",
                "If she touches you, you DIE instantly. No second chances."
        });

        // Three AI state cards
        double stateY = y + 130;
        double stateW = 200, stateH = 130;
        String[] stateNames = {"IDLE", "CHASING", "WAITING"};
        String[] stateDescs = {"15 seconds", "5 seconds", "3 seconds"};
        String[] stateDetails = {"Sleeping... safe to explore", "RUN! She's hunting you!", "Don't leave the safe zone!"};
        Color[] bgColors = {Color.rgb(15, 20, 15), Color.rgb(30, 8, 8), Color.rgb(25, 18, 5)};
        Color[] borderColors = {Color.rgb(60, 80, 60), Color.rgb(160, 30, 30), Color.rgb(180, 120, 20)};

        for (int i = 0; i < 3; i++) {
            double sx = cardX + 40 + i * (stateW + 25);
            drawLunaStateCard(gc, sx, stateY, stateW, stateH, i,
                    stateNames[i], stateDescs[i], stateDetails[i],
                    bgColors[i], borderColors[i]);
        }

        return y + cardH + 20;
    }

    /** Draws a single Pale Luna AI state card with face illustration. */
    private static void drawLunaStateCard(GraphicsContext gc, double sx, double stateY,
                                           double stateW, double stateH, int stateIndex,
                                           String name, String desc, String detail,
                                           Color bgColor, Color borderColor) {
        // Card background
        gc.setFill(bgColor);
        gc.fillRect(sx, stateY, stateW, stateH);
        gc.setStroke(borderColor);
        gc.setLineWidth(1.5);
        gc.strokeRect(sx, stateY, stateW, stateH);

        // Top label bar
        gc.setFill(borderColor.deriveColor(0, 1, 0.7, 1));
        gc.fillRect(sx, stateY, stateW, 22);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setFill(Color.WHITE);
        gc.fillText(name + " (" + desc + ")", sx + 10, stateY + 16);

        // Face illustration
        double faceX = sx + stateW / 2, faceY = stateY + 55;
        drawLunaFace(gc, faceX, faceY, stateIndex);

        // Detail text
        gc.setFont(Font.font("Arial", 11));
        gc.setFill(Color.rgb(160, 155, 150));
        gc.fillText(detail, sx + 10, stateY + stateH - 10);
    }

    /** Draws Pale Luna's face illustration for a given state (0=idle, 1=chasing, 2=waiting). */
    private static void drawLunaFace(GraphicsContext gc, double faceX, double faceY, int state) {
        double faceW = 30, faceH = 34;

        // Skin
        Color skin = (state == 0) ? Color.rgb(200, 190, 180, 0.4) : Color.rgb(235, 225, 215);
        gc.setFill(skin);
        gc.fillOval(faceX - faceW / 2, faceY - faceH / 2, faceW, faceH);

        // Hair
        Color hair = (state == 0) ? Color.rgb(160, 140, 30, 0.4) : Color.rgb(220, 200, 60);
        gc.setFill(hair);
        gc.fillArc(faceX - faceW / 2 - 2, faceY - faceH / 2 - 5, faceW + 4, faceH * 0.5, 0, 180, ArcType.ROUND);
        gc.fillOval(faceX - faceW / 2 - 3, faceY - faceH / 4, 6, faceH * 0.6);
        gc.fillOval(faceX + faceW / 2 - 3, faceY - faceH / 4, 6, faceH * 0.6);

        // Eyes
        if (state == 0) {
            gc.setFill(Color.rgb(150, 0, 0, 0.4));
            gc.fillOval(faceX - 7, faceY - 2, 5, 3);
            gc.fillOval(faceX + 3, faceY - 2, 5, 3);
        } else if (state == 1) {
            gc.setFill(Color.rgb(255, 0, 0, 0.3));
            gc.fillOval(faceX - 12, faceY - 8, 24, 16);
            gc.setFill(Color.rgb(255, 20, 20));
            gc.fillOval(faceX - 8, faceY - 3, 6, 5);
            gc.fillOval(faceX + 3, faceY - 3, 6, 5);
            gc.setFill(Color.rgb(255, 180, 180));
            gc.fillOval(faceX - 6, faceY - 1, 2, 2);
            gc.fillOval(faceX + 5, faceY - 1, 2, 2);
        } else {
            gc.setFill(Color.rgb(220, 0, 0));
            gc.fillOval(faceX - 7, faceY - 3, 5, 5);
            gc.fillOval(faceX + 3, faceY - 3, 5, 5);
            gc.setFill(Color.rgb(255, 100, 100));
            gc.fillOval(faceX - 6, faceY - 1, 2, 2);
            gc.fillOval(faceX + 4, faceY - 1, 2, 2);
        }

        // Mouth
        if (state == 1) {
            gc.setStroke(Color.rgb(100, 0, 0));
            gc.setLineWidth(1.5);
            gc.strokeArc(faceX - 6, faceY + 5, 12, 6, 180, 180, ArcType.OPEN);
        } else if (state == 2) {
            gc.setStroke(Color.rgb(80, 0, 0));
            gc.setLineWidth(1);
            gc.strokeArc(faceX - 4, faceY + 6, 8, 3, 180, 180, ArcType.OPEN);
        } else {
            gc.setStroke(Color.rgb(120, 100, 100, 0.3));
            gc.setLineWidth(0.8);
            gc.strokeLine(faceX - 4, faceY + 7, faceX + 4, faceY + 7);
        }
    }

    // ========================= SECTION 5: ESCAPE ROOMS =========================

    /** Draws the escape rooms section with safe zone tile visual. */
    private static double drawEscapeRoomsSection(GraphicsContext gc, double W, double y) {
        double cardX = 40, cardW = W - 80, cardH = 175;
        drawSectionCard(gc, cardX, y, cardW, cardH);
        drawSectionNumber(gc, cardX + 15, y + 15, 5);
        drawSectionTitle(gc, cardX + 55, y + 38, "ESCAPE ROOMS");

        drawBodyLines(gc, cardX + 30, y + 65, new String[]{
                "Green tiles marked \"S\" are SAFE ZONES in the maze.",
                "If Pale Luna is chasing, reach one to force her to wait.",
                "She'll stand at the door for 3 seconds then return to idle.",
                "WARNING: Do NOT leave while she's waiting at the door!"
        });

        // Escape room tile visual
        double rx = cardX + cardW - 150, ry = y + 55;
        gc.setFill(Color.rgb(0, 80, 0, 0.1));
        gc.fillOval(rx - 10, ry - 10, 80, 80);
        gc.setFill(Color.rgb(12, 35, 12));
        gc.fillRect(rx + 5, ry + 2, 50, 50);
        gc.setStroke(Color.rgb(0, 100, 0));
        gc.setLineWidth(2);
        gc.strokeRect(rx + 7, ry + 4, 46, 46);
        gc.setStroke(Color.rgb(0, 60, 0));
        gc.setLineWidth(1);
        gc.strokeRect(rx + 12, ry + 9, 36, 36);
        gc.setFill(Color.rgb(0, 160, 0));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gc.fillText("S", rx + 20, ry + 38);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.setFill(Color.rgb(0, 140, 0));
        gc.fillText("SAFE ZONE", rx + 5, ry + 65);

        return y + cardH + 20;
    }

    // ========================= SECTION 6: HUD GUIDE =========================

    /** Draws the HUD guide section with a mock HUD bar and annotations. */
    private static double drawHudGuideSection(GraphicsContext gc, double W, double y) {
        double cardX = 40, cardW = W - 80, cardH = 220;
        drawSectionCard(gc, cardX, y, cardW, cardH);
        drawSectionNumber(gc, cardX + 15, y + 15, 6);
        drawSectionTitle(gc, cardX + 55, y + 38, "HUD GUIDE");

        // Mock HUD bar
        double hudX = cardX + 30, hudY = y + 60, hudW = cardW - 60, hudH = 36;
        gc.setFill(Color.rgb(12, 12, 16));
        gc.fillRect(hudX, hudY, hudW, hudH);
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(1.5);
        gc.strokeLine(hudX, hudY + hudH, hudX + hudW, hudY + hudH);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.setFill(Color.rgb(180, 180, 200));
        gc.fillText("LEVEL 1/3", hudX + 8, hudY + 22);

        gc.setStroke(Color.rgb(50, 20, 20));
        gc.setLineWidth(1);
        gc.strokeLine(hudX + 82, hudY + 6, hudX + 82, hudY + 30);

        gc.setFill(Color.DARKRED);
        gc.fillOval(hudX + 90, hudY + 12, 7, 7);
        gc.setFill(Color.rgb(200, 200, 200));
        gc.fillText("0/1", hudX + 102, hudY + 22);
        gc.strokeLine(hudX + 130, hudY + 6, hudX + 130, hudY + 30);

        gc.setFill(Color.GOLD);
        gc.fillText("FIND: Mud", hudX + 140, hudY + 22);
        gc.strokeLine(hudX + 230, hudY + 6, hudX + 230, hudY + 30);

        gc.setFill(Color.rgb(180, 160, 120));
        gc.fillText("CHESTS: 0/3", hudX + 240, hudY + 22);
        gc.strokeLine(hudX + 340, hudY + 6, hudX + 340, hudY + 30);

        // Mini timer bar
        gc.setFill(Color.rgb(40, 40, 45));
        gc.fillRect(hudX + 350, hudY + 8, 120, 10);
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(hudX + 351, hudY + 9, 80, 8);
        gc.setFill(Color.LIMEGREEN);
        gc.fillText("WAKES IN: 9s", hudX + 350, hudY + 32);
        gc.strokeLine(hudX + 580, hudY + 6, hudX + 580, hudY + 30);
        gc.setFill(Color.LIMEGREEN);
        gc.fillText("[SAFE]", hudX + 590, hudY + 22);

        // HUD element annotations
        double annY = hudY + hudH + 20;
        String[][] annotations = {
                {"LEVEL", "Your current level (1-3)"},
                {"LOLLI", "Red Lolli found status"},
                {"FIND", "Item to collect this level"},
                {"CHESTS", "Chests opened so far"},
                {"TIMER", "Pale Luna's countdown"},
                {"SAFE", "In escape room indicator"}
        };

        for (int i = 0; i < annotations.length; i++) {
            double ax = cardX + 30 + (i % 3) * 260;
            double ay = annY + (i / 3) * 40;
            gc.setFill(Color.rgb(200, 70, 70));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.fillText(annotations[i][0], ax, ay);
            gc.setFill(Color.rgb(160, 155, 150));
            gc.setFont(Font.font("Arial", 12));
            gc.fillText("- " + annotations[i][1], ax + 55, ay);
        }

        return y + cardH + 20;
    }

    // ========================= SECTION 7: SURVIVAL TIPS =========================

    /** Draws the tips section with diamond bullet points. */
    private static double drawTipsSection(GraphicsContext gc, double W, double y) {
        double cardX = 40, cardW = W - 80, cardH = 180;
        drawSectionCard(gc, cardX, y, cardW, cardH);
        drawSectionNumber(gc, cardX + 15, y + 15, 7);
        drawSectionTitle(gc, cardX + 55, y + 38, "SURVIVAL TIPS");

        String[] tips = {
                "Memorize escape room locations before Pale Luna wakes.",
                "Open chests quickly during the 15-second idle window.",
                "Pale Luna gets closer each cycle \u2014 she doesn\u2019t reset to spawn!",
                "Later levels have more chests but less time between hunts.",
                "Plan your route. Panic is how Pale Luna wins."
        };

        gc.setFont(Font.font("Arial", 14));
        double tipY = y + 68;
        for (String tip : tips) {
            // Diamond bullet
            gc.setFill(Color.rgb(180, 40, 40));
            gc.fillRect(cardX + 33, tipY - 8, 6, 6);
            // Tip text
            gc.setFill(Color.rgb(190, 185, 180));
            gc.fillText(tip, cardX + 50, tipY);
            tipY += 24;
        }

        return y + cardH + 20;
    }

    // ========================= SHARED CARD COMPONENTS =========================

    /** Draws a section card background with accent border. */
    private static void drawSectionCard(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(Color.rgb(14, 12, 18, 0.9));
        gc.fillRect(x, y, w, h);
        gc.setFill(Color.rgb(120, 20, 20));
        gc.fillRect(x, y, w, 2);
        gc.setStroke(Color.rgb(40, 20, 25));
        gc.setLineWidth(1);
        gc.strokeRect(x, y, w, h);
    }

    /** Draws a numbered circle badge for a section. */
    private static void drawSectionNumber(GraphicsContext gc, double x, double y, int num) {
        gc.setFill(Color.rgb(60, 0, 0));
        gc.fillOval(x, y, 30, 30);
        gc.setStroke(Color.rgb(140, 30, 30));
        gc.setLineWidth(1.5);
        gc.strokeOval(x, y, 30, 30);
        gc.setFill(Color.rgb(200, 60, 60));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText(String.valueOf(num), x + (num < 10 ? 10 : 6), y + 21);
    }

    /** Draws a section title in horror-themed serif font. */
    private static void drawSectionTitle(GraphicsContext gc, double x, double y, String title) {
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 24));
        gc.setFill(Color.rgb(200, 70, 70));
        gc.fillText(title, x, y);
    }

    /** Draws multiple lines of body text at the given position. */
    private static void drawBodyLines(GraphicsContext gc, double x, double y, String[] lines) {
        gc.setFont(Font.font("Arial", 14));
        for (String line : lines) {
            gc.setFill(Color.rgb(190, 185, 180));
            gc.fillText(line, x, y);
            y += 22;
        }
    }
}
