import com.google.ortools.Loader;
import com.google.ortools.sat.*;


// java utilities
import java.util.ArrayList;
import java.util.List;

public class SolverCP {

    public final Grid grid;
    private final CpModel model;
    private BoolVar[][] hVars; // [h][w-1]
    private BoolVar[][] vVars; // [h-1][w]
    boolean[][] visited;
    List<BoolVar> loopEdges;
    int circles;


    public SolverCP(Grid grid) {
        Loader.loadNativeLibraries();
        this.grid = grid;
        this.model = new CpModel();
        loopEdges = new ArrayList<>();
    }

    public void buildModel(){
        int h = grid.h;
        int w = grid.w;

        // create BoolVars for edges
        hVars = new BoolVar[h][w - 1];
        vVars = new BoolVar[h - 1][w];

        // horizontal edges
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w - 1; c++) {
                hVars[r][c] = model.newBoolVar("H_" + r + "_" + c);
            }
        }
        // vertical edges
        for (int r = 0; r < h - 1; r++) {
            for (int c = 0; c < w; c++) {
                vVars[r][c] = model.newBoolVar("V_" + r + "_" + c);
            }
        }

        // create IntVars for degrees
        IntVar[][] deg = new IntVar[h][w];
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {

                // if deg > 2 then the loop would branch and break the rules
                deg[r][c] = model.newIntVar(0, 2, "deg_" + r + "_" + c);

                List<BoolVar> incident = new ArrayList<>(4);
                if (r > 0) incident.add(vVars[r - 1][c]);          // up
                if (c < w - 1) incident.add(hVars[r][c]);          // right
                if (r < h - 1) incident.add(vVars[r][c]);          // down
                if (c > 0) incident.add(hVars[r][c - 1]);          // left

                // deg[r][c] should be equal to incident edges of cells[r][c]
                model.addEquality(deg[r][c], LinearExpr.sum(incident.toArray(new BoolVar[0])));

                // deg can only be 0 or 2 for a solved state
                model.addDifferent(deg[r][c], 1);
            }
        }

        // line should enter and exit all circles, i.e. circles have degree 2
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (grid.cells[r][c].type != Type.NONE){
                    model.addEquality(deg[r][c], 2);
                }
            }
        }

        // white circle rules
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (grid.cells[r][c].type == Type.WHITE){
                    // line should go straight through the circle
                    if (c > 0 && c < w - 1) model.addEquality(hVars[r][c - 1], hVars[r][c]);
                    if (r > 0 && r < h - 1) model.addEquality(vVars[r - 1][c], vVars[r][c]);

                    // line should make a turn at either side or both
                    if (r > 1 && r < h - 2) model.addBoolOr(new Literal[]{vVars[r-2][c].not(),vVars[r+1][c].not()}).onlyEnforceIf(vVars[r][c]);
                    if (c > 1 && c < w - 2) model.addBoolOr(new Literal[]{hVars[r][c-2].not(),hVars[r][c+1].not()}).onlyEnforceIf(hVars[r][c]);
                }
            }
        }

        // black circle rules
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (grid.cells[r][c].type == Type.BLACK){
                    // line should not go straight through the circle
                    if (c > 0 && c < w - 1) model.addDifferent(hVars[r][c - 1], hVars[r][c]);
                    if (r > 0 && r < h - 1) model.addDifferent(vVars[r - 1][c], vVars[r][c]);

                    // line should go straight at least one time after exiting the circle
                    // UP direction
                    if (r > 0) {
                        if (r > 1) {
                            model.addImplication(vVars[r - 1][c], vVars[r - 2][c]);
                        } else {
                            model.addEquality(vVars[r - 1][c], 0);
                        }
                    }
                    // DOWN direction
                    if (r < h - 1) {
                        if (r < h - 2) {
                            model.addImplication(vVars[r][c], vVars[r + 1][c]);
                        } else {
                            model.addEquality(vVars[r][c], 0);
                        }
                    }
                    // LEFT direction
                    if (c > 0) {
                        if (c > 1) {
                            model.addImplication(hVars[r][c - 1], hVars[r][c - 2]);
                        } else {
                            model.addEquality(hVars[r][c - 1], 0);
                        }
                    }
                    // RIGHT direction
                    if (c < w - 1) {
                        if (c < w - 2) {
                            model.addImplication(hVars[r][c], hVars[r][c + 1]);
                        } else {
                            model.addEquality(hVars[r][c], 0);
                        }
                    }
                }
            }
        }
    }

    // helper function for traversing loops, marking vertices as visited, and storing loop edges.
    public List<BoolVar> traverse(int r, int c){
        int nr = r;
        int nc = c;
        int prevR = r;
        int prevC = c;
        if (grid.cells[r][c].type != Type.NONE) circles++;
        if (grid.horiz[r][c]) {
            nc = c + 1;
            loopEdges.add(hVars[r][c]);
        } else {
            nr = r + 1;
            loopEdges.add(vVars[r][c]);
        }

        while (nr != r || nc != c){
            if (grid.cells[nr][nc].type != Type.NONE) circles++;
            visited[nr][nc] = true;
            if (nc < grid.w - 1)
                if (grid.horiz[nr][nc])
                    if (nc + 1 != prevC) {
                        loopEdges.add(hVars[nr][nc]);
                        prevR = nr;
                        prevC = nc;
                        nc = nc + 1;
                        continue;
                    }
            if (nr < grid.h - 1)
                if (grid.vert[nr][nc])
                    if (nr + 1 != prevR) {
                        loopEdges.add(vVars[nr][nc]);
                        prevR = nr;
                        prevC = nc;
                        nr = nr + 1;
                        continue;
                    }
            if (nc > 0)
                if (grid.horiz[nr][nc - 1])
                    if(nc - 1 != prevC) {
                        loopEdges.add(hVars[nr][nc - 1]);
                        prevR = nr;
                        prevC = nc;
                        nc = nc - 1;
                        continue;
                    }
            if (nr > 0)
                if (grid.vert[nr - 1][nc])
                    if (nr - 1 != prevR) {
                        loopEdges.add(vVars[nr - 1][nc]);
                        prevR = nr;
                        prevC = nc;
                        nr = nr - 1;
                    }

        }
        return loopEdges;
    }

    // helper function to detect and count loops
    public boolean loopCount(){
        visited = new boolean[grid.h][grid.w];
        int loopCount = 0;
        for (int r = 0; r < grid.h; r++){
            for (int c = 0; c < grid.w; c++){
                if (grid.degree[r][c] == 2 && !visited[r][c]){
                    loopCount++;
                    circles = 0;
                    visited[r][c] = true;
                    loopEdges.clear();
                    loopEdges = traverse(r, c);

                    // if the loop crosses all circles, skip the constraint
                    if (circles == grid.circles) continue;

                    // solver should not use all edges of the loop, i.e. forbid the loop
                    model.addLessThan(LinearExpr.sum(loopEdges.toArray(new BoolVar[0])), loopEdges.size());
                }
            }
        }
        return loopCount == 1;
    }

    public void solve() {
        buildModel();

        double wallTime = 0;

        while (true){
            CpSolver solver = new CpSolver();
            CpSolverStatus status = solver.solve(model);

            if (status != CpSolverStatus.OPTIMAL && status != CpSolverStatus.FEASIBLE){
                System.out.println("No Solution");
                return;
            }

            // copy solver values to the grid
            for (int r = 0; r < grid.h; r++) {
                for (int c = 0; c < grid.w - 1; c++) {
                    boolean value = solver.booleanValue(hVars[r][c]);
                    grid.setHoriz(grid.cells[r][c], value);
                }
            }
            for (int r = 0; r < grid.h - 1; r++) {
                for (int c = 0; c < grid.w; c++) {
                    boolean value = solver.booleanValue(vVars[r][c]);
                    grid.setVert(grid.cells[r][c], value);
                }
            }

            wallTime+= solver.wallTime();

            if (loopCount()) {
                grid.print();
                System.out.println("Wall Time : " + wallTime + " s");
                return;
            }
        }
    }
}
