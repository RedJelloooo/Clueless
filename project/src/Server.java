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

        /**
         * Runnable for the player threads
         */
        @Override
        public void run() {
            try {
                displayMessage("\nPlayer connected\n");
                String clientCommand = "";

                // send scrambles to client
                output.writeObject(scrambles);
                output.flush();

                while (!clientCommand.equals("Quit")) {
                    clientCommand = (String) input.readObject();
                    displayMessage("\n" + clientCommand);

                    // for the server to track how many players we have
                    if (clientCommand.equals(Commands.PLAYER_JOINED.toString())) {
                        playerCount++;
                        displayMessage("\n" + playerCount + " players in the game.");
                    }

                    if (clientCommand.equals(Commands.PLAYER_LEFT.toString())) {
                        displayMessage("\n" + playerCount + " players in the game.");
                    }

                    // checks for word to calculate score of
                    if (!clientCommand.isEmpty() && clientCommand.charAt(0) == '?') {
                        calculateScore(clientCommand.replace("?",""));
                    }

                    // checks for score at the end of the round
                    if (!clientCommand.isEmpty() && clientCommand.charAt(0) == '#') {
                        String[] scoreboard = clientCommand.replace("#","").split(" ");
                        leaderboard = tournamentScoreboard.SortTextFile(scoreboard[0], Integer.parseInt(scoreboard[1]), Integer.parseInt(scoreboard[2]));
                    }

                    // checks for get leaderboard command and send the leaderboard to the client
                    if(clientCommand.equals(Commands.GET_LEADERBOARD.toString())) {
                        output.writeObject("#" + leaderboard);
                        output.flush();
                    }
                }
            }
            catch (IOException | ClassNotFoundException e) {
                Thread.currentThread().interrupt();
            } finally {
                try {
                    playerCount--;
                    displayMessage("\nThere are currently " + playerCount + " players\n");
                    connection.close(); // close connection to client
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    System.exit(1);
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
