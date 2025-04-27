import java.util.List;
import java.util.ArrayList;

/**
 * The PlayerState class represents the current status of a player in the Clue-Less game.
 *
 * It tracks the player's ID, character name, position on the board (row and column),
 * the cards the player holds, and whether the player was recently moved due to a suggestion.
 *
 * * Authors:
 *  * - Albert Rojas
 *
 */
public class PlayerState {
    private final String playerId;
    private final String characterName;
    private int row;
    private int col;
    private final List<String> cards;
    private boolean recentlyMovedBySuggestion;

    /**
     * Constructs a PlayerState with the specified player ID, character name, and starting position.
     *
     * @param playerId the unique identifier for the player
     * @param characterName the character assigned to the player
     * @param row the initial row position on the board
     * @param col the initial column position on the board
     */
    public PlayerState(String playerId, String characterName, int row, int col) {
        this.playerId = playerId;
        this.characterName = characterName;
        this.row = row;
        this.col = col;
        this.cards = new ArrayList<>();
        this.recentlyMovedBySuggestion = false;
    }

    /**
     * Gets the unique player ID.
     *
     * @return the player's ID
     */
    public String getPlayerId() { return playerId; }

    /**
     * Gets the name of the character assigned to the player.
     *
     * @return the character's name
     */
    public String getCharacterName() { return characterName; }


    /**
     * Gets the player's current row position.
     *
     * @return the current row
     */
    public int getRow() { return row; }

    /**
     * Gets the player's current column position.
     *
     * @return the current column
     */
    public int getCol() { return col; }


    /**
     * Updates the player's current position on the board.
     *
     * @param row the new row position
     * @param col the new column position
     */
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Retrieves the list of cards currently held by the player.
     *
     * @return the list of card names
     */
    public List<String> getCards() { return cards; }

    /**
     * Adds a card to the player's list of cards.
     *
     * @param card the card to add
     */
    public void addCard(String card) { cards.add(card); }

    /**
     * Checks whether the player was recently moved into a room due to another player's suggestion.
     *
     * @return true if the player was moved by a suggestion, false otherwise
     */
    public boolean wasMovedBySuggestion() { return recentlyMovedBySuggestion; }

    /**
     * Sets whether the player has been moved into a room by a suggestion.
     *
     * @param flag true if moved by a suggestion, false otherwise
     */
    public void setMovedBySuggestion(boolean flag) { recentlyMovedBySuggestion = flag; }
}
