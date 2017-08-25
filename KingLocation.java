package chess;

/**
 * KingLocation is a simple class used to store both kings location, and then
 * retrieve them based off the turn.
 *
 * @author Ben Clark
 */
public class KingLocation {

    /**
     * The current xy locations for both kings
     */
    private int whiteKingX;
    private int whiteKingY;
    private int blackKingX;
    private int blackKingY;

    /**
     * KingLocation takes in the white king's location, and black king's
     * location then assigns them to the globals.
     *
     * @param wKingX white king's x location
     * @param wKingY white king's y location
     * @param bKingX black king's x location
     * @param bKingY black king's y location
     */
    public KingLocation(int wKingX, int wKingY, int bKingX, int bKingY) {
        whiteKingX = wKingX;
        whiteKingY = wKingY;
        blackKingX = bKingX;
        blackKingY = bKingY;
    }

    /**
     * getKingX gets the kings x location based off of turn. true = white kings
     * x
     *
     * @param turn the current turn
     * @return the x location for the king
     */
    int getKingX(boolean turn) {
        return turn ? whiteKingX : blackKingX;
    }

    /**
     * getKingY gets the kings y location based off of turn. true = white kings
     * y
     *
     * @param turn the current turn
     * @return the y location for the king
     */
    int getKingY(boolean turn) {
        return turn ? whiteKingY : blackKingY;
    }

    /**
     * setKingLocation takes in the turn and the king's new location. sets the 
     * king's location
     * @param turn which king to set
     * @param kingX the new x for the king
     * @param kingY the new y for the king
     */
    void setKingLocation(boolean turn, int kingX, int kingY) {
        if (turn) {
            whiteKingX = kingX;
            whiteKingY = kingY;
        } else {
            blackKingX = kingX;
            blackKingY = kingY;
        }
    }
}
