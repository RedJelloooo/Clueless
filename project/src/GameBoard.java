import java.util.*;
import java.awt.Point;

public class GameBoard {

    public static final int SIZE = 5;


    private final Room[][] rooms;
    private final Set<Hallway> hallways;
    private final Map<String, PlayerState> playerPositions; // key = player name/ID
    private final Set<Point> hallwayPositions = new HashSet<>(); // TODO
    private final Map<Point, String> hallwayOccupants = new HashMap<>(); //TODO

    public GameBoard() {
        this.rooms = new Room[SIZE][SIZE];
        this.hallways = new HashSet<>();
        this.playerPositions = new HashMap<>();
        initializeRooms();
        initializeHallways();
    }

    private void initializeRooms() {
        String[][] names = {
                {"Study", "", "Hall", "", "Lounge"},
                {"", "", "", "", ""},
                {"Library", "", "Billiard Room", "", "Dining Room"},
                {"", "", "", "", ""},
                {"Conservatory", "", "Ballroom", "", "Kitchen"}
        };

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String name = names[row][col];
                if (!name.equals("")) {
                    rooms[row][col] = new Room(name, row, col);
                }
            }
        }
    }

//
//    private void initializeHallways() {
//        hallwayPositions.clear();
//        hallwayOccupants.clear();
//        for (int row = 0; row < SIZE; row += 2) {
//            for (int col = 0; col < SIZE; col += 2) {
//                // Horizontal hallway: check right neighbor
//                if (col + 2 < SIZE && rooms[row][col] != null && rooms[row][col + 2] != null) {
//                    hallways.add(new Hallway(row, col, row, col + 2));
//                }
//
//                // Vertical hallway: check bottom neighbor
//                if (row + 2 < SIZE && rooms[row][col] != null && rooms[row + 2][col] != null) {
//                    hallways.add(new Hallway(row, col, row + 2, col));
//                }
//            }
//        }
//    }

    //TODO
    private void initializeHallways() {
        hallwayPositions.clear();
        hallwayOccupants.clear();

        for (int row = 0; row < SIZE; row += 2) {
            for (int col = 0; col < SIZE; col += 2) {
                // Horizontal hallway: check right neighbor
                if (col + 2 < SIZE && rooms[row][col] != null && rooms[row][col + 2] != null) {
                    hallways.add(new Hallway(row, col, row, col + 2));
                    // midpoint is horizontal hallway
                    hallwayPositions.add(new Point(row, col + 1));
                }

                // Vertical hallway: check bottom neighbor
                if (row + 2 < SIZE && rooms[row][col] != null && rooms[row + 2][col] != null) {
                    hallways.add(new Hallway(row, col, row + 2, col));
                    // midpoint is vertical hallway
                    hallwayPositions.add(new Point(row + 1, col));
                }
            }
        }
    }
    //TODO

    public boolean addPlayer(String playerId, String characterName, int row, int col) {
        if (rooms[row][col].isOccupied()) return false;
        PlayerState player = new PlayerState(playerId, characterName, row, col);
        playerPositions.put(playerId, player);
        rooms[row][col].setOccupied(true);
        return true;
    }

//    public boolean movePlayer(String playerId, int targetRow, int targetCol) {
//        PlayerState player = playerPositions.get(playerId);
//        if (player == null) return false;
//
//        Room currentRoom = rooms[player.getRow()][player.getCol()];
//        Room targetRoom = rooms[targetRow][targetCol];
//
//        Hallway attemptedPath = new Hallway(player.getRow(), player.getCol(), targetRow, targetCol);
//        if (!hallways.contains(attemptedPath) || targetRoom.isOccupied()) {
//            return false;
//        }
//
//        // Update positions
//        currentRoom.setOccupied(false);
//        targetRoom.setOccupied(true);
//        player.setPosition(targetRow, targetCol);
//
//        return true;
//    }

    //TODO
    public boolean movePlayer(String playerId, int targetRow, int targetCol) {
        PlayerState player = playerPositions.get(playerId);
        if (player == null) return false;

        int currentRow = player.getRow();
        int currentCol = player.getCol();

        Hallway attemptedPath = new Hallway(currentRow, currentCol, targetRow, targetCol);
        if (!hallways.contains(attemptedPath)) return false;

        Point targetPoint = new Point(targetRow, targetCol);

        // Handle hallway move
        if (hallwayPositions.contains(targetPoint)) {
            if (hallwayOccupants.containsKey(targetPoint)) return false; // hallway is occupied

            // Free up current spot
            Room currentRoom = rooms[currentRow][currentCol];
            if (currentRoom != null) currentRoom.setOccupied(false);

            hallwayOccupants.put(targetPoint, playerId);
            player.setPosition(targetRow, targetCol);
            return true;
        }

        // Handle room move
        Room targetRoom = rooms[targetRow][targetCol];
        if (targetRoom == null || targetRoom.isOccupied()) return false;

        Room currentRoom = rooms[currentRow][currentCol];
        if (currentRoom != null) currentRoom.setOccupied(false);

        hallwayOccupants.remove(new Point(currentRow, currentCol)); // If coming from hallway
        targetRoom.setOccupied(true);
        player.setPosition(targetRow, targetCol);
        return true;
    }

    //TODO

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




