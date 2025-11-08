public class Line {
    private final Cell tail, head;
    private Direction dir;
    private Grid grid;

    public Line(Cell tail, Cell head, Grid grid) {
        this.tail = tail;
        this.head = head;
        this.grid = grid;
        if (tail.getX() > head.getX()){
            dir = Direction.UP;
        }
        if (tail.getX() < head.getX()){
            dir = Direction.DOWN;
        }
        if (tail.getY() > head.getY()){
            dir = Direction.LEFT;
        }
        if (tail.getY() < head.getY()) {
            dir = Direction.RIGHT;
        }
        tail.setDir(dir);
        tail.setPassed(true);
    }

    public Cell getTail() {
        return tail;
    }

    public Cell getHead() {
        return head;
    }

    public Direction getDir() {
        return dir;
    }



    @Override
    public String toString() {
        return "Line{" +
                "tail=" + tail +
                ", head=" + head +
                ", dir=" + dir +
                '}';
    }
}
