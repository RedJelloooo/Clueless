import java.util.*;
import java.awt.Point;

public class GameBoard {

    public static final int SIZE = 5;


    private final Room[][] rooms;
    private final Set<Hallway> hallways;
    private final Map<String, PlayerState> playerPositions; // key = player name/ID
    private final String solutionCharacter;
    private final String solutionWeapon;
    private final String solutionRoom;

    public GameBoard() {
        this.rooms = new Room[SIZE][SIZE];
        this.hallways = new HashSet<>();
        this.playerPositions = new HashMap<>();
        initializeRooms();
        initializeHallways();

        // Generate random hidden solution
        this.solutionCharacter = pickRandom(new String[]{
                "MissScarlet", "ColonelMustard", "MrsWhite",
                "MrGreen", "MrsPeacock", "ProfessorPlum"
        });
        this.solutionWeapon = pickRandom(new String[]{
                "Candlestick", "Knife", "LeadPipe", "Revolver", "Rope", "Wrench"
        });
        this.solutionRoom = pickRandom(new String[]{
                "Study", "Hall", "Lounge", "Library", "Billiard Room", "Dining Room",
                "Conservatory", "Ballroom", "Kitchen"
        });
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
//        Room room = rooms[row][col];
        Room room = getRoom(row, col);
        if (room == null) return false;

        // Restrict hallways to one player
        if (room.getName().equals("Hallway") && room.isOccupied()) {
            System.out.println("Hallway at (" + row + "," + col + ") is already occupied.");
            return false;
        }

        PlayerState player = new PlayerState(playerId, characterName, row, col);
        playerPositions.put(playerId, player);
        room.addOccupant(playerId);

        return true;
    }



    //TODO for debugging
    public boolean movePlayer(String playerId, int targetRow, int targetCol) {
        PlayerState player = playerPositions.get(playerId);
        if (player == null) {
            System.out.println("No player found with ID: " + playerId);
            return false;
        }

        int currentRow = player.getRow();
        int currentCol = player.getCol();
        System.out.printf("Player at (%d,%d), attempting to move to (%d,%d)\n", currentRow, currentCol, targetRow, targetCol);

        Hallway attemptedPath = new Hallway(currentRow, currentCol, targetRow, targetCol);
        if (!hallways.contains(attemptedPath)) {
            System.out.println("Invalid hallway: " + attemptedPath);
            return false;
        }

//        Room currentRoom = rooms[currentRow][currentCol];
//        Room targetRoom = rooms[targetRow][targetCol];
        Room currentRoom = getRoom(currentRow, currentCol);
        Room targetRoom = getRoom(targetRow, targetCol);

        if (targetRoom == null) {
            System.out.println("Target room is null at: (" + targetRow + "," + targetCol + ")");
            return false;
        }

        // Restrict hallways to 1 occupant
        if (targetRoom.getName().equals("Hallway") && targetRoom.isOccupied()) {
            System.out.println("Target hallway is already occupied.");
            return false;
        }

        // Move player
        if (currentRoom != null) {
            currentRoom.removeOccupant(playerId);
        }
        targetRoom.addOccupant(playerId);
        player.setPosition(targetRow, targetCol);

        System.out.println("Player moved successfully to: (" + targetRow + "," + targetCol + ")");
        return true;
    }


    public Room getRoom(String playerId) {
        PlayerState player = playerPositions.get(playerId);
        return rooms[player.getRow()][player.getCol()];
    }
    public PlayerState getPlayerState(String playerId) {
        return playerPositions.get(playerId);
    }


    public List<PlayerState> getAllPlayers() {
        return new ArrayList<>(playerPositions.values());
    }

    public void printBoardDebug() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Room room = rooms[r][c];
                if (room != null) {
                    Set<String> occupants = room.getOccupants();

                    String tag;
                    if (occupants.isEmpty()) {
                        tag = "   ";
                    } else {
                        // Build a compact initials string (e.g. MS, PP)
                        StringBuilder sb = new StringBuilder();
                        for (String playerId : occupants) {
                            String[] parts = playerId.split("(?=[A-Z])");
                            for (String part : parts) {
                                if (!part.isEmpty()) sb.append(part.charAt(0));
                            }
                            sb.append(',');
                        }
                        sb.setLength(Math.min(3, sb.length())); // Truncate to max 3 chars
                        tag = sb.toString();
                    }

                    System.out.printf("[%s]", tag);
                } else {
                    System.out.print("[   ]");
                }
            }
            System.out.println();
        }
    }


    public boolean canMove(String playerId, String direction) {
        PlayerState player = playerPositions.get(playerId);
        if (player == null) return false;

        int row = player.getRow();
        int col = player.getCol();
        int newRow = row, newCol = col;

        switch (direction) {
            case "UP" -> newRow--;
            case "DOWN" -> newRow++;
            case "LEFT" -> newCol--;
            case "RIGHT" -> newCol++;
            default -> {
                System.out.println("Invalid direction: " + direction);
                return false;
            }
        }

        // Check bounds
        if (newRow < 0 || newRow >= SIZE || newCol < 0 || newCol >= SIZE) {
            System.out.printf("Move out of bounds: (%d,%d)%n", newRow, newCol);
            return false;
        }

        // Check if there's a room or hallway
//        if (rooms[newRow][newCol] == null)
        if (getRoom(newRow, newCol) == null) {
            System.out.printf("No room at (%d,%d)%n", newRow, newCol);
            return false;
        }

        // Check hallway connection
        Hallway attemptedPath = new Hallway(row, col, newRow, newCol);
        if (!hallways.contains(attemptedPath)) {
            System.out.printf("No valid hallway between (%d,%d) and (%d,%d)%n", row, col, newRow, newCol);
            return false;
        }

        // Check occupancy
//        if (rooms[newRow][newCol].isOccupied())
        if (getRoom(newRow, newCol).isOccupied()) {
            System.out.printf("Target square (%d,%d) is occupied%n", newRow, newCol);
            return false;
        }

        return true;
    }
    private String pickRandom(String[] array) {
        return array[new Random().nextInt(array.length)];
    }

    public boolean isCorrectAccusation(String character, String weapon, String room) {
        return solutionCharacter.equals(character) &&
                solutionWeapon.equals(weapon) &&
                solutionRoom.equals(room);
    }

    public Point getSecretPassageDestination(int row, int col) {
        if (rooms[row][col] == null) return null;
        String roomName = rooms[row][col].getName();

        return switch (roomName) {
            case "Study" -> new Point(4, 4); // Kitchen
            case "Kitchen" -> new Point(0, 0); // Study
            case "Conservatory" -> new Point(0, 4); // Lounge
            case "Lounge" -> new Point(4, 0); // Conservatory
            default -> null;
        };
    }

    public Room getRoom(int row, int col) {
        if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
            return rooms[row][col];
        }
        return null;
    }




}




