import util.Commands;
import util.Score;
import util.TournamentScoreboard;
import util.WordFile;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import java.awt.BorderLayout;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Server class creates a server for players to connect to
 * the players are each a thread with their own score and round number
 * The server also calculates the score for the players and
 * generally ties everything together
 */
public class Server extends JFrame {

    private final GameBoard gameBoard = new GameBoard();
    private int playerCount = 0;
    private final static int MAX_PLAYERS = 16; //can possibly get rid of this
    private final JTextArea displayArea;
    private ServerSocket server;
    private final List<Player> players = new ArrayList<>();
    private final ExecutorService playerThreads;
    private final String[] scrambles;
    private final TournamentScoreboard tournamentScoreboard;
    private String leaderboard = "";
    private static final Map<String, int[]> startingPositions = Map.of(
            "MissScarlet", new int[]{4, 0},
            "ColonelMustard", new int[]{0, 2},
            "MrsWhite", new int[]{0, 4},
            "MrGreen", new int[]{4, 4},
            "MrsPeacock", new int[]{4, 2},
            "ProfessorPlum", new int[]{0, 0}
    );



    /**
     * creates a GUI interface for the server side.
     * also creates an array BlockingQueue to store the
     * various threads for the players.
     */
    public Server() {
        super("Server"); // title of the GUI
        playerThreads = Executors.newCachedThreadPool();
        displayArea = new JTextArea();
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        setSize(600, 300);
        setVisible(true);

        scrambles = WordFile.readLetterFile(new File("letters.txt")); // READS IN THE NEW LETTERS

        tournamentScoreboard = new TournamentScoreboard();
    }

