package game;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Tile represents one of the tiles of the board. Its contains a piece.
 *
 * @author Ben Clark
 */
public class Tile extends Rectangle {

    /**
     * The Piece that tile stores
     */
    private Piece piece;

    /**
     * Tile takes in an xy location for the tile and whether or not it is a 
     * light tile. Moves the tile to that location and fills the color either
     * light or dark depending on the boolean.
     * @param light whether or not this is a light tile
     * @param x the x location for this tile
     * @param y the y location for this tile
     */
    public Tile(boolean light, int x, int y) {
        setWidth(Game.TILE_SIZE);
        setHeight(Game.TILE_SIZE);
        relocate(x * Game.TILE_SIZE, y * Game.TILE_SIZE);
        setFill(light ? Color.valueOf("#FECEA0") : Color.valueOf("#D18B46"));
    }

    /**
     * hasPiece checks whether or not this tile has a piece.
     * @return whether or not piece is null
     */
    public boolean hasPiece() {
        return piece != null;
    }

    /**
     * getPiece gets the piece associated with this tile
     * @return the piece from this tile
     */
    public Piece getPiece() {
        return piece;
    }

    /**
     * setPiece sets the piece value for this tile.
     * @param piece the new piece for this tile
     */
    public void setPiece(Piece piece) {
        this.piece = piece;
    }
}
