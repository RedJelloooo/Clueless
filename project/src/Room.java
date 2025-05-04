import java.util.HashSet;
import java.util.Set;

/**
 * The Room class represents a room or hallway on the Clue-Less game board.
 * <p>
 * Each Room has a name, a position (row and column), and a set of player IDs representing occupants.
 * <p>
 * Rooms can be normal rooms (e.g., "Study", "Kitchen") or hallways connecting rooms.
 * <p>
 * * Authors:
 *  * - Albert Rojas
 *
 */
public class Room {
    private final String name;
    private final int row, col;
    private final Set<String> occupants = new HashSet<>();


    /**
     * Constructs a Room with the specified name and board coordinates.
     *
     * @param name the name of the room or hallway
     * @param row the row index on the game board
     * @param col the column index on the game board
     */
    public Room(String name, int row, int col) {
        this.name = name;
        this.row = row;
        this.col = col;
    }

    /**
     * Gets the name of the room.
     *
     * @return the room's name
     */
    public String getName() { return name; }

    /**
     * Gets the row index of the room on the board.
     *
     * @return the row position
     */
    public int getRow() { return row; }

    /**
     * Gets the column index of the room on the board.
     *
     * @return the column position
     */
    public int getCol() { return col; }

    /**
     * Checks whether the room currently has any occupants.
     *
     * @return true if at least one player is occupying the room, false otherwise
     */
    public boolean isOccupied() {
        return !occupants.isEmpty();
    }

    /**
     * Adds a player to the set of occupants in the room.
     *
     * @param playerId the ID of the player to add
     */
    public void addOccupant(String playerId) {
        occupants.add(playerId);
    }

    /**
     * Removes a player from the set of occupants in the room.
     *
     * @param playerId the ID of the player to remove
     */
    public void removeOccupant(String playerId) {
        occupants.remove(playerId);
    }

    /**
     * Gets a copy of the current occupants of the room.
     *
     * @return a new Set containing the player IDs of the occupants
     */
    public Set<String> getOccupants() {
        return new HashSet<>(occupants); // Defensive copy
    }
}
