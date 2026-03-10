package com.nsu.cse215l.redlolli.redlolli.map;

import java.util.LinkedList;
import java.util.Queue;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Maze {
    public static final double TILE_SIZE = 40.0;
    private int[][] mapGrid;

    // Constructor now takes the file path
    public Maze(String csvFilePath) {
        loadMapFromCSV(csvFilePath);
    }

    private void loadMapFromCSV(String path) {
        List<int[]> rowList = new ArrayList<>();

        try {
            // Read from the resources folder
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("Map file not found: " + path + ". Loading fallback map.");
                // Load a safe, enclosed fallback map so the game still runs
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

            // Convert the dynamic List into our standard 2D array
            mapGrid = new int[rowList.size()][];
            for (int i = 0; i < rowList.size(); i++) {
                mapGrid[i] = rowList.get(i);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback emergency grid so the game doesn't crash completely
            mapGrid = new int[][]{{1, 1, 1}, {1, 0, 1}, {1, 1, 1}};
        }
    }

    public void renderMaze(GraphicsContext gc) {
        if (mapGrid == null) return;

        // Set up the faint grid lines for the floor
        gc.setStroke(Color.rgb(30, 30, 30));
        gc.setLineWidth(1);

        for (int row = 0; row < mapGrid.length; row++) {
            for (int col = 0; col < mapGrid[row].length; col++) {
                double tileX = col * TILE_SIZE;
                double tileY = row * TILE_SIZE;

                if (mapGrid[row][col] == 1) {
                    // Wall styling (Dark slate color from the mockup)
                    gc.setFill(Color.rgb(45, 55, 65));
                    gc.fillRect(tileX, tileY, TILE_SIZE, TILE_SIZE);

                    // Subtle inner border to give the walls a slightly raised look
                    gc.setStroke(Color.rgb(70, 80, 90));
                    gc.strokeRect(tileX + 1, tileY + 1, TILE_SIZE - 2, TILE_SIZE - 2);

                    // Reset stroke back to faint grid color for the next floor tiles
                    gc.setStroke(Color.rgb(30, 30, 30));
                } else {
                    // Empty floor space - draw the grid cell
                    gc.strokeRect(tileX, tileY, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    public boolean isWallCollision(Rectangle2D nextHitbox) {
        if (mapGrid == null) return false;

        int leftCol = (int) (nextHitbox.getMinX() / TILE_SIZE);
        int rightCol = (int) (nextHitbox.getMaxX() / TILE_SIZE);
        int topRow = (int) (nextHitbox.getMinY() / TILE_SIZE);
        int bottomRow = (int) (nextHitbox.getMaxY() / TILE_SIZE);

        if (leftCol < 0 || rightCol >= mapGrid[0].length ||
                topRow < 0 || bottomRow >= mapGrid.length) {
            return true;
        }

        for (int r = topRow; r <= bottomRow; r++) {
            for (int c = leftCol; c <= rightCol; c++) {
                if (mapGrid[r][c] == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isHidingSpot(Rectangle2D playerHitbox) {
        if (mapGrid == null) return false;

        // Get the center of the player to check what tile they are standing on
        int centerX = (int) ((playerHitbox.getMinX() + playerHitbox.getMaxX()) / 2);
        int centerY = (int) ((playerHitbox.getMinY() + playerHitbox.getMaxY()) / 2);

        int col = centerX / (int) TILE_SIZE;
        int row = centerY / (int) TILE_SIZE;

        // Prevent OutOfBounds
        if (col < 0 || col >= mapGrid[0].length || row < 0 || row >= mapGrid.length) return false;

        // Return true if the tile is a 3
        return mapGrid[row][col] == 3;
    }

    // NOTE: In your renderMaze() method, you can optionally draw '3' tiles as
    // a dark vent grate or floorboards so the player knows they can hide there!

    private static class Node {
        int r, c;
        Node parent;
        Node(int r, int c, Node parent) {
            this.r = r;
            this.c = c;
            this.parent = parent;
        }
    }

    // Calculates the next tile the monster should move to
    public int[] getNextMove(int startR, int startC, int targetR, int targetC) {
        // If already in the same tile, no path needed
        if (startR == targetR && startC == targetC) return new int[]{startR, startC};

        int rows = mapGrid.length;
        int cols = mapGrid[0].length;
        boolean[][] visited = new boolean[rows][cols];
        Queue<Node> queue = new LinkedList<>();

        queue.add(new Node(startR, startC, null));
        visited[startR][startC] = true;

        Node targetNode = null;

        // Up, Down, Left, Right movement vectors
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            Node curr = queue.poll();

            // Target found
            if (curr.r == targetR && curr.c == targetC) {
                targetNode = curr;
                break;
            }

            // Check adjacent tiles
            for (int i = 0; i < 4; i++) {
                int nr = curr.r + dr[i];
                int nc = curr.c + dc[i];

                // If within bounds, not visited, and NOT A WALL (1)
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols
                        && !visited[nr][nc] && mapGrid[nr][nc] != 1) {
                    visited[nr][nc] = true;
                    queue.add(new Node(nr, nc, curr));
                }
            }
        }

        if (targetNode == null) return null; // No available path (player is walled off completely)

        // Backtrack from the target to find the FIRST step the monster needs to take
        Node step = targetNode;
        while (step.parent != null && step.parent.parent != null) {
            step = step.parent;
        }

        return new int[]{step.r, step.c}; // Returns the next [row, col] to move towards
    }

    // Checks if there are any walls blocking the direct path between two points
    public boolean hasLineOfSight(double x1, double y1, double x2, double y2) {
        if (mapGrid == null) return false;

        double distance = Math.hypot(x2 - x1, y2 - y1);
        int steps = (int) (distance / (TILE_SIZE / 2)); // Check every half-tile along the line

        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0 : (double) i / steps;
            double cx = x1 + t * (x2 - x1);
            double cy = y1 + t * (y2 - y1);

            int col = (int) (cx / TILE_SIZE);
            int row = (int) (cy / TILE_SIZE);

            if (row >= 0 && row < mapGrid.length && col >= 0 && col < mapGrid[0].length) {
                if (mapGrid[row][col] == 1) {
                    return false; // Vision is blocked by a wall!
                }
            }
        }
        return true; // Clear line of sight
    }

    public int[][] getMapGrid() { return mapGrid; }
}