import java.util.HashSet;
import java.util.Set;

public class Room {
    private final String name;
    private final int row, col;
    private final Set<String> occupants = new HashSet<>();

    public Room(String name, int row, int col) {
        this.name = name;
        this.row = row;
        this.col = col;
    }

    public String getName() { return name; }
    public int getRow() { return row; }
    public int getCol() { return col; }

    public boolean isOccupied() {
        return !occupants.isEmpty();
    }

    public void addOccupant(String playerId) {
        occupants.add(playerId);
    }

    public void removeOccupant(String playerId) {
        occupants.remove(playerId);
    }

    public Set<String> getOccupants() {
        return new HashSet<>(occupants); // Defensive copy
    }
}
