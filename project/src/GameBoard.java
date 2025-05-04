import java.util.*;
import java.awt.Point;

/**
 * The GameBoard class models the game environment for the Clue-Less game.
 * It manages the 5x5 grid of rooms and hallways, tracks player positions,
 * handles player movement, manages secret passages, and verifies suggestions and accusations.
 * <p>
 * It also randomly selects a hidden solution (character, weapon, and room) at game initialization.
 * <p>
 *  * Authors:
 *  *  - Albert Rojas
 */

public class GameBoard {

    public static final int SIZE = 5;


    private final Room[][] rooms;
    private final Set<Hallway> hallways;
    private final Map<String, PlayerState> playerPositions; // key = player name/ID
    private final String solutionCharacter;
    private final String solutionWeapon;
    private final String solutionRoom;

    /**
     * Constructs a GameBoard, initializes rooms, hallways, and picks a random solution.
     */
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


    /**
     * Initializes the 5x5 board with rooms and hallways according to the Clue-Less layout.
     */
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
                if (!name.isEmpty()) {
                    if (name.equals("H")) {
                        rooms[row][col] = new Room("Hallway", row, col);
                    } else {
                        rooms[row][col] = new Room(name, row, col);
                    }
                }
            }
        }
    }

    /**
     * Initializes the set of valid hallway connections between rooms.
     */
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

    /**
     * Adds a player to the game board at a specified starting location.
     *
     * @param playerId the unique ID or character name of the player
     * @param characterName the character assigned to the player
     * @param row the starting row position
     * @param col the starting column position
     * @return true if the player was successfully added, false otherwise
     */
    public boolean addPlayer(String playerId, String characterName, int row, int col) {
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

    /**
     * Attempts to move a player to a specified target location.
     * Validates that the move is legal according to hallway connections.
     *
     * @param playerId the player's ID
     * @param targetRow the row to move to
     * @param targetCol the column to move to
     * @return true if the move was successful, false if invalid
     */
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

    /**
     * Returns the Room object where the specified player currently is.
     *
     * @param playerId the player's ID
     * @return the current Room the player is located in
     */
    public Room getRoom(String playerId) {
        PlayerState player = playerPositions.get(playerId);
        return rooms[player.getRow()][player.getCol()];
    }

    /**
     * Retrieves the current state information for the specified player.
     *
     * @param playerId the player's ID
     * @return the PlayerState associated with the player
     */
    public PlayerState getPlayerState(String playerId) {
        return playerPositions.get(playerId);
    }

    /**
     * Returns a list of all player states currently on the board.
     *
     * @return a List of PlayerState objects
     */
    public List<PlayerState> getAllPlayers() {
        return new ArrayList<>(playerPositions.values());
    }

    /**
     * Prints a debug view of the board showing player locations.
     * Used mainly for server-side console output.
     */
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
                        StringBuilder sb = getStringBuilder(occupants);
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

    private static StringBuilder getStringBuilder(Set<String> occupants) {
        StringBuilder sb = new StringBuilder();
        for (String playerId : occupants) {
            String[] parts = playerId.split("(?=[A-Z])");
            for (String part : parts) {
                if (!part.isEmpty()) sb.append(part.charAt(0));
            }
            sb.append(',');
        }
        sb.setLength(Math.min(3, sb.length())); // Truncate to max 3 chars
        return sb;
    }

    /**
     * Determines if a player can legally move in a specified cardinal direction.
     *
     * @param playerId the player's ID
     * @param direction one of "UP", "DOWN", "LEFT", or "RIGHT"
     * @return true if the move is allowed, false otherwise
     */
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

        Room targetRoom = getRoom(newRow, newCol);
        if (targetRoom.getName().equals("Hallway") && targetRoom.isOccupied()) {
            System.out.printf("Target hallway (%d,%d) is occupied%n", newRow, newCol);
            return false;
        }


        return true;
    }

    /**
     * Randomly selects an item from an array.
     *
     * @param array an array of strings to choose from
     * @return a randomly selected string
     */
    private String pickRandom(String[] array) {
        return array[new Random().nextInt(array.length)];
    }


    /**
     * Verifies whether an accusation matches the game's hidden solution.
     *
     * @param character the accused character
     * @param weapon the accused weapon
     * @param room the accused room
     * @return true if the accusation is correct, false otherwise
     */
    public boolean isCorrectAccusation(String character, String weapon, String room) {
        return solutionCharacter.equals(character) &&
                solutionWeapon.equals(weapon) &&
                solutionRoom.equals(room);
    }


    /**
     * Retrieves the destination room for a secret passage if one exists.
     *
     * @param row the current row of the player
     * @param col the current column of the player
     * @return a Point indicating the destination row and column, or null if no passage
     */
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

    /**
     * Retrieves the Room object at a given grid position.
     *
     * @param row the row index
     * @param col the column index
     * @return the Room at the specified location, or null if invalid
     */
    public Room getRoom(int row, int col) {
        if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
            return rooms[row][col];
        }
        return null;
    }

    /**
     * Returns the solution character selected at the start of the game.
     *
     * @return the solution character's name
     */
    public String getSolutionCharacter() { return solutionCharacter; }

    /**
     * Returns the solution weapon selected at the start of the game.
     *
     * @return the solution weapon's name
     */
    public String getSolutionWeapon() { return solutionWeapon; }

    /**
     * Returns the solution room selected at the start of the game.
     *
     * @return the solution room's name
     */
    public String getSolutionRoom() { return solutionRoom; }


}




