public enum Direction {
    UP,
    DOWN,
    RIGHT,
    LEFT;

    public Direction getOpposite() {
        if (this == RIGHT) {
            return LEFT;
        } else if (this == LEFT) {
            return RIGHT;
        } else if (this == UP) {
            return DOWN;
        }
        return UP;
    }
}

