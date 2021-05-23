package networking;

/**
 * Player extends User by adding information about the name of the game, hostip,
 * and colorToPlay. Also adds functionality for getting this data.
 *
 * @author Ben Clark
 */
public class Player extends User {

    private final String gameName;
    private final String hostip;
    private final boolean colorToPlay;

    /**
     * Constructor that takes in all the data, and assigns it accordingly.
     *
     * @param username The username for the Player
     * @param gameName The name of the game that the player is in.
     * @param hostip The ip for the host.
     * @param colorToPlay The color that the player is playing
     * @param wins The number of wins that the player has.
     */
    public Player(String username, String gameName, String hostip, boolean colorToPlay, int wins) {
        super(username, wins);
        this.gameName = gameName;
        this.hostip = hostip;
        this.colorToPlay = colorToPlay;
    }

    /**
     * Constructor that takes in a user to copy from, and the player data to
     * store.
     *
     * @param user The user to copy from.
     * @param gameName The name of the game the player is in.
     * @param hostip The ip of the host.
     * @param colorToPlay The color that the player is playing.
     */
    public Player(User user, String gameName, String hostip, boolean colorToPlay) {
        super(user);
        this.gameName = gameName;
        this.hostip = hostip;
        this.colorToPlay = colorToPlay;
    }

    /**
     * Getter for the name of the game.
     *
     * @return The name of the game.
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * Getter for color to play. Converts it to a string where true is white.
     *
     * @return The color to play in string form.
     */
    public String getColorToPlay() {
        return colorToPlay ? "white" : "black";
    }

    /**
     * Getter for the color in boolean form.
     *
     * @return The color to play in boolean form.
     */
    public boolean getColor() {
        return colorToPlay;
    }

    /**
     * Getter for the host ip.
     *
     * @return The host ip.
     */
    public String getIp() {
        return hostip;
    }

    /**
     * Returns the information in a label form, used in score.
     *
     * @return Score label form of information.
     */
    public String labelDisplay() {
        return " " + getUsername() + ": " + getWins();
    }

    @Override
    public String toString() {
        return super.toString() + " " + gameName + " " + getColorToPlay();
    }
}
