import util.Commands;
import util.Score;
import util.TournamentScoreboard;
import util.WordFile;

import java.util.*;


import javax.swing.*;
import java.awt.BorderLayout;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.Point;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The Server class manages the Clue-Less game server.
 *
 * It accepts client connections, handles player actions (joining, moving, suggesting, accusing),
 * manages the game board state, deals cards, enforces turns, and broadcasts updates to all clients.
 *
 * Each connected player runs on a separate thread for simultaneous gameplay.
 */
public class Server extends JFrame {

    private final GameBoard gameBoard = new GameBoard();
    private int playerCount = 0;
    private boolean cardsDealt = false;
    private final static int MAX_PLAYERS = 16; //can possibly get rid of this
    private final JTextArea displayArea;
    private ServerSocket server;
    private final List<Player> players = new ArrayList<>();
    private final ExecutorService playerThreads;
    private final String[] scrambles;
    private final TournamentScoreboard tournamentScoreboard;
    private String leaderboard = "";
    private int currentTurnIndex = 0; // index into players list
    private boolean gameStarted = false;
    private String lastSuggester = null;
    private static final Map<String, int[]> startingPositions = Map.of(
            "MissScarlet", new int[]{4, 0},
            "ColonelMustard", new int[]{0, 2},
            "MrsWhite", new int[]{0, 4},
            "MrGreen", new int[]{4, 4},
            "MrsPeacock", new int[]{4, 2},
            "ProfessorPlum", new int[]{0, 0}
    );



    /**
     * Creates the Server GUI and initializes server resources,
     * including the game board, scramble data, and thread pool for players.
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
     * Starts the server, listening for incoming player connections on the designated port.
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
     * Continuously accepts new player connections and starts a thread for each new player.
     *
     * @throws IOException if an error occurs while accepting connections
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
     * Displays a message in the server GUI text area.
     *
     * @param message the message to display
     */
    private void displayMessage(final String message) {
        SwingUtilities.invokeLater(() -> displayArea.append(message));
    }

