package game;

/**
 * PiecType is an enum that stores the different pieces as an char.
 *
 * @author Ben Clark
 */
public enum PieceType {

    PAWN('p'),
    ROOK('r'),
    KNIGHT('n'),
    BISHOP('b'),
    QUEEN('q'),
    KING('k');

    final char type;

    /**
     * PieceType sets the type to one of the type.
     *
     * @param type Value of the piece.
     */
    PieceType(char type) {
        this.type = type;
    }
}
