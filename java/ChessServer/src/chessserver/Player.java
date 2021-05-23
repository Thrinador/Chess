package chessserver;

/**
 * Player extends from player by adding the color that the member is playing and
 * the name of the game that the member is in.
 *
 * @author Ben Clark
 */
public class Player extends Member {

    private final boolean pieceColor;
    private final String gameName;

    /**
     * Constructor that takes in a member that we send to be copied, a gameName
     * and a color. If the Color is "white" it becomes true else false.
     *
     * @param m Member to copy data from
     * @param gameName Name of the game the player is in.
     * @param color A string representing the color that the player is playing
     */
    public Player(Member m, String gameName, String color) {
        super(m);
        this.gameName = gameName;
        pieceColor = color.toLowerCase().equals("white");
    }

    /**
     * Getter for the game color, returns in String form where white is true and
     * black is false
     *
     * @return A String representing the color that the player is playing
     */
    public String getGameColor() {
        return pieceColor ? "white" : "black";
    }

    /**
     * Getter for the name of the game.
     *
     * @return The name of the game that the player is in.
     */
    public String getGameName() {
        return gameName;
    }
}
