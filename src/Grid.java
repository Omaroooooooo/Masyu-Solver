import java.util.ArrayList;

public class Grid {
    private final int height;
    private final int width;
    Cell[][] cells;
    Cell start;
    ArrayList<Line> lines;
    ArrayList<Cell> blacks;
    ArrayList<Cell> whites;

    public Grid(int height, int width, Type[][] types) {
        this.height = height;
        this.width = width;
        this.cells = new Cell[height][width];
        lines = new ArrayList<>();
        blacks = new ArrayList<>();
        whites = new ArrayList<>();
        generate(types);
        start = findStart();
    }

    private void generate(Type[][] types) {
        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                Cell cell = new Cell(i, j, types[i][j], this);
                this.cells[i][j] = cell;
                if (cell.getType() == Type.Black){
                    blacks.add(cells[i][j]);
                }
                if (cell.getType() == Type.White){
                    whites.add(cells[i][j]);
                }
            }
        }
    }

    public Cell[] getNeighbors(Cell cell){
        Cell[] neighbors = new Cell[4];
        if(cell.getY() + 1 < width){
            neighbors[0] = cells[cell.getX()][cell.getY() + 1];
        }
        if(cell.getX() + 1 < height){
            neighbors[1] = cells[cell.getX() + 1][cell.getY()];
        }
        if(cell.getY() - 1 >= 0){
            neighbors[2] = cells[cell.getX()][cell.getY() - 1];
        }
        if(cell.getX() - 1 >= 0){
            neighbors[3] = cells[cell.getX() - 1][cell.getY()];
        }
        return neighbors;
    }

    public boolean blackCirclesValid(){
        boolean valid = true;
        for (int i = 2; i < lines.size(); i++){
            if (lines.get(i).getHead().getType() == Type.Black && lines.get(i).getDir() != lines.get(i-1).getDir()){
                valid = false;
                break;
            }
            if (lines.get(i).getTail().getType() == Type.Black && lines.get(i).getDir() != lines.get((i+1) % lines.size()).getDir()){
                valid = false;
                break;
            }
            if (lines.get(i).getHead().getType() == Type.Black && lines.get(i).getDir() == lines.get((i+1) % lines.size()).getDir()){
                valid = false;
                break;
            }
            if (lines.get(i).getTail().getType() == Type.Black && lines.get(i).getDir() == lines.get(i-1).getDir()){
                valid = false;
                break;
            }
        }
        return valid;
    }

    public boolean whiteCirclesValid(){
        boolean valid = true;
        for (int i = 2; i < lines.size(); i++){
            if (lines.get(i).getHead().getType() == Type.White && lines.get(i).getDir() != lines.get((i+1) % lines.size()).getDir()){
                valid = false;
                break;
            }
            if (lines.get(i).getTail().getType() == Type.White && lines.get(i).getDir() != lines.get(i-1).getDir()){
                valid = false;
                break;
            }
            if (lines.get(i).getHead().getType() == Type.White && lines.get(i).getDir() == lines.get(i-1).getDir() && lines.get((i+2) % lines.size()).getDir() == lines.get(i).getDir()){
                valid = false;
                break;
            }
            if (lines.get(i).getTail().getType() == Type.White && lines.get(i).getDir() == lines.get((i+1) % lines.size()).getDir() && lines.get(i-2).getDir() == lines.get(i).getDir()){
                valid = false;
                break;
            }
        }
        return valid;
    }

    public boolean allCirclesValid(){
        return blackCirclesValid() && whiteCirclesValid();
    }

    public boolean isOneLoop(){
        boolean loop = true;
        for (int i = 0; i < lines.size() - 1; i++){
            if (lines.get(i).getHead() != lines.get(i + 1).getTail()) {
                loop = false;
                break;
            }
        }
        if (lines.get(lines.size()-1).getHead() != lines.get(0).getTail()){
            loop = false;
        }
        return loop;
    }

    public boolean allCirclesPassed(){
        for (int i = 0; i < height - 1; i++){
            for (int j = 0; j < width - 1; j++){
                if ((cells[i][j].getType() == Type.Black || cells[i][j].getType() == Type.White) && cells[i][j].isPassed()){
                    return true;
                }
            }
        }
        return false;
    }

    public Cell findStart(){
        if (whites.isEmpty()){
            return blacks.get(0);
        }
        if (blacks.isEmpty()){
            return whites.get(0);
        }
        if (blacks.get(0).getX() < whites.get(0).getX()){
            return blacks.get(0);
        }
        if (whites.get(0).getX() < blacks.get(0).getX()){
            return whites.get(0);
        }
        if (whites.get(0).getX() == blacks.get(0).getX() && blacks.get(0).getY() < whites.get(0).getY()){
            return blacks.get(0);
        }
        return whites.get(0);
    }

    public boolean isBranching(Line line){
        // Direction dir = this.getHead().getDir();
        // if (this.dir != dir || this.dir != dir.getOpposite()){
        //     return true;
        // }
        if (line.getHead().isPassed() == true && line.getHead() != start){
            return true;
        }
        return false;
    }

    public void DFS(Cell cell){
        Cell[] neighbors = getNeighbors(cell);
        for (int i = 0; i < neighbors.length; i++){
            if (neighbors[i] == null || neighbors[i].isPassed()){
                continue;
            }
            Line line = new Line(cell, neighbors[i], this);
            if (isBranching(line)){
                continue;
            }
            lines.add(line);
            if (!allCirclesValid()){
                line.getTail().setPassed(false);
                lines.remove(line);
                continue;
            }
            if (allCirclesPassed() && isOneLoop()){
                return;
            }
            // should remove line if backtracking and set isPassed true when loop found
            DFS(neighbors[i]);
            if (!allCirclesPassed() || !isOneLoop()){
                start.setPassed(false);
                line.getTail().setPassed(false);
                lines.remove(line);
            }
        }
    }
    public void solve(){
        Cell[] neighbors = getNeighbors(start);
        for (int i = 0; i < neighbors.length; i++){
            if (neighbors[i] == null){
                continue;
            }
            Line line = new Line(start, neighbors[i], this);
            lines.add(line);

            DFS(neighbors[i]);
            if (allCirclesPassed() && isOneLoop()){
                return;
            }

            start.setPassed(false);
            lines.remove(line);


        }
    }
}