    /**
     * Closes the server socket, releasing network resources.
     *
     * @throws IOException if an error occurs while closing the server
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
                                broadcastPlayerPositions();

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
                            broadcastPlayerPositions();  // <-- NEW: update all clients with everyone's positions
                            if (!cardsDealt && players.size() >= 2) { // TODO or >= 3 or >= 6 if you want full table
                                dealCardsToPlayers();
                                cardsDealt = true;
                            }


                        }


                        // MOVE_DIRECTION command (up, down, left, right)
                        if (clientCommand.startsWith("MOVE_DIRECTION")) {
                            if (eliminated) {
                                output.writeObject("ERROR You are eliminated and cannot move.");
                                output.flush();
                                continue;
                            }

                            if (!characterName.equals(players.get(currentTurnIndex).characterName)) {
                                output.writeObject("ERROR Not your turn.");
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
                                    if (moved) {
                                        broadcastPlayerPositions();

                                        // NEW: Check if the player moved into a room
                                        Room newRoom = gameBoard.getRoom(newRow, newCol);
                                        if (newRoom != null && !newRoom.getName().equals("Hallway")) {
                                            output.writeObject("PROMPT_SUGGESTION");
                                            output.flush();
                                        }else {
                                            nextTurn();
                                        }
                                    }

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

                            if (!characterName.equals(players.get(currentTurnIndex).characterName)) {
                                output.writeObject("ERROR Not your turn.");
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

                                lastSuggester = characterName;

                                // Move suspect (character) to current room
                                PlayerState suspectPlayer = gameBoard.getPlayerState(suspect);
                                if (suspectPlayer != null) {
                                    int oldRow = suspectPlayer.getRow();
                                    int oldCol = suspectPlayer.getCol();

                                    Room oldRoom = gameBoard.getRoom(oldRow, oldCol);
                                    if (oldRoom != null) oldRoom.removeOccupant(suspect);

                                    suspectPlayer.setPosition(currentRoom.getRow(), currentRoom.getCol());
                                    currentRoom.addOccupant(suspect);

                                    broadcastPlayerPositions();

                                }

                                String msg = characterName + " suggests: " + suspect + " with the " + weapon + " in the " + currentRoom.getName();
                                output.writeObject(msg); // Send confirmation to suggester
                                output.flush();

                                broadcast(characterName + " suggests: " + suspect + " with the " + weapon + " in the " + roomName);

                                List<String> suggestionCards = List.of(suspect, weapon, roomName);

                                // Get the left player (the player before the suggester)
                                Player leftPlayer = players.get((currentTurnIndex - 1 + players.size()) % players.size());
                                PlayerState leftState = gameBoard.getPlayerState(leftPlayer.characterName);

                                if (leftState != null) {
                                    List<String> matches = new ArrayList<>();
                                    for (String card : leftState.getCards()) {
                                        if (suggestionCards.contains(card)) {
                                            matches.add(card);
                                        }
                                    }

                                    if (!matches.isEmpty()) {
                                        // Send dropdown menu to the left player
                                        leftPlayer.output.writeObject("DISPROVE_OPTIONS " + String.join(",", matches));
                                        leftPlayer.output.flush();
                                    } else {
                                        broadcast("SUGGESTION_NOT_DISPROVED_BY_PREVIOUS"); // NEW LINE
                                        broadcast(leftPlayer.characterName + " cannot disprove the suggestion.");
                                        output.writeObject("PROMPT_ACCUSATION_OR_END");
                                        output.flush();

                                    }


                                }


                            } catch (Exception ex) {
                                ex.printStackTrace();
                                output.writeObject("ERROR Could not process suggestion.");
                                output.flush();
                            }
                        }



                        if (clientCommand.equals("SECRET_PASSAGE")) {
                            PlayerState player = gameBoard.getPlayerState(characterName);
                            if (player == null) {
                                output.writeObject("ERROR Player not found.");
                                output.flush();
                                continue;
                            }

                            int currentRow = player.getRow();
                            int currentCol = player.getCol();
                            Room currentRoom = gameBoard.getRoom(characterName);

                            if (currentRoom == null || currentRoom.getName().equals("Hallway")) {
                                output.writeObject("ERROR Not in a room with a secret passage.");
                                output.flush();
                                continue;
                            }

                            Point destination = gameBoard.getSecretPassageDestination(currentRow, currentCol);
                            if (destination == null) {
                                output.writeObject("ERROR No secret passage from this room.");
                                output.flush();
                                continue;
                            }

                            Room targetRoom = gameBoard.getRoom(destination.x, destination.y);
                            if (targetRoom == null) {
                                output.writeObject("ERROR Destination room is invalid.");
                                output.flush();
                                continue;
                            }

                            currentRoom.removeOccupant(characterName);
                            targetRoom.addOccupant(characterName);
                            player.setPosition(destination.x, destination.y);

                            output.writeObject("MOVED true to (" + destination.x + "," + destination.y + ") via secret passage");
                            output.flush();
                            broadcastPlayerPositions();
                        }

                        if (clientCommand.equals("END_TURN")) {
                            nextTurn();
                        }

                        if (clientCommand.startsWith("DISPROVE_SELECTED")) {
                            String cardShown = clientCommand.split(" ", 2)[1];

                            broadcast(characterName + " disproved the suggestion by showing a card.");

                            Player suggester = findPlayerByName(lastSuggester);
                            if (suggester != null) {
                                suggester.output.writeObject(characterName + " showed you: " + cardShown);
                                suggester.output.flush();

                                // IMPORTANT: Give suggester the choice AFTER disproof
                                suggester.output.writeObject("PROMPT_ACCUSATION_OR_END");
                                suggester.output.flush();
                            }

                        }

                        if (clientCommand.startsWith("ACCUSE")) {
                            if (eliminated) {
                                output.writeObject("ERROR You are eliminated and cannot make accusations.");
                                output.flush();
                                continue;
                            }

                            if (!characterName.equals(players.get(currentTurnIndex).characterName)) {
                                output.writeObject("ERROR Not your turn.");
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
                                broadcast("GAME_OVER " + characterName);
                                System.out.println(characterName + " WON the game!");
                                // You could optionally shut down the server or mark the game as over here
                            } else {
                                eliminated = true;
                                output.writeObject("Your accusation was incorrect. You are now out of the game.");
                                output.flush();

                                broadcast(characterName + " made an incorrect accusation and is eliminated from the game.");
                                System.out.println(characterName + " has been eliminated.");
                                // Optional: disable further actions from this player

                                broadcastPlayerPositions(); // Optional: useful to reflect position if needed
                                checkForVictory();

                                nextTurn();


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

                            broadcastPlayerPositions();  // Re-send everyone’s positions
                            output.writeObject("LOCATION Sent all player positions.");
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

    /**
     * Broadcasts a text message to all connected players.
     *
     * @param message the message to send to every client
     */
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

