public class Suggestion {
    private final String suggestingPlayer;
    private final String suspect;
    private final String weapon;
    private final String room;

    public Suggestion(String suggestingPlayer, String suspect, String weapon, String room) {
        this.suggestingPlayer = suggestingPlayer;
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
    }

    public String getSuggestingPlayer() { return suggestingPlayer; }
    public String getSuspect() { return suspect; }
    public String getWeapon() { return weapon; }
    public String getRoom() { return room; }

    @Override
    public String toString() {
        return suggestingPlayer + " suggests it was " + suspect + " in the " + room + " with the " + weapon;
    }
}
