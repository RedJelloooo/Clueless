import java.util.List;
import java.util.ArrayList;

public class PlayerState {
    private final String playerId;
    private final String characterName;
    private int row;
    private int col;
    private final List<String> cards;
    private boolean recentlyMovedBySuggestion;

    public PlayerState(String playerId, String characterName, int row, int col) {
        this.playerId = playerId;
        this.characterName = characterName;
        this.row = row;
        this.col = col;
        this.cards = new ArrayList<>();
        this.recentlyMovedBySuggestion = false;
    }

    public String getPlayerId() { return playerId; }
    public String getCharacterName() { return characterName; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public List<String> getCards() { return cards; }
    public void addCard(String card) { cards.add(card); }

    public boolean wasMovedBySuggestion() { return recentlyMovedBySuggestion; }
    public void setMovedBySuggestion(boolean flag) { recentlyMovedBySuggestion = flag; }
}
