public class Cell {
    private final int x, y;
    private final Type type;
    private final Grid grid;
    private boolean passed;
    private Direction dir;

    public Cell(int x, int y, Type type, Grid grid) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.grid = grid;
        passed = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Type getType() {
        return type;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public Direction getDir() {
        return dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "x=" + x +
                ", y=" + y +
                ", type=" + type +
                ", grid=" + grid +
                ", passed=" + passed +
                ", dir=" + dir +
                '}';
    }
}
