import java.util.*;

public class Grid {

    final int h, w;
    final Cell[][] cells;

    // Edge usage: used[x][y][d] = true if edge from (x,y) in direction d is part of the loop
    final boolean[][][] used;

    // Number of edges touching each cell
    final int[][] degree;

    // Active endpoints (cells with degree 1)
    final List<Cell> endpoints = new ArrayList<>();

    boolean solved = false;

    public Grid(int h, int w, Type[][] types) {
        this.h = h;
        this.w = w;
        cells = new Cell[h][w];
        used = new boolean[h][w][4];
        degree = new int[h][w];

        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                cells[i][j] = new Cell(i, j, types[i][j]);
    }

    boolean inside(int x, int y) {
        return 0 <= x && x < h && 0 <= y && y < w;
    }

    // Add or remove edges and update endpoints dynamically
    void addEdge(Cell c, Direction d) {
        int x = c.row, y = c.col;
        int nx = x + d.dx, ny = y + d.dy;

        used[x][y][d.ordinal()] = true;
        degree[x][y]++;

        used[nx][ny][d.opposite().ordinal()] = true;
        degree[nx][ny]++;

        updateEndpoint(c);
        updateEndpoint(cells[nx][ny]);
    }

    void removeEdge(Cell c, Direction d) {
        int x = c.row, y = c.col;
        int nx = x + d.dx, ny = y + d.dy;

        used[x][y][d.ordinal()] = false;
        degree[x][y]--;

        used[nx][ny][d.opposite().ordinal()] = false;
        degree[nx][ny]--;

        updateEndpoint(c);
        updateEndpoint(cells[nx][ny]);
    }

    void updateEndpoint(Cell c) {
        if (degree[c.row][c.col] == 1) {
            if (!endpoints.contains(c)) endpoints.add(c);
        } else {
            endpoints.remove(c);
        }
    }

    boolean checkBlack(Cell c) {
        // Basic check: only enforce when cell is black and has 2 edges
        if (c.type != Type.BLACK || degree[c.row][c.col] < 2) return true;

        // Collect directions used at the black cell
        List<Direction> dirs = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (used[c.row][c.col][d.ordinal()]) dirs.add(d);
        }
        if (dirs.size() != 2) return false;

        // must be a turn inside the black cell
        if (dirs.get(0).opposite() == dirs.get(1)) return false;

        // AFTER-CELL rule:
        // For each of the two exit directions, if the adjacent neighbour is finished (degree==2),
        // it MUST be straight (its two used directions are opposite) and it must include the back-edge.
        for (Direction out : dirs) {
            int nx = c.row + out.dx;
            int ny = c.col + out.dy;
            // neighbour must be on board
            if (!inside(nx, ny)) return false;

            // if neighbour isn't completed yet we can't enforce the after-rule, so skip it (conservative)
            if (degree[nx][ny] < 2) continue;

            // neighbour must have exactly 2 used directions
            List<Direction> nd = new ArrayList<>();
            for (Direction d : Direction.values()) if (used[nx][ny][d.ordinal()]) nd.add(d);
            if (nd.size() != 2) return false;

            // neighbour must be straight (two used dirs are opposites)
            if (nd.get(0).opposite() != nd.get(1)) return false;

            // neighbour must include the back-edge to the black cell
            if (!nd.contains(out.opposite())) return false;
        }

