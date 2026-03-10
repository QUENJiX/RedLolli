package com.nsu.cse215l.redlolli.redlolli.map;

import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;
import java.util.List;

public class Maze {
    private int[][] grid;
    private double tileSize = 40.0;
    private List<Tile> tiles = new ArrayList<>();

    public Maze(int[][] layout) {
        this.grid = layout;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                tiles.add(new Tile(grid[i][j], j * tileSize, i * tileSize, tileSize));
            }
        }
    }

    public void render(GraphicsContext gc) {
        for (Tile t : tiles) t.render(gc);
    }

    // This is vital for collision!
    public boolean isWall(double pixelX, double pixelY) {
        int col = (int) (pixelX / tileSize);
        int row = (int) (pixelY / tileSize);

        if (row < 0 || row >= grid.length || col < 0 || col >= grid[0].length) return true;
        return grid[row][col] == Tile.WALL;
    }
}