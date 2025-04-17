public class Room {
    private final String name;
    private final int row, col;
    private boolean occupied;

    public Room(String name, int row, int col) {
        this.name = name;
        this.row = row;
        this.col = col;
        this.occupied = false;
    }

    public String getName() { return name; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }
}
