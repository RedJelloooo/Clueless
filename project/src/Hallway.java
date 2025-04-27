import java.util.Objects;

/**
 * The Hallway class represents a connection between two adjacent rooms
 * on the Clue-Less game board. A hallway allows players to move between rooms.
 *
 * Hallways are uniquely identified by the two rooms (by their row and column coordinates) they connect.
 *
 * Two Hallways are considered equal if they connect the same pair of rooms, regardless of the order.
 *
 *  *  Authors:
 *  *   - Albert Rojas
 */
public class Hallway {
    private final int row1, col1;
    private final int row2, col2;


    /**
     * Constructs a Hallway connecting two specified room coordinates.
     *
     * @param row1 the row of the first room
     * @param col1 the column of the first room
     * @param row2 the row of the second room
     * @param col2 the column of the second room
     */
    public Hallway(int row1, int col1, int row2, int col2) {
        int[] sorted = sortCoords(row1, col1, row2, col2);
        this.row1 = sorted[0];
        this.col1 = sorted[1];
        this.row2 = sorted[2];
        this.col2 = sorted[3];
    }

    /**
     * Sorts the two room coordinates to ensure a consistent ordering.
     * This guarantees that Hallways are equal regardless of the order the rooms were specified.
     *
     * @param r1 row of the first room
     * @param c1 column of the first room
     * @param r2 row of the second room
     * @param c2 column of the second room
     * @return an array containing the sorted coordinates
     */
    private int[] sortCoords(int r1, int c1, int r2, int c2) {
        if (r1 < r2 || (r1 == r2 && c1 < c2)) {
            return new int[]{r1, c1, r2, c2};
        } else {
            return new int[]{r2, c2, r1, c1};
        }
    }

    /**
     * Determines if two Hallway objects are equal.
     *
     * Two Hallways are equal if they connect the same two rooms.
     *
     * @param o the object to compare to
     * @return true if the Hallways are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Hallway other)) return false;
        boolean isEqual = row1 == other.row1 && col1 == other.col1 &&
                row2 == other.row2 && col2 == other.col2;
        if (!isEqual) {
            System.out.printf("Hallway mismatch: [%d,%d -> %d,%d] vs [%d,%d -> %d,%d]%n",
                    row1, col1, row2, col2,
                    other.row1, other.col1, other.row2, other.col2);
        }
        return isEqual;
    }


    /**
     * Computes a hash code for the Hallway, based on the two room coordinates.
     *
     * @return the hash code of the Hallway
     */
    @Override
    public int hashCode() {
        return Objects.hash(row1, col1, row2, col2);
    }
}
