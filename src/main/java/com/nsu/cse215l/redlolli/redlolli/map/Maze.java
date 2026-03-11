package com.nsu.cse215l.redlolli.redlolli.map;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Loads, stores, and renders the maze grid from CSV files.
 * Provides collision detection, escape room queries, BFS pathfinding, and line-of-sight checks.
 *
 * <p>Tile types:
 * <ul>
 *   <li>0 — Floor (walkable)</li>
 *   <li>1 — Wall (solid)</li>
 *   <li>2, 3 — Chest spawn positions (converted to floor after entity spawn)</li>
 *   <li>5 — Pale Luna spawn (converted to floor after entity spawn)</li>
 *   <li>6 — Escape room / safe zone</li>
 *   <li>7 — Player spawn (converted to floor after loading)</li>
 * </ul>
 */
public class Maze {

    // ========================= CONSTANTS =========================

    public static final double TILE_SIZE = 40.0;
    public static final double Y_OFFSET = 50.0;

    // ========================= FIELDS =========================

    private int[][] mapGrid;
    private int playerSpawnRow = 1;
    private int playerSpawnCol = 1;

    // ========================= CONSTRUCTOR =========================

    public Maze(String csvFilePath) {
        loadMapFromCSV(csvFilePath);
    }

    // ========================= MAP LOADING =========================

    /** Loads the maze grid from a CSV resource file, detecting player spawn and building the grid. */
    private void loadMapFromCSV(String path) {
        List<int[]> rowList = new ArrayList<>();

        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("Map file not found: " + path + ". Loading fallback map.");
                mapGrid = new int[][]{
                        {1, 1, 1, 1, 1, 1},
                        {1, 0, 0, 0, 2, 1},
                        {1, 0, 1, 1, 0, 1},
                        {1, 0, 0, 0, 0, 1},
                        {1, 1, 1, 1, 1, 1}
                };
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                int[] row = new int[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    row[i] = Integer.parseInt(tokens[i].trim());
                }
                rowList.add(row);
            }
            br.close();

            mapGrid = new int[rowList.size()][];
            for (int i = 0; i < rowList.size(); i++) {
                mapGrid[i] = rowList.get(i);
            }

            // Find and consume player spawn tile (7)
            for (int r = 0; r < mapGrid.length; r++) {
                for (int c = 0; c < mapGrid[r].length; c++) {
                    if (mapGrid[r][c] == 7) {
                        playerSpawnRow = r;
                        playerSpawnCol = c;
                        mapGrid[r][c] = 0;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            mapGrid = new int[][]{{1, 1, 1}, {1, 0, 1}, {1, 1, 1}};
        }
    }

    // ========================= RENDERING =========================

    /** Renders the entire maze grid with differentiated wall styles and floor tiles. */
    public void renderMaze(GraphicsContext gc) {
        if (mapGrid == null) return;

        int maxRow = mapGrid.length - 1;
        int maxCol = mapGrid[0].length - 1;

        for (int row = 0; row < mapGrid.length; row++) {
            for (int col = 0; col < mapGrid[row].length; col++) {
                double tileX = col * TILE_SIZE;
                double tileY = row * TILE_SIZE + Y_OFFSET;
                int tile = mapGrid[row][col];
                boolean isBorder = (row == 0 || row == maxRow || col == 0 || col == maxCol);

                if (tile == 1) {
                    if (isBorder) {
                        renderBorderWall(gc, tileX, tileY);
                    } else {
                        renderInnerWall(gc, tileX, tileY);
                    }
                } else if (tile == 6) {
                    renderEscapeRoom(gc, tileX, tileY);
                } else {
                    renderFloor(gc, tileX, tileY, row, col);
                }
            }
        }
    }

    /** Renders a dark outer border wall tile with stone texture lines. */
    private void renderBorderWall(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.rgb(18, 14, 18));
        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);

        gc.setStroke(Color.rgb(30, 25, 32));
        gc.setLineWidth(0.5);
        gc.strokeLine(x, y + TILE_SIZE * 0.33, x + TILE_SIZE, y + TILE_SIZE * 0.33);
        gc.strokeLine(x, y + TILE_SIZE * 0.66, x + TILE_SIZE, y + TILE_SIZE * 0.66);

