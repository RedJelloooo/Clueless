
/**
 * The Suggestion class represents a suggestion made by a player during the Clue-Less game.
 *
 * A suggestion consists of a suspect (character), a weapon, and the room
 * where the suggestion is made. The suggestion is tied to the player who made it.
 *
 *  Authors:
 *  - Albert Rojas
 *
 */
public class Suggestion {
    private final String suggestingPlayer;
    private final String suspect;
    private final String weapon;
    private final String room;

    /**
     * Constructs a Suggestion with the specified suggesting player, suspect, weapon, and room.
     *
     * @param suggestingPlayer the name of the player making the suggestion
     * @param suspect the name of the character being suggested
     * @param weapon the name of the weapon being suggested
     * @param room the name of the room where the suggestion is made
     */
    public Suggestion(String suggestingPlayer, String suspect, String weapon, String room) {
        this.suggestingPlayer = suggestingPlayer;
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
    }

    /**
     * Gets the name of the player who made the suggestion.
     *
     * @return the suggesting player's name
     */
    public String getSuggestingPlayer() { return suggestingPlayer; }

    /**
     * Gets the suspect character of the suggestion.
     *
     * @return the name of the suspect character
     */
    public String getSuspect() { return suspect; }

    /**
     * Gets the weapon involved in the suggestion.
     *
     * @return the name of the weapon
     */
    public String getWeapon() { return weapon; }

    /**
     * Gets the room where the suggestion was made.
     *
     * @return the name of the room
     */
    public String getRoom() { return room; }


    /**
     * Returns a string representation of the suggestion, summarizing the player,
     * suspect, weapon, and room involved.
     *
     * @return a formatted string describing the suggestion
     */
    @Override
    public String toString() {
        return suggestingPlayer + " suggests it was " + suspect + " in the " + room + " with the " + weapon;
    }
}