        return true;
    }

    boolean checkWhite(Cell c) {
        // Basic check: only enforce when cell is white and has 2 edges
        if (c.type != Type.WHITE || degree[c.row][c.col] < 2) return true;

        // Collect directions used at the white cell
        List<Direction> dirs = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (used[c.row][c.col][d.ordinal()]) dirs.add(d);
        }
        if (dirs.size() != 2) return false;

        // must go straight inside the white cell
        if (dirs.get(0).opposite() != dirs.get(1)) return false;

        // AFTER-CELL rule:
        // Let the two straight directions be a and b (opposites).
        // At least one of the two adjacent neighbours (in directions a or b), when completed (degree==2),
        // must be a TURN. If both neighbours are completed and both are straight, that's invalid.
        Direction a = dirs.get(0), b = dirs.get(1);
        int ax = c.row + a.dx, ay = c.col + a.dy;
        int bx = c.row + b.dx, by = c.col + b.dy;

        boolean aDone = inside(ax, ay) && degree[ax][ay] == 2;
        boolean bDone = inside(bx, by) && degree[bx][by] == 2;

        // If one or both neighbours are not yet finished, we cannot reject yet (conservative)
        if (!aDone || !bDone) return true;

        // Both neighbours are finished: check if at least one is a turn
        boolean aTurn = false, bTurn = false;

        // check neighbour A
        List<Direction> ad = new ArrayList<>();
        for (Direction d : Direction.values()) if (used[ax][ay][d.ordinal()]) ad.add(d);
        if (ad.size() != 2) return false;
        if (ad.get(0).opposite() != ad.get(1)) aTurn = true; // it's a turn
        // also ensure neighbour connects back into the white (sanity)
        if (!ad.contains(a.opposite())) return false;

        // check neighbour B
        List<Direction> bd = new ArrayList<>();
        for (Direction d : Direction.values()) if (used[bx][by][d.ordinal()]) bd.add(d);
        if (bd.size() != 2) return false;
        if (bd.get(0).opposite() != bd.get(1)) bTurn = true; // it's a turn
        if (!bd.contains(b.opposite())) return false;

        // If neither side is a turn, then white's requirement is violated
        return aTurn || bTurn;
    }

    boolean checkAllCircles() {
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                if (degree[i][j] == 2) {
                    if (!checkBlack(cells[i][j]) || !checkWhite(cells[i][j])) return false;
                }
        return true;
    }

    boolean allCirclesFinished() {
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                if (cells[i][j].type != Type.NONE && degree[i][j] != 2)
                    return false;
        return true;
    }

    // Single loop check via DFS
    boolean singleLoop() {
        int sx = -1, sy = -1;
        outer:
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                if (degree[i][j] > 0) {
                    sx = i;
                    sy = j;
                    break outer;
                }

        if (sx == -1) return false;

        boolean[][] vis = new boolean[h][w];
        int countUsed = dfsCount(sx, sy, vis);

        int totalEdges = 0;
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                if (degree[i][j] > 0) totalEdges++;

        return countUsed == totalEdges;
    }

    int dfsCount(int x, int y, boolean[][] vis) {
        vis[x][y] = true;
        int r = 1;
        for (Direction d : Direction.values()) {
            if (used[x][y][d.ordinal()]) {
                int nx = x + d.dx, ny = y + d.dy;
                if (!vis[nx][ny]) r += dfsCount(nx, ny, vis);
            }
        }
        return r;
    }

    // DFS with multiple endpoints and forced moves
    void dfs() {
        if (solved) return;

        if (allCirclesFinished() && singleLoop()) {
            solved = true;
            return;
        }

        if (endpoints.isEmpty()) return;

        // Pick the endpoint with degree 1
        Cell c = endpoints.getLast();

        // Determine forced moves if the cell touches a circle
        List<Direction> candidates = new ArrayList<>();
        for (Direction d : Direction.values()) {
            int nx = c.row + d.dx, ny = c.col + d.dy;
            if (!inside(nx, ny)) continue;
            if (used[c.row][c.col][d.ordinal()]) continue;
            if (degree[c.row][c.col] == 2) continue;
            if (degree[nx][ny] == 2) continue;

            // Black circle: force turn
            if (c.type == Type.BLACK && degree[c.row][c.col] == 1) {
                for (Direction od : Direction.values()) {
                    if (used[c.row][c.col][od.ordinal()] && d == od.opposite()) continue;
                }
            }

            // White circle: force straight
            if (c.type == Type.WHITE && degree[c.row][c.col] == 1) {
                for (Direction od : Direction.values()) {
                    if (used[c.row][c.col][od.ordinal()] && d != od && d != od.opposite()) continue;
                }
            }

            candidates.add(d);
        }

        for (Direction d : candidates) {
            addEdge(c, d);
            if (checkAllCircles())
                dfs();
            if (solved) return;
            removeEdge(c, d);
        }
    }

    public void solve() {
        // Add all black/white cells as initial endpoints to propagate constraints
        outer:
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                if (cells[i][j].type != Type.NONE) {
                    endpoints.add(cells[i][j]);
                    break outer;
                }

        dfs();
    }

    public void print() {
        char[][] buf = new char[2 * h + 1][2 * w + 1];
        for (char[] row : buf) Arrays.fill(row, ' ');

        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                int cx = 2 * x + 1;
                int cy = 2 * y + 1;
                buf[cx][cy] = switch (cells[x][y].type) {
                    case NONE -> '.';
                    case WHITE -> 'w';
                    case BLACK -> 'b';
                };

                for (Direction d : Direction.values()) {
                    if (used[x][y][d.ordinal()]) {
                        int xx = cx + d.dx;
                        int yy = cy + d.dy;
                        buf[xx][yy] = (d == Direction.UP || d == Direction.DOWN) ? '|' : '-';
                    }
                }
            }
        }

        for (char[] row : buf)
            System.out.println(new String(row));
    }
}
