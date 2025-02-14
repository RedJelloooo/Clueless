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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.sound.sampled.*;
import javax.swing.*;

/**
 * @author Brandon Cano (Server and Client Logic)
 * @author Alex Arand (GUI Implementation)
 * This is the client class that handles our game on the player end
 * This sets up a GUI with multiple backgrounds as well as
 * keeps track of the player score and round
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

    // GUI components
    private final JButton backToMainMenuFromSkinsButton;
    private final JButton exitFromGameToMainMenuButton;
    private final JButton continueToNextRoundButton;
    private final JButton exitTheApplicationButton;
    private final JButton joinTheTournamentButton;
    private final JButton displayLeaderboard;
    private final JButton skinsButton;
    private final JButton chooseName;
    private final JButton displayRules;


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

    /**
     * file names for the images
     */
    private static final String[] names = {
            "oldCap.png", //https://www.uiowa.edu
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
     * creates a GUI interface on the client
     * also sends s a message to the server that the client is connected
     *
     * @param host - the IP address of the host to connect to
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
        skinsButton = new JButton();
        chooseName = new JButton();
        displayRules = new JButton();

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
        //setup for rules
        URL rulesURL = getClass().getResource("rules.png");
        if (rulesURL != null) {
            ImageIcon rule = new ImageIcon(rulesURL);
            rules = new JLabel(rule);
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
            joinTheTournamentButton.setVisible(true);
            displayLeaderboard.setVisible(true);
            imagesJComboBox.setVisible(false);
            skinsButton.setVisible(true);
            enterName.setVisible(false);
            chooseName.setVisible(true);
            display.setVisible(true);
            displayRules.setVisible(true);
            rules.setVisible(false);
            mainMenuInitialize();
        });

        displayRules.addActionListener(e ->{
            menu.setVisible(false);
            backToMainMenuFromSkinsButton.setVisible(true);
            exitTheApplicationButton.setVisible(false);
            joinTheTournamentButton.setVisible(false);
            displayLeaderboard.setVisible(false);
            skinsButton.setVisible(false);
            chooseName.setVisible(false);
            displayRules.setVisible(false);
            rules.setVisible(true);
            backToMainMenuFromSkinsButton.setBounds(600, 450, 150, 50);
        });

        joinTheTournamentButton.addActionListener(e -> {
            if (clientRound >= 5) {
                return;
            }

            exitFromGameToMainMenuButton.setBounds(620, 500, 150, 50);

            audioEnabler();

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
     * Will attempt to connect the client to the server and set up the streams for the IO.
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
     * This will take any message and process it so that it can be shown in the client side.
     *
     * @throws IOException for when the streams fail
     */
    private void processConnection() throws IOException {
        sendData(Commands.PLAYER_JOINED.toString());

        do { // process message
            try {
                if (haveScramble) {
                    message = (String) inputStream.readObject();
                } else {
                    scrambles = (String[]) inputStream.readObject();
                    haveScramble = true;
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
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }
        } while (!message.equals("SERVER >>> TERMINATE"));
    }

    /**
     * This will close the connection of the client.
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
     * This will take the message entered in and send it to the server.
     *
     * @param message text to be sent to server
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
     * starts the timer for the game
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
     * Starts the round for the word game
     * creates new labels and hides the unused labels
     * also starts the time
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
     * Ends the current round of the word game
     * and shows the user a menu to play a new shuffle
     * or exit the game
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
        rules.setVisible(false);
        displayRules.setVisible(true);
        menu.setVisible(true);
        backToMainMenuFromSkinsButton.setVisible(false);
        exitFromGameToMainMenuButton.setVisible(false);
        exitFromGameToMainMenuButton.setVisible(false);
        scrambleForCurrentRoundLabel.setVisible(false);
        continueToNextRoundButton.setVisible(false);
        exitTheApplicationButton.setVisible(true);
        joinTheTournamentButton.setVisible(true);
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
        gameLogo.setVisible(true);
        gameLogo.setVisible(true);
        display.setVisible(true);
    }

    /**
     * adds all the necessary components, helper method
     */
    private void addJFrameComponents() {
        add(rules);
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
        add(menu);
        add(display);
    }

    /**
     * sets the bounds for all the needed button on the menu
     */
    private void initiateBounds() {
        rules.setBounds(0,0,800,600);
        menu.setBounds(-10, -50, 800, 600);
        backToMainMenuFromSkinsButton.setBounds(600, 400, 150, 50);
        scrambleForCurrentRoundLabel.setBounds(50, 225, 750, 60);
        displayRules.setBounds(600,275,150,25);
        exitFromGameToMainMenuButton.setBounds(600, 435, 150, 25);
        exitTheApplicationButton.setBounds(600, 435, 150, 25);
        joinTheTournamentButton.setBounds(600, 339, 150, 25);
        timeRemainingLabel.setBounds(50, 225, 700, 100);
        continueToNextRoundButton.setBounds(600,403,150,25);
        currentRoundLabel.setBounds(70, 310, 150, 50);
        gameBackgroundLabel.setBounds(0, 0, 800, 600);
        gameTimerLabel.setBounds(565, -10, 225, 150);
        imagesJComboBox.setBounds(600, 275, 150, 50);
        displayLeaderboard.setBounds(600, 371, 150, 25);
        clientScoreLabel.setBounds(40, 340, 200, 50);
        enterName.setBounds(600, 275, 150, 50);
        textField.setBounds(250, 350, 400, 50);
        skinsButton.setBounds(600, 403, 150, 25);
        currentName.setBounds(600, 350, 150, 50);
        chooseName.setBounds(600, 307, 150, 25);
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
        exitTheApplicationButton.setText("Exit");
        displayRules.setText("Rules");
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
}
