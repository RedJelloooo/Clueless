import ui.Leaderboard;
import util.Commands;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.Point;
import java.util.List;



/**
 * The Client class represents the player's interface for the Clue-Less game.
 * It handles the GUI setup, networking with the server, gameplay actions
 * (such as moving, suggesting, and accusing), and interaction with detective notes.
 *
 * Players use this client to connect to a server, select characters,
 * make moves and suggestions, view their cards, and keep track of other players' actions.
 *
 * Authors:
 *  - Brandon Cano (Server and Client Logic)
 *  - Alex Arand (GUI Implementation)
 *  - Albert Rojas (GUI Implementation)
 */

public class Client extends JFrame {
    // networking parts
    private String[] scrambles = new String[5];
    private boolean haveScramble = false;
    private int clientScore = 0;
    private int clientRound = 1;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Socket client;
    private final String chatServer;
    private final Set<String> wordsGuessed = new HashSet<>();
    private String message = "";
    private JComboBox<String> characterDropdown;
    private final Set<Point> secretPassageRooms = Set.of(
            new Point(0, 0), // Study
            new Point(0, 4), // Lounge
            new Point(4, 0), // Conservatory
            new Point(4, 4)  // Kitchen
    );
    private int currentPlayerRow = -1;
    private int currentPlayerCol = -1;
    private String myCards = "";
    private final java.util.List<String> detectiveNotes = new ArrayList<>();
    private final Map<String, Map<String, Boolean>> detectiveTable = new HashMap<>();




    // GUI components
    private JPanel boardPanel;
    private JLabel[][] roomLabels;

    private static final int BOARD_SIZE = 5;
    private JLabel[][] boardLabels;

    private final JButton backToMainMenuFromSkinsButton;
    private final JButton exitFromGameToMainMenuButton;
    private final JButton continueToNextRoundButton;
    private final JButton exitTheApplicationButton;
    private final JButton joinTheTournamentButton;
    private final JButton displayLeaderboard;
    private final JButton skinsButton;
    private final JButton chooseName;
    private final JButton displayRules;
    private JScrollPane scrollPane;
    private JButton secretPassageButton;
    private final JButton makeSuggestionButton = new JButton("Make Suggestion");
    private final JButton makeAccusationButton = new JButton("Make Accusation");
    private JButton myCardsButton;
    private JButton detectiveNotePad;


    private final JLabel scrambleForCurrentRoundLabel;
    private final JLabel gameBackgroundLabel;
    private final JLabel timeRemainingLabel;
    private final JLabel currentRoundLabel;
    private final JLabel clientScoreLabel;
    private final JLabel gameTimerLabel;
    private final JLabel currentName;
    private final JLabel display;
    private JLabel gameLogo;
    private JLabel menu;
    private JLabel rules;



    private final JTextField enterName;

    private final JComboBox<String> imagesJComboBox;
    private final JTextField textField;

    private String name = "Player";


    private static final String[] characters = {
            "MissScarlet", "ColonelMustard", "MrsWhite",
            "MrGreen", "MrsPeacock", "ProfessorPlum"
    };

    /**
     * file names for the images
     */
    private static final String[] names = {
            "Clue.png",
            "amogusbackground.gif",
            "hansgiffinal.gif",
            "christmas.png", //https://newevolutiondesigns.com/images/freebies/4k-christmas-wallpaper-2.jpg
            "kermit.png", // https://www.wallpaperflare.com/bliss-windows-xp-kermit-the-frog-microsoft-windows-the-muppet-show-1920x1440-animals-frogs-hd-art-wallpaper-smhoy
            "ECEPROFS.png"
    };

    private static final String[] gameBackgrounds = {"roundone.png"};
    private final Icon[] gBackImages = new Icon[1];
    // timer pieces
    private Timer waitTimer;
    private Timer timer;

    private static final int timePerRound = 60;
    private int secondsInGame = 60;
    private int seconds;

    private Clip clip;

