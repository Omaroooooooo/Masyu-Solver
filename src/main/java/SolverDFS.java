

public class SolverDFS {

    public final Grid grid;

    public SolverDFS(Grid grid) {
        this.grid = grid;
    }

    // Depth-First Search expanding from the endpoint, trying every possible direction, and pruning if any rule is broken
    void dfs() {
        if (grid.solved) return;

        if (grid.allCirclesFinished() && grid.singleLoop()) {
            grid.solved = true;
            return;
        }

        if (grid.endpoints.isEmpty()) return;

        // Pick the endpoint with degree 1
        Cell c = grid.endpoints.getLast();

        // Neighbor coordinates
        int nx, ny;

        // UP
        nx = c.row - 1;
        ny = c.col;
        if (grid.inside(nx, ny))
            if (!grid.vert[nx][ny])
                if (grid.degree[nx][ny] < 2){
                    grid.setVert(grid.cells[nx][ny], true);
                    if (grid.checkAllCircles())
                        dfs();
                    if (grid.solved) return;
                    grid.setVert(grid.cells[nx][ny], false); // backtrack
                }

        // RIGHT
        nx = c.row;
        ny = c.col + 1;
        if (grid.inside(nx, ny))
            if (!grid.horiz[c.row][c.col])
                if (grid.degree[nx][ny] < 2){
                    grid.setHoriz(grid.cells[c.row][c.col], true);
                    if (grid.checkAllCircles())
                        dfs();
                    if (grid.solved) return;
                    grid.setHoriz(grid.cells[c.row][c.col], false); // backtrack
                }

        // DOWN
        nx = c.row + 1;
        ny = c.col;
        if (grid.inside(nx, ny))
            if (!grid.vert[c.row][c.col])
                if (grid.degree[nx][ny] < 2) {
                    grid.setVert(grid.cells[c.row][c.col], true);
                    if (grid.checkAllCircles())
                        dfs();
                    if (grid.solved) return;
                    grid.setVert(grid.cells[c.row][c.col], false); // backtrack
                }

        // LEFT
        nx = c.row;
        ny = c.col - 1;
        if (grid.inside(nx, ny))
            if (!grid.horiz[nx][ny])
                if (grid.degree[nx][ny] < 2){
                    grid.setHoriz(grid.cells[nx][ny], true);
                    if (grid.checkAllCircles())
                        dfs();
                    if (grid.solved) return;
                    grid.setHoriz(grid.cells[nx][ny], false); // backtrack
                }
    }

    public void solve() {
        // Find the closest circle and start DFS from there.
        outer:
        for (int i = 0; i < grid.h; i++)
            for (int j = 0; j < grid.w; j++)
                if (grid.cells[i][j].type != Type.NONE) {
                    grid.endpoints.add(grid.cells[i][j]);
                    break outer;
                }

        dfs();
    }
}
