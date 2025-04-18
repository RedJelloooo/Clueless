import java.util.Objects;

public class Hallway {
    private final int row1, col1;
    private final int row2, col2;

    public Hallway(int row1, int col1, int row2, int col2) {
        int[] sorted = sortCoords(row1, col1, row2, col2);
        this.row1 = sorted[0];
        this.col1 = sorted[1];
        this.row2 = sorted[2];
        this.col2 = sorted[3];
    }

    private int[] sortCoords(int r1, int c1, int r2, int c2) {
        if (r1 < r2 || (r1 == r2 && c1 < c2)) {
            return new int[]{r1, c1, r2, c2};
        } else {
            return new int[]{r2, c2, r1, c1};
        }
    }


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


    @Override
    public int hashCode() {
        return Objects.hash(row1, col1, row2, col2);
    }
}
