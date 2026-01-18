import java.util.*;

public class Grid {

    final int h, w;
    final Cell[][] cells;

    // Edge usage
    final boolean[][] horiz; // horiz[r][c] is true if there is a horizontal edge between r,c and r,c+1
    final boolean[][] vert; // vert[r][c] is true if there is a vertical edge between r,c and r+1,c

    // Number of edges touching each cell
    final int[][] degree;

    // Active endpoints (cells with degree 1)
    final List<Cell> endpoints;

    boolean solved = false;

    int circles;

    public Grid(int h, int w, Type[][] types) {
        this.h = h;
        this.w = w;
        circles = 0;
        cells = new Cell[h][w];
        horiz = new boolean[h][w-1];
        vert = new boolean[h-1][w];
        degree = new int[h][w];
        endpoints = new ArrayList<>();

        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++){
                cells[i][j] = new Cell(i, j, types[i][j]);
                if (types[i][j] != Type.NONE) circles++;
            }
    }

    // Helper function to check if coordinates are inside the grid
    boolean inside(int x, int y) {
        return 0 <= x && x < h && 0 <= y && y < w;
    }

    // Adding or removing a horizontal edge
    public void setHoriz(Cell c, boolean val) {
        if (horiz[c.row][c.col] == val) return;

        horiz[c.row][c.col] = val;

        if (val){
            degree[c.row][c.col]++;
            degree[c.row][c.col+1]++;
        } else {
            degree[c.row][c.col]--;
            degree[c.row][c.col+1]--;
        }

        updateEndpoint(c);
        updateEndpoint(cells[c.row][c.col+1]);
    }

    // Adding or removing a vertical edge
    public void setVert(Cell c, boolean val) {
        if (vert[c.row][c.col] == val) return;

        vert[c.row][c.col] = val;

        if (val){
            degree[c.row][c.col]++;
            degree[c.row+1][c.col]++;
        } else {
            degree[c.row][c.col]--;
            degree[c.row+1][c.col]--;
        }

        updateEndpoint(c);
        updateEndpoint(cells[c.row+1][c.col]);
    }

    void updateEndpoint(Cell c) {
        if (degree[c.row][c.col] == 1) {
            if (!endpoints.contains(c)) endpoints.add(c);
        } else {
            endpoints.remove(c);
        }
    }

    // Check black circle rules
    boolean checkBlack(Cell c) {
        // Basic check: only enforce when cell is black and has 2 edges
        if (c.type != Type.BLACK || degree[c.row][c.col] < 2) return true;

        boolean up = c.row > 0 && vert[c.row-1][c.col];
        boolean down = c.row < h - 1 && vert[c.row][c.col];
        boolean left = c.col > 0 && horiz[c.row][c.col-1];
        boolean right = c.col < w - 1 && horiz[c.row][c.col];

        // must be a turn inside the black cell
        if ((up && down) || (left && right)) return false;

        // Neighbors
        Cell n1, n2;

        // First Neighbor is either above or below, should not be out of bounds, and must have degree 2
        if (up) {
            n1 = (inside(c.row-1, c.col) && degree[c.row-1][c.col] == 2) ? cells[c.row-1][c.col] : null;
        } else {
            n1 = (inside(c.row+1, c.col) && degree[c.row+1][c.col] == 2) ? cells[c.row+1][c.col] : null;
        }

        // Second neighbor is  either left or right, should not be out of bounds, and must have degree 2.
        if (left) {
            n2 = (inside(c.row, c.col-1) && degree[c.row][c.col-1] == 2) ? cells[c.row][c.col-1] : null;
        } else {
            n2 = (inside(c.row,c.col+1) && degree[c.row][c.col+1] == 2) ? cells[c.row][c.col+1] : null;
        }

        // Loop must go straight at both neighbors
        if (n1 != null){
            boolean up1 = n1.row > 0 && vert[n1.row-1][n1.col];
            boolean down1 = n1.row < h - 1 && vert[n1.row][n1.col];
            boolean left1 = n1.col > 0 && horiz[n1.row][n1.col-1];
            boolean right1 = n1.col < w - 1 && horiz[n1.row][n1.col];

            if ((up1 && right1) || (right1 && down1) || (down1 && left1) || (left1 && up1)) return false;
        }
        if (n2 != null){
            boolean up2 = n2.row > 0 && vert[n2.row-1][n2.col];
            boolean down2 = n2.row < h - 1 && vert[n2.row][n2.col];
            boolean left2 = n2.col > 0 && horiz[n2.row][n2.col-1];
            boolean right2 = n2.col < w - 1 && horiz[n2.row][n2.col];

            if ((up2 && right2) || (right2 && down2) || (down2 && left2) || (left2 && up2)) return false;
        }

        return true;
    }

    // Check white circle rules
    boolean checkWhite(Cell c) {
        // Basic check: only enforce when cell is white and has 2 edges
        if (c.type != Type.WHITE || degree[c.row][c.col] < 2) return true;

        boolean up = c.row > 0 && vert[c.row-1][c.col];
        boolean down = c.row < h - 1 && vert[c.row][c.col];
        boolean left = c.col > 0 && horiz[c.row][c.col-1];
        boolean right = c.col < w - 1 && horiz[c.row][c.col];

        // must be a straight line inside the white cell
        if ((up && right) || (right && down) || (down && left) || (left && up)) return false;

        // Neighbors
        Cell n1, n2;

        // Neighbors are either up and down or left and right, should not be out of bounds, and must have degree 2
        if (up) {
            n1 = (inside(c.row-1, c.col) && degree[c.row-1][c.col] == 2) ? cells[c.row-1][c.col] : null;
            n2 = (inside(c.row+1, c.col) && degree[c.row+1][c.col] == 2) ? cells[c.row+1][c.col] : null;
        } else {
            n1 = (inside(c.row, c.col-1) && degree[c.row][c.col-1] == 2) ? cells[c.row][c.col-1] : null;
            n2 = (inside(c.row,c.col+1) && degree[c.row][c.col+1] == 2) ? cells[c.row][c.col+1] : null;
        }

        // Loop must turn at one neighbor at least
        boolean n1Turn = true, n2Turn = true;
        if (n1 != null){
            boolean up1 = n1.row > 0 && vert[n1.row-1][n1.col];
            boolean down1 = n1.row < h - 1 && vert[n1.row][n1.col];
            boolean left1 = n1.col > 0 && horiz[n1.row][n1.col-1];
            boolean right1 = n1.col < w - 1 && horiz[n1.row][n1.col];

            if ((up1 && down1) || (left1 && right1)) n1Turn = false;
        }
        if (n2 != null){
            boolean up2 = n2.row > 0 && vert[n2.row-1][n2.col];
            boolean down2 = n2.row < h - 1 && vert[n2.row][n2.col];
            boolean left2 = n2.col > 0 && horiz[n2.row][n2.col-1];
            boolean right2 = n2.col < w - 1 && horiz[n2.row][n2.col];

            if ((up2 && down2) || (left2 && right2)) n2Turn = false;
        }

        return n1Turn || n2Turn;
    }

    // Check the rules for all black and white circles
    boolean checkAllCircles() {
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                if (degree[i][j] == 2) {
                    if (!checkBlack(cells[i][j]) || !checkWhite(cells[i][j])) return false;
                }
        return true;
    }

    // Check that the loop goes through all circles, i.e. they have degree 2
    boolean allCirclesFinished() {
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                if (cells[i][j].type != Type.NONE && degree[i][j] != 2)
                    return false;
        return true;
    }

    // Single loop check. For DFS, it is sufficient to check if all degrees are either 0 or 2 and at least one cell has degree 2
    boolean singleLoop() {
        boolean atLeastOneCell = false;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (degree[i][j] != 0 && degree[i][j] != 2){
                    return false;
                } else {
                    if (degree[i][j] == 2){
                        atLeastOneCell = true;
                    }
                }
            }
        }
        return atLeastOneCell;
    }

    public void print() {
        char[][] buf = new char[2 * h + 1][2 * w + 1];
        for (char[] row : buf) Arrays.fill(row, ' ');

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {

                int cx = 2 * r + 1;   // printable center row
                int cy = 2 * c + 1;   // printable center col

                // Draw circle / empty
                buf[cx][cy] = switch (cells[r][c].type) {
                    case NONE  -> '+';
                    case WHITE -> 'w';
                    case BLACK -> 'b';
                };

                // ---- VERTICAL EDGES ----
                // Up (edge stored at vert[r-1][c])
                if (r > 0 && vert[r - 1][c]) {
                    buf[cx - 1][cy] = '|';
                }

                // Down (edge stored at vert[r][c])
                if (r < h - 1 && vert[r][c]) {
                    buf[cx + 1][cy] = '|';
                }

                // ---- HORIZONTAL EDGES ----
                // Left (edge stored at horiz[r][c-1])
                if (c > 0 && horiz[r][c - 1]) {
                    buf[cx][cy - 1] = '-';
                }

                // Right (edge stored at horiz[r][c])
                if (c < w - 1 && horiz[r][c]) {
                    buf[cx][cy + 1] = '-';
                }
            }
        }

        for (char[] row : buf)
            System.out.println(new String(row));
    }

}