    /**
     * Sends all players the latest player positions on the board.
     */
    private void broadcastPlayerPositions() {
        List<PlayerState> allPlayers = gameBoard.getAllPlayers();
        for (Player p : players) {
            try {
                StringBuilder sb = new StringBuilder("ALL_POSITIONS");
                for (PlayerState ps : allPlayers) {
                    sb.append(" ").append(ps.getCharacterName())
                            .append(",").append(ps.getRow()).append(",").append(ps.getCol());
                }
                p.output.writeObject(sb.toString());
                p.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Deals Clue-Less cards (characters, weapons, rooms) randomly to all players,
     * excluding the solution cards.
     */
    private void dealCardsToPlayers() {
        List<String> deck = new ArrayList<>();

        // Add all possible cards
        deck.addAll(Arrays.asList(
                "MissScarlet", "ColonelMustard", "MrsWhite",
                "MrGreen", "MrsPeacock", "ProfessorPlum"
        ));
        deck.addAll(Arrays.asList(
                "Candlestick", "Knife", "LeadPipe", "Revolver", "Rope", "Wrench"
        ));
        deck.addAll(Arrays.asList(
                "Study", "Hall", "Lounge", "Library", "Billiard Room", "Dining Room",
                "Conservatory", "Ballroom", "Kitchen"
        ));

        // Remove the solution cards
        deck.remove(gameBoard.getSolutionCharacter());
        deck.remove(gameBoard.getSolutionWeapon());
        deck.remove(gameBoard.getSolutionRoom());

        // Shuffle the deck
        Collections.shuffle(deck);

        // Deal cards round-robin
        int playerIndex = 0;
        for (String card : deck) {
            Player player = players.get(playerIndex);
            PlayerState playerState = gameBoard.getPlayerState(player.characterName);
            if (playerState != null) {
                playerState.addCard(card);
            }
            playerIndex = (playerIndex + 1) % players.size();
        }

        // OPTIONAL: notify each player of their cards
        for (Player p : players) {
            try {
                PlayerState ps = gameBoard.getPlayerState(p.characterName);
                if (ps != null) {
                    p.output.writeObject("YOUR_CARDS " + ps.getCards());
                    p.output.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        gameStarted = true;
        notifyCurrentTurnPlayer();

    }

    /**
     * Notifies the current player that it is their turn.
     */
    private void notifyCurrentTurnPlayer() {
        if (players.isEmpty()) return;
        Player currentPlayer = players.get(currentTurnIndex);
        try {
            currentPlayer.output.writeObject("YOUR_TURN");
            currentPlayer.output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Advances the turn to the next eligible (non-eliminated) player.
     */
    private void nextTurn() {
        //It keeps incrementing currentTurnIndex until it finds a player who is NOT eliminated.
        //It stops if it loops all the way around (to avoid infinite loops if everyone is eliminated).
        if (players.isEmpty()) return;

        int startingIndex = currentTurnIndex;
        do {
            currentTurnIndex = (currentTurnIndex + 1) % players.size();
        } while (players.get(currentTurnIndex).eliminated && currentTurnIndex != startingIndex);

        notifyCurrentTurnPlayer();
    }


    /**
     * Checks if a victory condition has been met (only one player left).
     * If so, announces the winner and ends the game.
     */
    private void checkForVictory() {

        if (players.isEmpty()) return;

        List<Player> activePlayers = new ArrayList<>();
        for (Player p : players) {
            if (!p.eliminated) {
                activePlayers.add(p);
            }
        }

        if (activePlayers.size() == 1) {
            Player winner = activePlayers.get(0);
            try {
                winner.output.writeObject("You WON! Everyone else has been eliminated.");
                winner.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            broadcast(winner.characterName + " has WON the game because all other players were eliminated!");
            broadcast("GAME_OVER " + winner.characterName);
            System.out.println(winner.characterName + " has WON by default!");

        }
    }


    /**
     * Finds and returns a Player object by their character name.
     *
     * @param name the character name
     * @return the Player object, or null if not found
     */
    private Player findPlayerByName(String name) {
        for (Player p : players) {
            if (p.characterName.equals(name)) {
                return p;
            }
        }
        return null;
    }







}
