import java.util.*;
import java.awt.Point;

public class GameBoard {

    public static final int SIZE = 5;


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
        String[][] names = {
                {"Study", "H", "Hall", "H", "Lounge"},
                {"H", "", "H", "", "H"},
                {"Library", "H", "Billiard Room", "H", "Dining Room"},
                {"H", "", "H", "", "H"},
                {"Conservatory", "H", "Ballroom", "H", "Kitchen"}
        };

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String name = names[row][col];
                if (!name.equals("")) {
                    if (name.equals("H")) {
                        rooms[row][col] = new Room("Hallway", row, col);
                    } else {
                        rooms[row][col] = new Room(name, row, col);
                    }
                }
            }
        }
    }


    private void initializeHallways() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (rooms[row][col] == null) continue;

                // Try to connect to all 4 cardinal neighbors (up, down, left, right)
                int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
                for (int[] dir : directions) {
                    int nRow = row + dir[0];
                    int nCol = col + dir[1];

                    if (nRow >= 0 && nRow < SIZE && nCol >= 0 && nCol < SIZE && rooms[nRow][nCol] != null) {
                        hallways.add(new Hallway(row, col, nRow, nCol));
                    }
                }
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

        int currentRow = player.getRow();
        int currentCol = player.getCol();

        Hallway attemptedPath = new Hallway(currentRow, currentCol, targetRow, targetCol);
        if (!hallways.contains(attemptedPath)) {
            System.out.println("Invalid hallway connection: " + attemptedPath);
            return false;
        }

        Room targetRoom = rooms[targetRow][targetCol];
        if (targetRoom == null || targetRoom.isOccupied()) return false;

        Room currentRoom = rooms[currentRow][currentCol];
        if (currentRoom != null) currentRoom.setOccupied(false);

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
                if (rooms[r][c] != null) {
                    String occupant = rooms[r][c].isOccupied() ? "X" : " ";
                    System.out.print("[" + rooms[r][c].getName().charAt(0) + occupant + "]");
                } else {
                    System.out.print("[  ]");
                }
            }
            System.out.println();
        }
    }
}




