package game;

/**
 * MoveType is an enum used to make the algebraic notation. Stores the different
 * types of moves that are possible in a chess game.
 *
 * @author Ben Clark
 */
public enum MoveType {

    MOVE(0),
    CAPTURE(1),
    KING_CASTLE(2),
    QUEEN_CASTLE(3),
    CHECK(4),
    CHECK_MATE(5),
    PAWN_PROMOTION(6),
    EN_PASSANT(7);

    final int type;

    /**
     * assigns the value to the enum.
     *
     * @param type the value of the enum
     */
    MoveType(int type) {
        this.type = type;
    }
}
