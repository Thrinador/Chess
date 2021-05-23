package chessserver;

import java.net.InetAddress;

/**
 * Member is a client who has registered with the server. Once an incoming
 * request has registered the server will store information about them.
 *
 * @author Ben Clark
 */
public class Member {

    private final String username;
    private InetAddress inet;
    private int gamesWon;

    /**
     * Constructor for new registers. Records there InetAddress and their
     * username
     *
     * @param inet The InetAddress for the Member
     * @param username The username for the member
     */
    public Member(InetAddress inet, String username) {
        this.inet = inet;
        this.username = username;
        this.gamesWon = 0;
    }

    /**
     * Copy constructor that copies all the data from the incoming object
     *
     * @param m The member we are coping data from.
     */
    public Member(Member m) {
        this.inet = m.inet;
        this.username = m.username;
        this.gamesWon = m.gamesWon;
    }

    /**
     * Getter for the username
     *
     * @return The current username for the member
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter for the IPAddress. Which is found from the InetAddress.
     *
     * @return The members current IPAddress
     */
    public String getIPAddress() {
        return inet.getHostAddress();
    }

    /**
     * Getter for the games won.
     *
     * @return The number of games won
     */
    public int getGamesWon() {
        return gamesWon;
    }

    /**
     * Increments the number of games won.
     */
    public void wonGame() {
        gamesWon++;
    }

    /**
     * Checks to see if the incoming object is an equivalent Member. Two members
     * are equivalent if they have the same username.
     *
     * @param obj The object to compare against
     * @return Whether or not the two objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        Member other = (Member) obj;
        return other.getUsername().equals(username);
    }
}
