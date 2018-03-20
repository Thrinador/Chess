package game;

/**
 * PiecType is an enum that stores the different pieces as an int.
 *
 * @author Ben Clark
 */
public enum PieceType {

    PAWN(1),
    ROOK(2),
    KNIGHT(3),
    BISHOP(4),
    QUEEN(5),
    KING(6);

    final int type;

    /**
     * PieceType sets the type to one of the type.
     *
     * @param type Value of the piece.
     */
    PieceType(int type) {
        this.type = type;
    }
}