        gc.setStroke(Color.rgb(10, 8, 12));
        gc.setLineWidth(1);
        gc.strokeRect(x, y, TILE_SIZE, TILE_SIZE);
    }

    /** Renders an inner maze wall tile with brick pattern, mortar lines, and 3D depth edges. */
    private void renderInnerWall(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.rgb(55, 40, 48));
        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);

        // Mortar lines (brick layout)
        gc.setStroke(Color.rgb(38, 28, 35));
        gc.setLineWidth(0.7);
        gc.strokeLine(x, y + TILE_SIZE * 0.5, x + TILE_SIZE, y + TILE_SIZE * 0.5);
        gc.strokeLine(x + TILE_SIZE * 0.5, y, x + TILE_SIZE * 0.5, y + TILE_SIZE * 0.5);
        gc.strokeLine(x + TILE_SIZE * 0.25, y + TILE_SIZE * 0.5, x + TILE_SIZE * 0.25, y + TILE_SIZE);
        gc.strokeLine(x + TILE_SIZE * 0.75, y + TILE_SIZE * 0.5, x + TILE_SIZE * 0.75, y + TILE_SIZE);

        // Highlight top-left edge (depth)
        gc.setStroke(Color.rgb(70, 52, 62));
        gc.setLineWidth(1);
        gc.strokeLine(x + 1, y + 1, x + TILE_SIZE - 1, y + 1);
        gc.strokeLine(x + 1, y + 1, x + 1, y + TILE_SIZE - 1);

        // Shadow bottom-right edge
        gc.setStroke(Color.rgb(30, 20, 28));
        gc.strokeLine(x + TILE_SIZE - 1, y + 1, x + TILE_SIZE - 1, y + TILE_SIZE - 1);
        gc.strokeLine(x + 1, y + TILE_SIZE - 1, x + TILE_SIZE - 1, y + TILE_SIZE - 1);
    }

    /** Renders a green escape room (safe zone) tile with "S" label. */
    private void renderEscapeRoom(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.rgb(12, 30, 12));
        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        gc.setStroke(Color.rgb(0, 80, 0));
        gc.setLineWidth(1);
        gc.strokeRect(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4);
        gc.setFill(Color.rgb(0, 100, 0, 0.6));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("S", x + 14, y + 27);
    }

    /** Renders a floor tile with subtle checkerboard pattern. */
    private void renderFloor(GraphicsContext gc, double x, double y, int row, int col) {
        boolean checker = (row + col) % 2 == 0;
        gc.setFill(checker ? Color.rgb(18, 18, 22) : Color.rgb(22, 22, 28));
        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        gc.setStroke(Color.rgb(30, 30, 36));
        gc.setLineWidth(0.3);
        gc.strokeRect(x, y, TILE_SIZE, TILE_SIZE);
    }

    // ========================= COLLISION DETECTION =========================

    /** Returns true if the given hitbox overlaps any wall tile. */
    public boolean isWallCollision(Rectangle2D nextHitbox) {
        if (mapGrid == null) return false;

        int leftCol   = (int) (nextHitbox.getMinX() / TILE_SIZE);
        int rightCol  = (int) (nextHitbox.getMaxX() / TILE_SIZE);
        int topRow    = (int) ((nextHitbox.getMinY() - Y_OFFSET) / TILE_SIZE);
        int bottomRow = (int) ((nextHitbox.getMaxY() - Y_OFFSET) / TILE_SIZE);

        if (leftCol < 0 || rightCol >= mapGrid[0].length || topRow < 0 || bottomRow >= mapGrid.length) {
            return true;
        }

        for (int r = topRow; r <= bottomRow; r++) {
            for (int c = leftCol; c <= rightCol; c++) {
                if (mapGrid[r][c] == 1) return true;
            }
        }
        return false;
    }

    /** Returns true if the center of the given hitbox is on an escape room tile (type 6). */
    public boolean isEscapeRoom(Rectangle2D playerHitbox) {
        if (mapGrid == null) return false;

        int centerX = (int) ((playerHitbox.getMinX() + playerHitbox.getMaxX()) / 2);
        int centerY = (int) ((playerHitbox.getMinY() + playerHitbox.getMaxY()) / 2);

        int col = centerX / (int) TILE_SIZE;
        int row = (int) ((centerY - Y_OFFSET) / TILE_SIZE);

        if (col < 0 || col >= mapGrid[0].length || row < 0 || row >= mapGrid.length) return false;
        return mapGrid[row][col] == 6;
    }

    // ========================= PATHFINDING (BFS) =========================

    /** BFS node for pathfinding. */
    private static class Node {
        int r, c;
        Node parent;
        Node(int r, int c, Node parent) {
            this.r = r;
            this.c = c;
            this.parent = parent;
        }
    }

    /**
     * Finds the next tile to move to on the shortest path from start to target using BFS.
     *
     * @return [row, col] of the next tile, or null if no path exists
     */
    public int[] getNextMove(int startR, int startC, int targetR, int targetC) {
        if (startR == targetR && startC == targetC) return new int[]{startR, startC};

        int rows = mapGrid.length;
        int cols = mapGrid[0].length;
        boolean[][] visited = new boolean[rows][cols];
        Queue<Node> queue = new LinkedList<>();

        queue.add(new Node(startR, startC, null));
        visited[startR][startC] = true;

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};
        Node targetNode = null;

        while (!queue.isEmpty()) {
            Node curr = queue.poll();
            if (curr.r == targetR && curr.c == targetC) {
                targetNode = curr;
                break;
            }
            for (int i = 0; i < 4; i++) {
                int nr = curr.r + dr[i];
                int nc = curr.c + dc[i];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols
                        && !visited[nr][nc] && mapGrid[nr][nc] != 1) {
                    visited[nr][nc] = true;
                    queue.add(new Node(nr, nc, curr));
                }
            }
        }

        if (targetNode == null) return null;

        // Walk back to the first step from start
        Node step = targetNode;
        while (step.parent != null && step.parent.parent != null) {
            step = step.parent;
        }
        return new int[]{step.r, step.c};
    }

    // ========================= LINE OF SIGHT =========================

    /** Returns true if there is a clear line of sight (no walls) between two points. */
    public boolean hasLineOfSight(double x1, double y1, double x2, double y2) {
        if (mapGrid == null) return false;

        double distance = Math.hypot(x2 - x1, y2 - y1);
        int steps = (int) (distance / (TILE_SIZE / 2));

        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0 : (double) i / steps;
            double cx = x1 + t * (x2 - x1);
            double cy = y1 + t * (y2 - y1);

            int col = (int) (cx / TILE_SIZE);
            int row = (int) ((cy - Y_OFFSET) / TILE_SIZE);

            if (row >= 0 && row < mapGrid.length && col >= 0 && col < mapGrid[0].length) {
                if (mapGrid[row][col] == 1) return false;
            }
        }
        return true;
    }

    // ========================= GETTERS =========================

    public int[][] getMapGrid()    { return mapGrid; }
    public int getPlayerSpawnRow() { return playerSpawnRow; }
    public int getPlayerSpawnCol() { return playerSpawnCol; }
}