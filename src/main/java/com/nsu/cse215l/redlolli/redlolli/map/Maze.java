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
    public static final double Y_OFFSET = 50.0;
    private int[][] mapGrid;
    private int playerSpawnRow = 1;
    private int playerSpawnCol = 1;

    public Maze(String csvFilePath) {
        loadMapFromCSV(csvFilePath);
    }

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

            // Find player spawn tile (7) and convert to walkable
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

    public int getPlayerSpawnRow() { return playerSpawnRow; }
    public int getPlayerSpawnCol() { return playerSpawnCol; }

    public void renderMaze(GraphicsContext gc) {
        if (mapGrid == null) return;

        gc.setStroke(Color.rgb(30, 30, 30));
        gc.setLineWidth(1);

        for (int row = 0; row < mapGrid.length; row++) {
            for (int col = 0; col < mapGrid[row].length; col++) {
                double tileX = col * TILE_SIZE;
                double tileY = row * TILE_SIZE + Y_OFFSET;

                if (mapGrid[row][col] == 1) {
                    gc.setFill(Color.rgb(45, 55, 65));
                    gc.fillRect(tileX, tileY, TILE_SIZE, TILE_SIZE);
                    gc.setStroke(Color.rgb(70, 80, 90));
                    gc.strokeRect(tileX + 1, tileY + 1, TILE_SIZE - 2, TILE_SIZE - 2);
                    gc.setStroke(Color.rgb(30, 30, 30));
                } else if (mapGrid[row][col] == 6) {
                    // Escape room door - greenish safe zone indicator
                    gc.setFill(Color.rgb(15, 35, 15));
                    gc.fillRect(tileX, tileY, TILE_SIZE, TILE_SIZE);
                    gc.setStroke(Color.rgb(0, 80, 0));
                    gc.strokeRect(tileX + 2, tileY + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                    gc.setFill(Color.rgb(0, 100, 0, 0.6));
                    gc.fillText("S", tileX + 14, tileY + 27);
                    gc.setStroke(Color.rgb(30, 30, 30));
                } else {
                    gc.strokeRect(tileX, tileY, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    public boolean isWallCollision(Rectangle2D nextHitbox) {
        if (mapGrid == null) return false;

        int leftCol = (int) (nextHitbox.getMinX() / TILE_SIZE);
        int rightCol = (int) (nextHitbox.getMaxX() / TILE_SIZE);
        int topRow = (int) ((nextHitbox.getMinY() - Y_OFFSET) / TILE_SIZE);
        int bottomRow = (int) ((nextHitbox.getMaxY() - Y_OFFSET) / TILE_SIZE);

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

    public boolean isEscapeRoom(Rectangle2D playerHitbox) {
        if (mapGrid == null) return false;

        int centerX = (int) ((playerHitbox.getMinX() + playerHitbox.getMaxX()) / 2);
        int centerY = (int) ((playerHitbox.getMinY() + playerHitbox.getMaxY()) / 2);

        int col = centerX / (int) TILE_SIZE;
        int row = (int) ((centerY - Y_OFFSET) / TILE_SIZE);

        if (col < 0 || col >= mapGrid[0].length || row < 0 || row >= mapGrid.length) return false;

        return mapGrid[row][col] == 6;
    }

    private static class Node {
        int r, c;
        Node parent;
        Node(int r, int c, Node parent) {
            this.r = r;
            this.c = c;
            this.parent = parent;
        }
    }

    public int[] getNextMove(int startR, int startC, int targetR, int targetC) {
        if (startR == targetR && startC == targetC) return new int[]{startR, startC};

        int rows = mapGrid.length;
        int cols = mapGrid[0].length;
        boolean[][] visited = new boolean[rows][cols];
        Queue<Node> queue = new LinkedList<>();

        queue.add(new Node(startR, startC, null));
        visited[startR][startC] = true;

        Node targetNode = null;

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

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

        Node step = targetNode;
        while (step.parent != null && step.parent.parent != null) {
            step = step.parent;
        }

        return new int[]{step.r, step.c};
    }

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
                if (mapGrid[row][col] == 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[][] getMapGrid() { return mapGrid; }
}