    /**
     * Will run the server by creating a new socket and then attempt
     * to accept connections from clients
     */
    public void runServer() {
        try {
            server = new ServerSocket(23625, MAX_PLAYERS);

            try {
                displayMessage("Waiting for connections");
                getConnections();
            } catch (EOFException eofException) {
                displayMessage("\nServer terminated connection");
            } finally {
                closeServer();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * This method gets connection from clients that are attempting to join the server.
     * This also adds a new thread to the blocking queue for the new player.
     * @throws IOException - if adding another player fails
     */
    private void getConnections() throws IOException {
        while (true) {
            Socket connection = server.accept();
            displayMessage("\nConnection received from: " + connection.getInetAddress().getHostName());

            try {
                Player newPlayer = new Player(connection);
                players.add(newPlayer);
                playerThreads.execute(newPlayer);

            } catch (ClassNotFoundException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
    }

    /**
     * displays a message from the client
     * @param message - message to display
     */
    private void displayMessage(final String message) {
        SwingUtilities.invokeLater(() -> displayArea.append(message));
    }

    /**
     * closes the server
     * @throws IOException - if there is an error closing the server
     */
    private void closeServer() throws IOException {
        try {
            server.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * Class for players of our word game
     * Players are eaach a thread that is added to an ArrayBlockingQueue
     */
    private class Player implements Runnable {

        private final Socket connection; // connection to client
        private final ObjectInputStream input;
        private final ObjectOutputStream output;
        private String characterName;
        private boolean eliminated = false;

        /**
         * constructor for the player
         * @param socket - socket to connect ot the server
         */
        public Player(Socket socket) throws ClassNotFoundException {
            connection = socket;

            try {
                input = new ObjectInputStream(connection.getInputStream());
                output = new ObjectOutputStream(connection.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            try {
                displayMessage("\nPlayer connected\n");
                String clientCommand = "";

                // send scrambles to client
                output.writeObject(scrambles);
                output.flush();

                while (!clientCommand.equals("Quit")) {
                    try {
                        clientCommand = (String) input.readObject();
                        System.out.println("[" + characterName + "] Command received: " + clientCommand);
                        displayMessage("\n" + clientCommand);

                        // JOIN command
                        if (clientCommand.startsWith("JOIN")) {
                            this.characterName = clientCommand.split(" ")[1];

                            int[] start = startingPositions.get(characterName);
                            if (start == null) {
                                output.writeObject("FAILED JOIN: Unknown character");
                                output.flush();
                                continue;
                            }

                            boolean added = gameBoard.addPlayer(characterName, characterName, start[0], start[1]);

                            if (added) {
                                output.writeObject("JOINED " + characterName);
                            } else {
                                System.out.println("JOIN failed: position at (0,0) occupied or name taken");  // ← Add this
                                output.writeObject("FAILED JOIN");
                            }
                            output.flush();
                        }



                        // MOVE_DIRECTION command (up, down, left, right)
                        if (clientCommand.startsWith("MOVE_DIRECTION")) {
                            if (eliminated) {
                                output.writeObject("ERROR You are eliminated and cannot move.");
                                output.flush();
                                continue;
                            }

                            try {
                                String direction = clientCommand.split(" ")[1];

                                if (characterName == null) {
                                    output.writeObject("ERROR Player has not joined yet.");
                                    output.flush();
                                    continue;
                                }

                                System.out.println("Player ID: " + characterName);
                                PlayerState player = gameBoard.getPlayerState(characterName);
                                if (player == null) {
                                    System.out.println("Player not found!");
                                    output.writeObject("MOVED false (player not found)");
                                    output.flush();
                                    continue;
                                }

                                int row = player.getRow();
                                int col = player.getCol();
                                int newRow = row, newCol = col;
                                System.out.printf("Current position: (%d,%d)%n", row, col);

                                switch (direction) {
                                    case "UP" -> newRow--;
                                    case "DOWN" -> newRow++;
                                    case "LEFT" -> newCol--;
                                    case "RIGHT" -> newCol++;
                                }

                                System.out.printf("Attempting to move %s to (%d,%d)%n", direction, newRow, newCol);

                                boolean canMove = gameBoard.canMove(characterName, direction);
                                if (canMove) {
                                    boolean moved = gameBoard.movePlayer(characterName, newRow, newCol);
                                    output.writeObject("MOVED " + moved + " to (" + newRow + "," + newCol + ")");
                                } else {
                                    output.writeObject("MOVED false (Illegal move in direction: " + direction + ")");
                                }
                                output.flush();
                            } catch (Exception ex) {
                                System.err.println("Error in MOVE_DIRECTION block:");
                                ex.printStackTrace();
                                output.writeObject("MOVED false (Server error: " + ex.getMessage() + ")");
                                output.flush();
                            }
                        }

                        if (clientCommand.startsWith("SUGGEST")) {
                            if (eliminated) {
                                output.writeObject("ERROR You are eliminated and cannot make suggestions.");
                                output.flush();
                                continue;
                            }

                            try {
                                String[] parts = clientCommand.split(" ");
                                if (parts.length < 3) {
                                    output.writeObject("ERROR Invalid suggestion format.");
                                    output.flush();
                                    return;
                                }

                                String suspect = parts[1];
                                String weapon = parts[2];
                                System.out.println(characterName + " made a suggestion: " + suspect + " with the " + weapon);

                                Room currentRoom = gameBoard.getRoom(characterName);
                                if (currentRoom == null) {
                                    output.writeObject("ERROR Cannot suggest, room not found.");
                                    output.flush();
                                    return;
                                }

                                if (currentRoom.getName().equals("Hallway")) {
                                    System.out.print(currentRoom.getName());
                                    output.writeObject("ERROR Cannot make a suggestion from a hallway.");
                                    output.flush();
                                    continue; // <--- this keeps the socket open and loops to next command;
                                }


                                String roomName = currentRoom.getName();

                                System.out.println(characterName + " made a suggestion: " +
                                        suspect + " with the " + weapon + " in the " + currentRoom.getName());

                                // Move suspect (character) to current room
                                PlayerState suspectPlayer = gameBoard.getPlayerState(suspect);
                                if (suspectPlayer != null) {
                                    int oldRow = suspectPlayer.getRow();
                                    int oldCol = suspectPlayer.getCol();

                                    Room oldRoom = gameBoard.getRoom(suspect);
                                    if (oldRoom != null) oldRoom.setOccupied(false);

                                    suspectPlayer.setPosition(currentRoom.getRow(), currentRoom.getCol());
                                    currentRoom.setOccupied(true);
                                }

                                String msg = characterName + " suggests: " + suspect + " with the " + weapon + " in the " + currentRoom.getName();
                                output.writeObject(msg); // Send confirmation to suggester
                                output.flush();

                                // TODO: In future - notify other players to disprove
                                // Step 4: Check if other players can disprove the suggestion
                                List<String> suggestionCards = List.of(suspect, weapon, roomName);

                                boolean disproved = false;
                                for (Player p : players) {
                                    // skip the suggester
                                    if (p.characterName.equals(characterName)) continue;

                                    PlayerState otherState = gameBoard.getPlayerState(p.characterName);
                                    if (otherState == null) continue;

                                    for (String card : otherState.getCards()) {
                                        if (suggestionCards.contains(card)) {
                                            // Found someone who can disprove — send only to that player
                                            try {
                                                p.output.writeObject("You can disprove " + characterName + "'s suggestion. Reveal: " + card);
                                                p.output.flush();

                                                // Notify suggester
                                                output.writeObject("Your suggestion was disproved by " + p.characterName + " showing: " + card);
                                                output.flush();

                                                System.out.println(p.characterName + " disproved the suggestion with: " + card);
                                                disproved = true;
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            break;
                                        }
                                    }

                                    if (disproved) break; // Stop once someone disproves
                                }

                                if (!disproved) {
                                    output.writeObject("No one could disprove your suggestion.");
                                    output.flush();
                                    System.out.println("Suggestion could not be disproved.");
                                }

                            } catch (Exception ex) {
                                ex.printStackTrace();
                                output.writeObject("ERROR Could not process suggestion.");
                                output.flush();
                            }
                        }

                        if (clientCommand.startsWith("ACCUSE")) {
                            if (eliminated) {
                                output.writeObject("ERROR You are eliminated and cannot make accusations.");
                                output.flush();
                                continue;
                            }

                            String[] parts = clientCommand.split(" ");
                            if (parts.length < 4) {
                                output.writeObject("ERROR Invalid accusation format. Use: ACCUSE <Suspect> <Weapon> <Room>");
                                output.flush();
                                continue;
                            }

                            String accusedCharacter = parts[1];
                            String accusedWeapon = parts[2];
                            String accusedRoom = parts[3];

                            boolean correct = gameBoard.isCorrectAccusation(accusedCharacter, accusedWeapon, accusedRoom);

                            if (correct) {
                                output.writeObject("You WON! Your accusation was correct: " + accusedCharacter + " with the " + accusedWeapon + " in the " + accusedRoom + ".");
                                output.flush();

                                broadcast(characterName + " has made a CORRECT accusation and won the game!");
                                System.out.println(characterName + " WON the game!");
                                // You could optionally shut down the server or mark the game as over here
                            } else {
                                eliminated = true;
                                output.writeObject("Your accusation was incorrect. You are now out of the game.");
                                output.flush();

                                broadcast(characterName + " made an incorrect accusation and is eliminated from the game.");
                                System.out.println(characterName + " has been eliminated.");
                                // Optional: disable further actions from this player
                            }

                            continue; // skip to next command
                        }



                        // WHERE command
                        if (clientCommand.equals("WHERE")) {
                            if (eliminated) {
                                output.writeObject("ERROR You are eliminated and cannot check location.");
                                output.flush();
                                continue;
                            }

                            System.out.println("Where recevied");
                            Room room = gameBoard.getRoom(characterName);
                            if (room != null) {
                                output.writeObject("LOCATION " + room.getName() + " [" + room.getRow() + "," + room.getCol() + "]");
                            } else {
                                output.writeObject("LOCATION Unknown");
                            }
                            output.flush();
                        }

                        // other commands...
                        if (clientCommand.equals(Commands.PLAYER_JOINED.toString())) {
                            playerCount++;
                            displayMessage("\n" + playerCount + " players in the game.");
                        }

                        if (clientCommand.equals(Commands.PLAYER_LEFT.toString())) {
                            displayMessage("\n" + playerCount + " players in the game.");
                        }

                        if (!clientCommand.isEmpty() && clientCommand.charAt(0) == '?') {
                            calculateScore(clientCommand.replace("?", ""));
                        }

                        if (!clientCommand.isEmpty() && clientCommand.charAt(0) == '#') {
                            String[] scoreboard = clientCommand.replace("#", "").split(" ");
                            leaderboard = tournamentScoreboard.SortTextFile(scoreboard[0], Integer.parseInt(scoreboard[1]), Integer.parseInt(scoreboard[2]));
                        }

                        if (clientCommand.equals(Commands.GET_LEADERBOARD.toString())) {
                            output.writeObject("#" + leaderboard);
                            output.flush();
                        }

                        // Optional: debug board after every move
                        gameBoard.printBoardDebug();

                    } catch (Exception inner) {
                        System.err.println("Error while processing client command:");
                        inner.printStackTrace();
                        try {
                            output.writeObject("ERROR " + inner.getMessage());
                            output.flush();
                        } catch (IOException io) {
                            io.printStackTrace();
                        }
                    }
                }
            } catch (Exception outer) {
                System.err.println("Fatal error in client thread:");
                outer.printStackTrace();
            } finally {
                try {
                    playerCount--;
                    players.remove(this);  // Remove this player from the list
                    displayMessage("\nThere are currently " + playerCount + " players\n");
                    connection.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

        }


        /**
         * Calculates the users current score
         * using the score method from Util
         * @param phrase - phrase to be scored
         * @throws IOException - if writing the output fails
         */
        private void calculateScore(String phrase) throws IOException {
            String[] items = phrase.split(" ");
            char[] upperCharArray = items[0].toUpperCase().toCharArray();
            output.writeObject("!" + Score.calculate(items[1], upperCharArray));
            output.flush();
        }

    }

    private void broadcast(String message) {
        for (Player player : players) {
            try {
                player.output.writeObject(message);
                player.output.flush();
            } catch (IOException e) {
                System.err.println("Failed to send message to player: " + e.getMessage());
            }
        }
    }


}