    /**
     * Creates the client GUI, initializes the network settings,
     * and sets up the game components such as buttons, panels, and timers.
     *
     * @param host the IP address of the server to connect to
     */
    public Client(String host) {
        // set name of window
        super("Client");
        // set which server the client connects to
        chatServer = host;

        // GUI setup
        setLayout(null);


        backToMainMenuFromSkinsButton = new JButton();
        exitFromGameToMainMenuButton = new JButton();
        continueToNextRoundButton = new JButton();
        exitTheApplicationButton = new JButton();
        joinTheTournamentButton = new JButton();
        displayLeaderboard = new JButton();
        myCardsButton = new JButton();
        detectiveNotePad = new JButton();
        skinsButton = new JButton();
        chooseName = new JButton();
        displayRules = new JButton();
        detectiveNotePad.setToolTipText("View all clues you've collected from players!");


        timeRemainingLabel = new JLabel(seconds + " seconds remaining!");
        currentRoundLabel = new JLabel("Round " + clientRound);
        gameTimerLabel = new JLabel(String.valueOf(secondsInGame));
        gameBackgroundLabel = new JLabel(gameBackgrounds[0]);
        scrambleForCurrentRoundLabel = new JLabel();
        clientScoreLabel = new JLabel();
        currentName = new JLabel();

        enterName = new JTextField("Enter your name here");
        textField = new JTextField();

        textField.setEditable(true);

        JButton upButton = new JButton("Up");
        upButton.setBounds(25, 400, 100, 25);
        add(upButton);

        JButton downButton = new JButton("Down");
        downButton.setBounds(25, 430, 100, 25);
        add(downButton);

        JButton leftButton = new JButton("Left");
        leftButton.setBounds(25, 460, 100, 25);
        add(leftButton);

        JButton rightButton = new JButton("Right");
        rightButton.setBounds(25, 490, 100, 25);
        add(rightButton);

//        JButton secretPassageButton = new JButton("Secret Passage");
        secretPassageButton = new JButton("Secret Passage");
        secretPassageButton.setBounds(25, 520, 100, 25);  // adjust position if needed
        add(secretPassageButton);

        secretPassageButton.addActionListener(e -> {
            System.out.println("Sending command: SECRET_PASSAGE");
            sendData("SECRET_PASSAGE");
        });



        System.out.println("Sending move command: MOVE_DIRECTION UP");


        // Add Action Listeners
        upButton.addActionListener(e -> {
            System.out.println("Sending move command: MOVE_DIRECTION UP");
            sendData("MOVE_DIRECTION UP");
        });
        downButton.addActionListener(e -> {
            System.out.println("Sending move command: MOVE_DIRECTION DOWN");
            sendData("MOVE_DIRECTION DOWN");
        });
        leftButton.addActionListener(e -> {
            System.out.println("Sending move command: MOVE_DIRECTION LEFT");
            sendData("MOVE_DIRECTION LEFT");
        });
        rightButton.addActionListener(e -> {
            System.out.println("Sending move command: MOVE_DIRECTION RIGHT");
            sendData("MOVE_DIRECTION RIGHT");
        });


        boardLabels = new JLabel[BOARD_SIZE][BOARD_SIZE];
        JPanel boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        boardPanel.setBounds(150, 55, 400, 400); // adjust size as needed

        String[][] roomGridNames = {
                {"Study", "H", "Hall", "H", "Lounge"},
                {"H", "", "H", "", "H"},
                {"Library", "H", "Billiard", "H", "Dining"},
                {"H", "", "H", "", "H"},
                {"Conservatory", "H", "Ballroom", "H", "Kitchen"}
        };

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                String name = roomGridNames[row][col];
                JLabel label = new JLabel(); // safe default


                label.setOpaque(true);

                if (name.equals("H")) {
                    label = new JLabel(); // Hallway
                    label.setOpaque(true);
                    label.setBackground(Color.LIGHT_GRAY);
                    label.setToolTipText("Hallway");
                } else if (name.equals("")) {
                    label = new JLabel(); // Invalid/unused
                    label.setOpaque(true);
                    label.setBackground(Color.BLACK);
                } else {
                    label = new JLabel(name, SwingConstants.CENTER); // Room
                    label.setOpaque(true);
                    label.setBackground(Color.CYAN);
                    label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    label.setToolTipText(name);
                }


                boardLabels[row][col] = label;
                boardPanel.add(label);
            }
        }

        add(boardPanel);
        boardPanel.setVisible(false);


        URL rulesURL = getClass().getResource("rules.png");
        if (rulesURL != null) {
            ImageIcon rule = new ImageIcon(rulesURL);
            rules = new JLabel(rule);
            scrollPane = new JScrollPane(rules);
            scrollPane.setBounds(0, 0, 800, 600);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVisible(false); // hide it by default

        }


        // setup for game logo
        URL gameLogoURL = getClass().getResource("gamelogo in game context.png");
        if (gameLogoURL != null) {
            ImageIcon logo = new ImageIcon(gameLogoURL);
            gameLogo = new JLabel(logo);
        }

        // setup for JComboBox
        Icon[] icons = new Icon[names.length];
        for (int i = 0; i < names.length; i++) {
            URL image = getClass().getResource(names[i]);

            if (image != null) {
                icons[i] = new ImageIcon(image);
            }
        }

        URL image = getClass().getResource(gameBackgrounds[0]);
        if (image != null) {
            gBackImages[0] = new ImageIcon(image);
        }

        imagesJComboBox = new JComboBox<>(names);
        imagesJComboBox.setMaximumRowCount(4); // display four rows
        display = new JLabel(icons[0]);

        characterDropdown = new JComboBox<>(characters);
        characterDropdown.setBounds(600, 240, 150, 25);
        characterDropdown.setVisible(true);
        add(characterDropdown);

        JButton joinGameButton = new JButton("Join Game");
        joinGameButton.setBounds(600, 275, 150, 25);
        add(joinGameButton);


        joinGameButton.addActionListener(e -> {
            String selected = (String) characterDropdown.getSelectedItem();
            name = selected;
            sendData("JOIN " + selected);
            myCardsButton.setVisible(true);
            detectiveNotePad.setVisible(true);
           boardPanel.setVisible(true); //
        });

        makeSuggestionButton.addActionListener(e -> {
            String[] suspects = {
                    "MissScarlet", "ColonelMustard", "MrsWhite",
                    "MrGreen", "MrsPeacock", "ProfessorPlum"
            };

            String[] weapons = {
                    "Candlestick", "Knife", "LeadPipe", "Revolver", "Rope", "Wrench"
            };

            JComboBox<String> suspectDropdown = new JComboBox<>(suspects);
            JComboBox<String> weaponDropdown = new JComboBox<>(weapons);
            JButton notesButton = new JButton("Detective Notepad");

            // Action for the Detective Notes button
            notesButton.addActionListener(event -> detectiveNotePad.doClick());

            JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5)); // 3 rows, 2 columns, nice spacing
            panel.add(new JLabel("Suspect:"));
            panel.add(suspectDropdown);
            panel.add(new JLabel("Weapon:"));
            panel.add(weaponDropdown);
            panel.add(new JLabel("")); // Empty cell for alignment
            panel.add(notesButton);

            int result = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    "Make a Suggestion",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String suspect = (String) suspectDropdown.getSelectedItem();
                String weapon = (String) weaponDropdown.getSelectedItem();
                sendData("SUGGEST " + suspect + " " + weapon);
            }
        });


        makeAccusationButton.addActionListener(e -> {
            String[] suspects = {
                    "MissScarlet", "ColonelMustard", "MrsWhite",
                    "MrGreen", "MrsPeacock", "ProfessorPlum"
            };

            String[] weapons = {
                    "Candlestick", "Knife", "LeadPipe", "Revolver", "Rope", "Wrench"
            };

            String[] rooms = {
                    "Study", "Hall", "Lounge", "Library", "Billiard Room", "Dining Room",
                    "Conservatory", "Ballroom", "Kitchen"
            };

            JComboBox<String> suspectDropdown = new JComboBox<>(suspects);
            JComboBox<String> weaponDropdown = new JComboBox<>(weapons);
            JComboBox<String> roomDropdown = new JComboBox<>(rooms);

            JPanel panel = new JPanel(new GridLayout(3, 2));
            panel.add(new JLabel("Suspect:"));
            panel.add(suspectDropdown);
            panel.add(new JLabel("Weapon:"));
            panel.add(weaponDropdown);
            panel.add(new JLabel("Room:"));
            panel.add(roomDropdown);

            int result = JOptionPane.showConfirmDialog(this, panel, "Make an Accusation", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String suspect = (String) suspectDropdown.getSelectedItem();
                String weapon = (String) weaponDropdown.getSelectedItem();
                String room = (String) roomDropdown.getSelectedItem();

                sendData("ACCUSE " + suspect + " " + weapon + " " + room);
            }
        });

        myCardsButton.addActionListener(e -> {
            if (myCards.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You have no cards yet!", "My Cards", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Your cards are:\n" + myCards, "My Cards", JOptionPane.INFORMATION_MESSAGE);
            }
        });


        detectiveNotePad.addActionListener(e -> {
            Set<String> playersSet = new HashSet<>();
            for (Map<String, Boolean> map : detectiveTable.values()) {
                playersSet.addAll(map.keySet());
            }
            playersSet.add("Me"); // always include Me
            List<String> playersList = new ArrayList<>(playersSet);
            Collections.sort(playersList);

            String[] columnNames = new String[playersList.size() + 1];
            columnNames[0] = "Card";
            for (int i = 0; i < playersList.size(); i++) {
                columnNames[i + 1] = playersList.get(i);
            }

            List<String> allCards = Arrays.asList(
                    "MissScarlet", "ColonelMustard", "MrsWhite",
                    "MrGreen", "MrsPeacock", "ProfessorPlum",
                    "Candlestick", "Knife", "LeadPipe", "Revolver", "Rope", "Wrench",
                    "Study", "Hall", "Lounge", "Library", "Billiard Room", "Dining Room",
                    "Conservatory", "Ballroom", "Kitchen"
            );

            Object[][] data = new Object[allCards.size()][playersList.size() + 1];
            for (int row = 0; row < allCards.size(); row++) {
                String card = allCards.get(row);
                data[row][0] = card;
                for (int col = 0; col < playersList.size(); col++) {
                    String player = playersList.get(col);
                    boolean marked = detectiveTable.containsKey(card) &&
                            detectiveTable.get(card).getOrDefault(player, false);
                    data[row][col + 1] = marked ? "✔" : ""; // Default to checkmark if true, blank otherwise
                }
            }

            DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column != 0; // Only players' columns are editable
                }
            };

            JTable table = new JTable(model);
            table.setRowHeight(25);
            table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
            table.setFont(new Font("SansSerif", Font.PLAIN, 12));

            // 🔵 NEW: Add mouse click listener to cycle through ✔ -> ✖ -> ◯ -> ""
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int row = table.rowAtPoint(evt.getPoint());
                    int col = table.columnAtPoint(evt.getPoint());
                    if (col > 0) { // prevent editing card names
                        String current = (String) model.getValueAt(row, col);
                        String next;
                        if (current == null || current.isEmpty()) {
                            next = "✔";
                        } else if (current.equals("✔")) {
                            next = "✖";
                        } else if (current.equals("✖")) {
                            next = "◯";
                        } else {
                            next = ""; // go back to blank
                        }
                        model.setValueAt(next, row, col);
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);

            JButton saveButton = new JButton("Save and Close");
            saveButton.addActionListener(ev -> {
                for (int row = 0; row < model.getRowCount(); row++) {
                    String card = (String) model.getValueAt(row, 0);
                    for (int col = 1; col < model.getColumnCount(); col++) {
                        String player = columnNames[col];
                        String mark = (String) model.getValueAt(row, col);

                        detectiveTable.putIfAbsent(card, new HashMap<>());
                        detectiveTable.get(card).put(player, "✔".equals(mark)); // Only ✔ counts as true internally
                    }
                }
                JOptionPane.showMessageDialog(Client.this, "Detective Notes Saved!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            });

            JDialog dialog = new JDialog(Client.this, "Detective Notepad", true);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(saveButton, BorderLayout.SOUTH);

            dialog.getContentPane().add(panel);
            dialog.setSize(650, 450);
            dialog.setLocationRelativeTo(Client.this);
            dialog.setVisible(true);
        });




        URL menuImage = getClass().getResource("menu.png");
        if (menuImage != null) {
            menu = new JLabel(new ImageIcon(menuImage));
        }

        initiateText();
        initiateBounds();
        addJFrameComponents();
        mainMenuInitialize();

        skinsButton.setVisible(true);

        imagesJComboBox.addItemListener(event -> {
            // determine which item selected
            if (event.getStateChange() == ItemEvent.SELECTED)
                display.setIcon(icons[imagesJComboBox.getSelectedIndex()]);
        });

        textField.addActionListener(event -> {
            String guess = event.getActionCommand();
            guess = guess.replace("!", "").replace("?", "").replace("#", "");// replace code characters
            if (!wordsGuessed.contains(guess) && !guess.isEmpty()) {
                wordsGuessed.add(guess);
                sendData("?" + scrambles[clientRound - 1] + " " + guess);
            }
            textField.setText("");
        });

        enterName.addActionListener(event -> {
            name = event.getActionCommand();
            name = name.replace("!", "").replace("?", "").replace("#", "");
            currentName.setText("Name: " + name.replace(" ", ""));
        });

        exitTheApplicationButton.addActionListener(e -> {
            sendData(Commands.PLAYER_LEFT.toString());
            System.exit(0);
        });

        chooseName.addActionListener(e -> {
            backToMainMenuFromSkinsButton.setVisible(true);
            exitTheApplicationButton.setVisible(false);
            joinTheTournamentButton.setVisible(false);
            displayLeaderboard.setVisible(false);
            skinsButton.setVisible(false);
            currentName.setVisible(true);
            chooseName.setVisible(false);
            enterName.setVisible(true);
            display.setVisible(true);
            displayRules.setVisible(false);
        });

        skinsButton.addActionListener(e -> {
            backToMainMenuFromSkinsButton.setVisible(true);
            exitTheApplicationButton.setVisible(false);
            joinTheTournamentButton.setVisible(false);
            displayLeaderboard.setVisible(false);
            skinsButton.setVisible(false);
            chooseName.setVisible(false);
            displayRules.setVisible(false);
            showBackgroundOptions();
        });

        backToMainMenuFromSkinsButton.addActionListener(e -> {
            backToMainMenuFromSkinsButton.setBounds(600, 400, 150, 50);
            backToMainMenuFromSkinsButton.setVisible(false);
            exitTheApplicationButton.setVisible(true);
            joinTheTournamentButton.setVisible(false);//TODO set false cant delete will break the game
            displayLeaderboard.setVisible(true);
            myCardsButton.setVisible(true);
            detectiveNotePad.setVisible(true);
            imagesJComboBox.setVisible(false);
            skinsButton.setVisible(true);
            enterName.setVisible(false);
            chooseName.setVisible(true);
            display.setVisible(true);
            displayRules.setVisible(true);
            if (scrollPane != null) scrollPane.setVisible(false);
            mainMenuInitialize();
        });

        displayRules.addActionListener(e ->{
            hideAllScreens();
            menu.setVisible(false);
            backToMainMenuFromSkinsButton.setVisible(true);
            exitTheApplicationButton.setVisible(false);
            joinTheTournamentButton.setVisible(false);
            displayLeaderboard.setVisible(false);
            skinsButton.setVisible(false);
            chooseName.setVisible(false);
            displayRules.setVisible(false);
            rules.setVisible(true);
            myCardsButton.setVisible(true);
            detectiveNotePad.setVisible(true);

            if (rules != null && rules.getIcon() instanceof ImageIcon imageIcon) {
                int imageWidth = imageIcon.getIconWidth();
                int imageHeight = imageIcon.getIconHeight();

                int extraWidth = 10;
                int extraHeight = 20;

                setSize(820 + extraWidth, 640 + extraHeight); //820, 640
                setLocationRelativeTo(null); // Center the window
            }


            scrollPane.setVisible(true);
            backToMainMenuFromSkinsButton.setBounds(600, 450, 150, 50);

        });

        joinTheTournamentButton.addActionListener(e -> {
            if (clientRound >= 5) {
                return;
            }

            exitFromGameToMainMenuButton.setBounds(620, 500, 150, 50);

            audioEnabler();
            myCardsButton.setVisible(true);
            detectiveNotePad.setVisible(true);
            exitFromGameToMainMenuButton.setVisible(true);
            exitTheApplicationButton.setVisible(false);
            joinTheTournamentButton.setVisible(false);
            displayLeaderboard.setVisible(false);
            timeRemainingLabel.setVisible(true);
            timeRemainingLabel.setVisible(true);
            skinsButton.setVisible(false);
            chooseName.setVisible(false);
            display.setVisible(true);
            menu.setVisible(false);
            displayRules.setVisible(false);

            seconds = 5; // Resets seconds after initialization
            timeRemainingLabel.setText(seconds + " seconds remaining!");
            waitTimer = new Timer(1000, t -> {
                seconds--;
                timeRemainingLabel.setText(seconds + " seconds remaining!");
                if (seconds == 0) {
                    waitTimer.stop();
                    startRound();
                }
            });
            waitTimer.start();
        });

        exitFromGameToMainMenuButton.addActionListener(e -> {
            if (timer != null && timer.isRunning()) {
                timer.stop();
            }
            if (waitTimer != null && waitTimer.isRunning()) {
                waitTimer.stop();
            }

            clip.stop();
            mainMenuInitialize();
        });

        continueToNextRoundButton.addActionListener(e -> {
            exitFromGameToMainMenuButton.setBounds(620, 500, 150, 50);
            continueToNextRoundButton.setVisible(false);
            continueToNextRoundButton.setVisible(false);
            displayLeaderboard.setVisible(false);

            clientRound += 1;
            currentRoundLabel.setText("Round " + clientRound);
            wordsGuessed.clear();
            startRound();
        });

        displayLeaderboard.addActionListener(e -> sendData(Commands.GET_LEADERBOARD.toString()));
    }

    /**
     * Establishes a socket connection to the server and sets up input/output streams.
     * Then listens for and processes messages from the server.
     */
    public void runClient() {
        try {
            client = new Socket(InetAddress.getByName(chatServer), 23625); // port might need to be changed

            outputStream = new ObjectOutputStream(client.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(client.getInputStream());

            processConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
    }

    /**
     * Processes incoming messages from the server and updates the client GUI and state accordingly.
     *
     * @throws IOException if an I/O error occurs when reading server messages
     */
    private void processConnection() throws IOException {
        sendData(Commands.PLAYER_JOINED.toString());

        do { // process message
            try {
                if (haveScramble) {
                    message = (String) inputStream.readObject();
                }else {
                    scrambles = (String[]) inputStream.readObject();
                    haveScramble = true;
                    continue; // skip rest of loop, no message to process
                }

                if (!message.isEmpty() && message.charAt(0) == '!') {
                    clientScore += Integer.parseInt(message.replace("!", ""));
                    clientScoreLabel.setText("Current Score: " + clientScore);
                }

                if (!message.isEmpty() && message.charAt(0) == '#') {
                    String leaderboard = message.replace("#", "");
                    Leaderboard application = new Leaderboard(leaderboard);
                    application.setSize(400, 600);
                    application.setLocationRelativeTo(null);
                    application.setTitle("Leaderboard");
                    application.setVisible(true);
                }

                if (message.contains("suggests:")) {
                    JOptionPane.showMessageDialog(this,
                            message,
                            "New Suggestion Made",
                            JOptionPane.INFORMATION_MESSAGE);
                }


                if (message.startsWith("You WON!")) {
                    JOptionPane.showMessageDialog(this, message, "🎉 You Won the Game!", JOptionPane.INFORMATION_MESSAGE);

                    // Disable buttons because game is over
                    makeSuggestionButton.setEnabled(false);
                    makeAccusationButton.setEnabled(false);
                    secretPassageButton.setEnabled(false);


                    int response = JOptionPane.showConfirmDialog(
                            this,
                            "Would you like to play again?",
                            "Play Again?",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (response == JOptionPane.YES_OPTION) {
                        this.dispose(); // Close the current window
                        Client newClient = new Client(chatServer); // Create a fresh client
                        newClient.setTitle("Client - New Game");
                        newClient.setSize(800, 600);
                        newClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        newClient.setVisible(true);
                        newClient.setResizable(false);
                        newClient.setLocationRelativeTo(null);
                        newClient.runClient(); // Reconnect to server
                    } else {
                        sendData(Commands.PLAYER_LEFT.toString());
                        System.exit(0);
                    }
                }



                if (message.startsWith("Your accusation was incorrect")) {
                    JOptionPane.showMessageDialog(this, message, "❌ Incorrect Accusation", JOptionPane.WARNING_MESSAGE);
                }


                if (message.equals("YOUR_TURN")) {
                    // Enable your move, suggest, and accuse buttons
                    makeSuggestionButton.setEnabled(true);
                    makeAccusationButton.setEnabled(true);
                    secretPassageButton.setEnabled(true);

                    JOptionPane.showMessageDialog(this,
                            "It's your turn!💃🕺",
                            "Your Turn",
                            JOptionPane.INFORMATION_MESSAGE);

                    //Beep Sound
                    Toolkit.getDefaultToolkit().beep();
                }

                if (message.startsWith("GAME_OVER")) {
                    String winner = message.substring("GAME_OVER".length()).trim();
                    JOptionPane.showMessageDialog(this,
                            "🏆 " + winner + " has won the game! 🏆\nThe game will now close.",
                            "Game Over",
                            JOptionPane.INFORMATION_MESSAGE);

                    sendData(Commands.PLAYER_LEFT.toString()); // Politely tell server you're leaving
                    System.exit(0); // Exit the client
                }


                if (message.equals("PROMPT_SUGGESTION")) {
                    SwingUtilities.invokeLater(() -> {
                        detectiveNotePad.doClick(); // <-- open Detective Notepad automatically
                        makeSuggestionButton.doClick(); // <-- then pop the Suggestion menu
                    });
                }



                if (message.equals("SUGGESTION_NOT_DISPROVED_BY_PREVIOUS")) {
                    JOptionPane.showMessageDialog(this,
                            "The player before you could not disprove your suggestion.",
                            "Suggestion Not Disproved",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                if (message.equals("PROMPT_ACCUSATION_OR_END")) {
                    SwingUtilities.invokeLater(() -> {
                        int response = JOptionPane.showOptionDialog(
                                this,
                                "Would you like to make an accusation or end your turn?",
                                "Choose an Action",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                new String[]{"Make Accusation", "End Turn"},
                                "End Turn"
                        );

                        if (response == JOptionPane.YES_OPTION) {
                            makeAccusationButton.doClick(); // auto-clicks the Accusation button
                        } else {
                            sendData("END_TURN"); // Send new command to the server
                        }
                    });
                }



                //  NEW: Handle custom game messages from Clue-Less server
                if (message.startsWith("LOCATION")) {
                    // Format: "LOCATION Ballroom [2,2]"
                    String[] parts = message.split(" ");
                    String[] coords = parts[2].replace("[", "").replace("]", "").split(",");
                    int row = Integer.parseInt(coords[0]);
                    int col = Integer.parseInt(coords[1]);

                    updateBoard(name, row, col);
                    JOptionPane.showMessageDialog(this, message, "Location", JOptionPane.INFORMATION_MESSAGE);
                }

                if (message.startsWith("MOVED false")) {
                    JOptionPane.showMessageDialog(this, message, "Move Result", JOptionPane.INFORMATION_MESSAGE);
                }

                if (message.startsWith("JOINED") || message.startsWith("FAILED")) {
                    JOptionPane.showMessageDialog(this, message, "Join Result", JOptionPane.INFORMATION_MESSAGE);
                }

                if (message.startsWith("ERROR")) {
                    JOptionPane.showMessageDialog(this, message, "Game Error", JOptionPane.ERROR_MESSAGE);
                }

                if (message.startsWith("ALL_POSITIONS")) {
                    // First, clear all initials from the board
                    for (int r = 0; r < BOARD_SIZE; r++) {
                        for (int c = 0; c < BOARD_SIZE; c++) {
                            JLabel label = boardLabels[r][c];
                            if (label != null && label.getText() != null && label.getText().contains("(")) {
                                String text = label.getText();
                                if (text.contains("<br>")) {
                                    // HTML format (room)
                                    text = text.substring(0, text.indexOf("<br>"));
                                    label.setText(text);
                                } else if (text.contains(" (")) {
                                    // Plain format (hallway)
                                    label.setText(text.substring(0, text.indexOf(" (")));
                                }
                            }
                        }
                    }

                    // Then, re-add every player properly
                    String[] parts = message.split(" ");
                    for (int i = 1; i < parts.length; i++) {
                        String[] tokens = parts[i].split(",");
                        String playerName = tokens[0];
                        int row = Integer.parseInt(tokens[1]);
                        int col = Integer.parseInt(tokens[2]);
                        updateBoard(playerName, row, col);  // use the correct playerName
                    }
                }


//                if (message.startsWith("YOUR_CARDS")) {
//                    String cardsList = message.substring("YOUR_CARDS".length()).trim();
//                    myCards = cardsList; // Save the cards for later
//                    JOptionPane.showMessageDialog(this, "Your cards are:\n" + cardsList,
//                            "Your Cards", JOptionPane.INFORMATION_MESSAGE);
//                }
                if (message.startsWith("YOUR_CARDS")) {
                    String cardsList = message.substring("YOUR_CARDS".length()).trim();
                    myCards = cardsList; // Save the cards for later

                    // ✨ NEW: Populate ALL possible cards into detectiveTable
                    List<String> allCards = Arrays.asList(
                            "MissScarlet", "ColonelMustard", "MrsWhite",
                            "MrGreen", "MrsPeacock", "ProfessorPlum",
                            "Candlestick", "Knife", "LeadPipe", "Revolver", "Rope", "Wrench",
                            "Study", "Hall", "Lounge", "Library", "Billiard Room", "Dining Room",
                            "Conservatory", "Ballroom", "Kitchen"
                    );

                    for (String card : allCards) {
                        detectiveTable.putIfAbsent(card, new HashMap<>()); // create row for card
                    }

                    //  Mark "Me" as owning only the cards I actually have
                    String[] myOwnCards = cardsList.replace("[", "").replace("]", "").split(",");
                    for (String card : myOwnCards) {
                        card = card.trim();
                        if (!card.isEmpty()) {
                            detectiveTable.get(card).put("Me", true); // ✅ check mark only my cards
                        }
                    }

                    JOptionPane.showMessageDialog(this, "Your cards are:\n" + cardsList,
                            "Your Cards", JOptionPane.INFORMATION_MESSAGE);
                }


                if (message.contains("showed you:")) {
                    String[] parts = message.split("showed you:");
                    String disapprovingPlayer = parts[0].trim();
                    String shownCard = parts[1].trim();

                    // Create a detective note entry like "MrsWhite: Revolver"
                    String detectiveEntry = disapprovingPlayer + ": " + shownCard;

                    if (!detectiveNotes.contains(detectiveEntry)) {
                        detectiveNotes.add(detectiveEntry);
                    }

                    //  NEW: Update detectiveTable
                    detectiveTable.putIfAbsent(shownCard, new HashMap<>()); // just in case
                    detectiveTable.get(shownCard).put(disapprovingPlayer, true); //  check mark under that player

                    JOptionPane.showMessageDialog(this,
                            disapprovingPlayer + " disproved your suggestion by showing you: " + shownCard,
                            "Detective Note Updated",
                            JOptionPane.INFORMATION_MESSAGE);
                }


                if (message.startsWith("DISPROVE_OPTIONS")) {
                    SwingUtilities.invokeLater(() -> {
                        detectiveNotePad.doClick(); // <-- pop open the Detective Notes first

                        String[] options = message.substring("DISPROVE_OPTIONS".length()).trim().split(",");
                        String selectedCard = (String) JOptionPane.showInputDialog(
                                this,
                                "Choose a card to disprove the suggestion:",
                                "Disprove Suggestion",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                options,
                                options[0]
                        );
                        if (selectedCard != null) {
                            sendData("DISPROVE_SELECTED " + selectedCard);
                        }
                    });
                }
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }
        } while (!message.equals("SERVER >>> TERMINATE"));
    }

    /**
     * Closes the network connection by shutting down input and output streams and the socket.
     */
    private void closeConnection() {
        try {
            outputStream.close();
            inputStream.close();
            client.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * Sends a message (command or data) to the server through the output stream.
     *
     * @param message the text message to be sent to the server
     */
    private void sendData(String message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush(); // flush data to output
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * Starts the timer that controls the time per round for the original word game.
     */
    private void startTimer() {
        secondsInGame = Client.timePerRound;
        gameTimerLabel.setText(secondsInGame + " seconds");
        timer = new Timer(1000, e -> {
            secondsInGame--;
            gameTimerLabel.setText(secondsInGame + " seconds");
            if (secondsInGame == 0) {
                timer.stop();
                endRound();
            }
        });
    }

    /**
     * Initializes and starts a new round in the word game mode.
     * Sets up relevant labels and timers for the round.
     */
    private void startRound() {

        scrambleForCurrentRoundLabel.setText(spaceScramble(scrambles[clientRound - 1]));
        clientScoreLabel.setText("Current Score: " + clientScore);
        scrambleForCurrentRoundLabel.setVisible(true);
        gameBackgroundLabel.setIcon(gBackImages[0]);
        gameBackgroundLabel.setVisible(true);
        timeRemainingLabel.setVisible(false);
        currentRoundLabel.setVisible(true);
        clientScoreLabel.setVisible(true);
        gameTimerLabel.setVisible(true);
        textField.setVisible(true);
        display.setVisible(true);
        menu.setVisible(false);

        clientScoreLabel.setBounds(40, 340, 200, 50);
        currentRoundLabel.setBounds(70, 310, 150, 50);

        startTimer();
        timer.start();
    }

    /**
     * Ends the current round of the word game and presents options
     * to continue to the next scramble or exit to the main menu.
     */
    private void endRound() {
        sendData("#" + name + " " + clientScore + " " + clientRound);

        continueToNextRoundButton.setVisible(clientRound < 5);


        if (clientRound >= 5) {
            currentRoundLabel.setVisible(false);
            clientScoreLabel.setVisible(false);
            currentRoundLabel.setText("Round " + clientRound);
        }

        clientScoreLabel.setBounds(600, 307, 200, 50);
        currentRoundLabel.setBounds(600, 275, 150, 50);

        exitFromGameToMainMenuButton.setBounds(600, 435, 150, 25);
        scrambleForCurrentRoundLabel.setVisible(false);
        scrambleForCurrentRoundLabel.setVisible(false);
        gameBackgroundLabel.setVisible(false);
        displayLeaderboard.setVisible(true);
        currentRoundLabel.setVisible(true);
        currentRoundLabel.setVisible(true);
        gameTimerLabel.setVisible(false);
        textField.setVisible(false);
        display.setVisible(true);
        menu.setVisible(true);
    }

    private String spaceScramble(String letters) {
        String[] temp = letters.split("");
        StringBuilder s = new StringBuilder();
        for (String l : temp) {
            s.append(l).append(" ");
        }
        return s.toString();
    }

    /**
     * GUI screens setup
     * sets up the main menu of the GUI program
     * has buttons to exit, play the game, and change skin
     */
    private void mainMenuInitialize() {
//        rules.setVisible(false);
        myCardsButton.setVisible(true);
        detectiveNotePad.setVisible(true);
        scrollPane.setVisible(false);
        displayRules.setVisible(true);
        menu.setVisible(true);
        backToMainMenuFromSkinsButton.setVisible(false);
        exitFromGameToMainMenuButton.setVisible(false);
        exitFromGameToMainMenuButton.setVisible(false);
        scrambleForCurrentRoundLabel.setVisible(false);
        continueToNextRoundButton.setVisible(false);
        exitTheApplicationButton.setVisible(true);
        joinTheTournamentButton.setVisible(false); //TODO set to false cant delete will break the game
        gameBackgroundLabel.setVisible(false);
        timeRemainingLabel.setVisible(false);
        displayLeaderboard.setVisible(true);
        currentRoundLabel.setVisible(false);
        clientScoreLabel.setVisible(false);
        imagesJComboBox.setVisible(false);
        gameTimerLabel.setVisible(false);
        currentName.setVisible(false);
        skinsButton.setVisible(true);
        chooseName.setVisible(true);
        textField.setVisible(false);
        enterName.setVisible(false);
        gameLogo.setVisible(false);
        gameLogo.setVisible(false);
        display.setVisible(true);
        if (scrollPane != null) scrollPane.setVisible(false);
    }

    /**
     * adds all the necessary components, helper method
     */
    private void addJFrameComponents() {
//        add(rules);
        add(makeAccusationButton);
        add(displayRules);
        add(displayLeaderboard);
        add(gameLogo);
        add(exitTheApplicationButton);
        add(enterName);
        add(chooseName);
        add(currentName);
        add(joinTheTournamentButton);
        add(chooseName);
        add(skinsButton);
        add(backToMainMenuFromSkinsButton);
        add(timeRemainingLabel);
        add(exitFromGameToMainMenuButton);
        add(imagesJComboBox);
        add(gameTimerLabel);
        add(textField);
        add(continueToNextRoundButton);
        add(currentRoundLabel);
        add(clientScoreLabel);
        add(scrambleForCurrentRoundLabel);
        add(gameBackgroundLabel);
        add(myCardsButton);
        add(detectiveNotePad);
        add(makeSuggestionButton);
        add(menu);
        add(display);

        makeSuggestionButton.setEnabled(false);
        makeAccusationButton.setEnabled(false);
        secretPassageButton.setEnabled(false);

        mainMenuInitialize();

        if (scrollPane != null) {
            add(scrollPane); // must be last to control visibility correctly
        }
    }

    /**
     * sets the bounds for all the needed button on the menu
     */
    private void initiateBounds() {



        menu.setBounds(-10, -50, 800, 600); //-10, -50, 800, 600
        makeSuggestionButton.setBounds(150, 20, 150, 25); //New Suggestion button
        makeAccusationButton.setBounds(600, 404, 150, 25);//600, 372, 150, 25
        backToMainMenuFromSkinsButton.setBounds(600, 400, 150, 50);
        scrambleForCurrentRoundLabel.setBounds(50, 225, 750, 60);
        displayRules.setBounds(600, 307, 150, 25); //600,275,150,25
        exitFromGameToMainMenuButton.setBounds(600, 435, 150, 25);
        exitTheApplicationButton.setBounds(600, 436, 150, 25); // 600, 435, 150, 25
//        joinTheTournamentButton.setBounds(600, 372, 150, 25);
        timeRemainingLabel.setBounds(50, 225, 700, 100);
        continueToNextRoundButton.setBounds(600,403,150,25);
        currentRoundLabel.setBounds(70, 310, 150, 50);
        gameBackgroundLabel.setBounds(0, 0, 800, 600);
        gameTimerLabel.setBounds(565, -10, 225, 150);
        imagesJComboBox.setBounds(600, 275, 150, 50);
//        displayLeaderboard.setBounds(600, 372, 150, 25);
        myCardsButton.setBounds(600, 339, 150, 25);
        detectiveNotePad.setBounds(600, 372, 150, 25);
        clientScoreLabel.setBounds(40, 340, 200, 50);
        enterName.setBounds(600, 275, 150, 50);
        textField.setBounds(250, 350, 400, 50);
        currentName.setBounds(600, 350, 150, 50);
//        chooseName.setBounds(600, 307, 150, 25);
        gameLogo.setBounds(0, -100, 800, 800);
        gameLogo.setBounds(0, -100, 800, 800);
        display.setBounds(0, 0, 800, 600);
        display.setBounds(0, 0, 800, 600);
    }

    /**
     * puts the main menu text in the correct place
     */
    private void initiateText() {
        scrambleForCurrentRoundLabel.setFont(new Font("Comic Sans", Font.PLAIN, 48));
        timeRemainingLabel.setFont(new Font("Comic Sans", Font.PLAIN, 48));
        currentRoundLabel.setFont(new Font("Comic Sans", Font.PLAIN, 24));
        currentRoundLabel.setFont(new Font("Comic Sans", Font.PLAIN, 24));
        clientScoreLabel.setFont(new Font("Comic Sans", Font.BOLD, 14));
        gameTimerLabel.setFont(new Font("Comic Sans", Font.PLAIN, 36));
        timeRemainingLabel.setForeground(Color.black);
        timeRemainingLabel.setBackground(Color.white);
        timeRemainingLabel.setOpaque(true);

        scrambleForCurrentRoundLabel.setHorizontalAlignment(SwingConstants.CENTER);

        backToMainMenuFromSkinsButton.setText("Main Menu");
        exitFromGameToMainMenuButton.setText("Exit from Game");
        continueToNextRoundButton.setText("Next Scramble");
        joinTheTournamentButton.setText("Join Tournament");
        displayLeaderboard.setText("Leaderboard");
        myCardsButton.setText("My Cards");
        detectiveNotePad.setText("Detective Notepad");
        exitTheApplicationButton.setText("Exit");
        displayRules.setText("Rules");
        myCardsButton.setText("My Cards");
        detectiveNotePad.setText("Detective Notepad");
        currentName.setText("Name: " + name);
        currentName.setText("Name: " + name);
        chooseName.setText("Choose Name");
        skinsButton.setText("Skins");

        timeRemainingLabel.setHorizontalAlignment(JLabel.CENTER);
    }

    /**
     * dropdown box to show the available skins
     */
    private void showBackgroundOptions() {
        imagesJComboBox.setVisible(true);
    }

    /**
     * this will start playing the audio that we have implemented
     */
    private void audioEnabler() {
        try {
            String soundName = "dubstepJeaprody.wav"; //found on https://www.youtube.com/watch?v=CN1yxadBBEU
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(Objects.requireNonNull(getClass().getResource(soundName)).toURI()));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            //clip.loop();
        } catch (UnsupportedAudioFileException | URISyntaxException | LineUnavailableException | IOException ex) {
            System.out.println("Audio is not working");
        }
    }

    /**
     * Hides all major screens and components when navigating between different GUI states.
     */
    private void hideAllScreens() {
        menu.setVisible(false);
        myCardsButton.setVisible(true);
        detectiveNotePad.setVisible(true);
        gameBackgroundLabel.setVisible(false);
        display.setVisible(false);

        if (scrollPane != null) scrollPane.setVisible(false);
        gameLogo.setVisible(false);
    }

    /**
     * Updates the visual position of a player on the board based on their row and column.
     *
     * @param playerName the name of the player to update
     * @param row the new row position
     * @param col the new column position
     */
    private void updateBoard(String playerName, int row, int col) {

        JLabel current = boardLabels[row][col];
        if (current == null) return;

        String initials = getInitials(playerName);
        String tooltip = current.getToolTipText();

        if (playerName.equals(name)) {
            currentPlayerRow = row;
            currentPlayerCol = col;

            boolean inSecretPassageRoom = secretPassageRooms.contains(new Point(row, col));
            secretPassageButton.setEnabled(inSecretPassageRoom);
        }


        if ("Hallway".equals(tooltip)) {
            // Inline initials for hallway
            String base = current.getText() != null ? current.getText().split(" ")[0] : "Hallway";
            current.setText(base + " (" + initials + ")");
        } else {
            // Show initials below the room name using HTML
            String existing = current.getText();
            String roomName = existing.contains("<br>") ? existing.substring(0, existing.indexOf("<br>")) : existing;
            String initialLine = existing.contains("<br>") ? existing.substring(existing.indexOf("<br>") + 4) : "";


            Set<String> initialsSet = new HashSet<>();
            for (String s : initialLine.replace("(", "").replace(")", "").split(", ")) {
                if (!s.trim().isEmpty()) {
                    initialsSet.add(s.trim());
                }
            }
            initialsSet.add(initials);

            String joinedInitials = String.join(", ", initialsSet);

            current.setText("<html><center>" + roomName + "<br>(" + joinedInitials + ")</center></html>");
        }
    }

    /**
     * Converts a character name into a set of initials (e.g., "MissScarlet" -> "MS").
     *
     * @param characterName the full character name
     * @return the initials in uppercase
     */
    private String getInitials(String characterName) {
        // Convert names like "MissScarlet" to "MS", "ProfessorPlum" to "PP"
        StringBuilder initials = new StringBuilder();
        for (String part : characterName.split("(?=[A-Z])")) {  // Split camel case
            if (!part.isEmpty()) initials.append(part.charAt(0));
        }
        return initials.toString().toUpperCase();
    }

    /**
     * Determines if a given card is a suspect.
     *
     * @param card the name of the card
     * @return true if the card is a suspect, false otherwise
     */
    private boolean isSuspect(String card) {
        return Arrays.asList("MissScarlet", "ColonelMustard", "MrsWhite", "MrGreen", "MrsPeacock", "ProfessorPlum")
                .contains(card);
    }

    /**
     * Determines if a given card is a weapon.
     *
     * @param card the name of the card
     * @return true if the card is a weapon, false otherwise
     */
    private boolean isWeapon(String card) {
        return Arrays.asList("Candlestick", "Knife", "LeadPipe", "Revolver", "Rope", "Wrench")
                .contains(card);
    }

    /**
     * Determines if a given card is a room.
     *
     * @param card the name of the card
     * @return true if the card is a room, false otherwise
     */
    private boolean isRoom(String card) {
        return Arrays.asList("Study", "Hall", "Lounge", "Library", "Billiard Room", "Dining Room", "Conservatory", "Ballroom", "Kitchen")
                .contains(card);
    }
}
