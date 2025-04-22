import util.Commands;
import util.Score;
import util.TournamentScoreboard;
import util.WordFile;

import javax.swing.*;
import java.awt.BorderLayout;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final ExecutorService playerThreads;
    private final String[] scrambles;
    private final TournamentScoreboard tournamentScoreboard;
    private String leaderboard = "";

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
                playerThreads.execute(new Player(connection));
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
//                        if (clientCommand.startsWith("JOIN")) {
//                            String characterName = clientCommand.split(" ")[1];
//                            boolean added = gameBoard.addPlayer(characterName, characterName, 0, 0);
                        if (clientCommand.startsWith("JOIN")) {
                            this.characterName = clientCommand.split(" ")[1]; // Save it in the Player instance
                            boolean added = gameBoard.addPlayer(characterName, characterName, 0, 0);

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

                        // WHERE command
                        if (clientCommand.equals("WHERE")) {
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
                    displayMessage("\nThere are currently " + playerCount + " players\n");
                    connection.close(); // close connection to client
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
}
