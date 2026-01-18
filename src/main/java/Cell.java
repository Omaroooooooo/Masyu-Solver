public class Cell {
    public final int row, col;
    public final Type type;

    public Cell(int x, int y, Type type) {
        this.row = x;
        this.col = y;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "row=" + row +
                ", col=" + col +
                ", type=" + type +
                '}';
    }
}
