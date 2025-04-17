import java.util.*;

public class GameBoard {

    public static final int SIZE = 3;

    private final Room[][] rooms;
    private final Set<Hallway> hallways;
    private final Map<String, PlayerState> playerPositions; // key = player name/ID

    public GameBoard() {
        this.rooms = new Room[SIZE][SIZE];
        this.hallways = new HashSet<>();
        this.playerPositions = new HashMap<>();
        initializeRooms();
        initializeHallways();
    }

    private void initializeRooms() {
        // Create 3x3 rooms with names for fun
        String[][] names = {
                {"Study", "Hall", "Lounge"},
                {"Library", "Billiard Room", "Dining Room"},
                {"Conservatory", "Ballroom", "Kitchen"}
        };

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                rooms[row][col] = new Room(names[row][col], row, col);
            }
        }
    }

    private void initializeHallways() {
        // Horizontal hallways
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE - 1; col++) {
                hallways.add(new Hallway(row, col, row, col + 1));
            }
        }
        // Vertical hallways
        for (int col = 0; col < SIZE; col++) {
            for (int row = 0; row < SIZE - 1; row++) {
                hallways.add(new Hallway(row, col, row + 1, col));
            }
        }
    }

    public boolean addPlayer(String playerId, String characterName, int row, int col) {
        if (rooms[row][col].isOccupied()) return false;
        PlayerState player = new PlayerState(playerId, characterName, row, col);
        playerPositions.put(playerId, player);
        rooms[row][col].setOccupied(true);
        return true;
    }

    public boolean movePlayer(String playerId, int targetRow, int targetCol) {
        PlayerState player = playerPositions.get(playerId);
        if (player == null) return false;

        Room currentRoom = rooms[player.getRow()][player.getCol()];
        Room targetRoom = rooms[targetRow][targetCol];

        Hallway attemptedPath = new Hallway(player.getRow(), player.getCol(), targetRow, targetCol);
        if (!hallways.contains(attemptedPath) || targetRoom.isOccupied()) {
            return false;
        }

        // Update positions
        currentRoom.setOccupied(false);
        targetRoom.setOccupied(true);
        player.setPosition(targetRow, targetCol);

        return true;
    }

    public Room getRoom(String playerId) {
        PlayerState player = playerPositions.get(playerId);
        return rooms[player.getRow()][player.getCol()];
    }

    public List<PlayerState> getAllPlayers() {
        return new ArrayList<>(playerPositions.values());
    }

    public void printBoardDebug() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Room room = rooms[r][c];
                String occupant = room.isOccupied() ? "X" : " ";
                System.out.print("[" + room.getName().charAt(0) + occupant + "]");
            }
            System.out.println();
        }
    }
}
