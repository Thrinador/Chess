package networking;

/**
 * User represents a person playing chess, they store their username and number
 * of wins they have.
 *
 * @author Ben Clark
 */
public class User {

    private String username;
    private int wins;

    /**
     * Default constructor that sets the username to null, and the number of
     * wins to -1
     */
    public User() {
        username = null;
        wins = -1;
    }

    /**
     * Constructor that takes in the username and wins, and assigns them.
     *
     * @param username The username for the user
     * @param wins The number of wins that the user has
     */
    public User(String username, int wins) {
        this.username = username;
        this.wins = wins;
    }

    /**
     * Copy constructor that copies all the data
     *
     * @param other The User that we are coping.
     */
    public User(User other) {
        this.username = other.username;
        this.wins = other.wins;
    }

    /**
     * Getter for the username
     *
     * @return The username for the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter for the number of wins
     *
     * @return The number of wins for the user
     */
    public int getWins() {
        return wins;
    }

    /**
     * Setter for the username.
     *
     * @param username New username for the user.
     */
    public void setUsername(String username) {
        if (username != null && username.length() <= 15) {
            this.username = username;
        }
    }

    /**
     * Setter for the number of wins.
     *
     * @param wins New Number of wins for the user.
     */
    public void setWins(int wins) {
        if (wins >= 0) {
            this.wins = wins;
        }
    }

    /**
     * Checks to see if the user has a username.
     *
     * @return Is the username null
     */
    public boolean hasUsername() {
        return username != null;
    }

    /**
     * Removes the username and sets the username to null
     */
    public void removeUsername() {
        username = null;
    }

    @Override
    public String toString() {
        return username + " " + wins;
    }
}
