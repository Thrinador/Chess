package game;

import javafx.scene.image.ImageView;

/**
 * Piece represents a piece in chess. It contains it's x, y location in pixels
 * its type, team, whether or not it has moved and some mouse listener for click
 * and drag movement.
 *
 * @author Ben Clark
 */
public class Piece extends ImageView {

    private double mouseX, mouseY;
    private double oldX, oldY;
    private boolean team;
    private boolean moved;
    private PieceType type;

    /**
     * Piece takes in a string location for it's image, its x,y location, the
     * piece's team, and the type of piece.
     *
     * @param url the string location of the piece
     * @param x the x coordinate for the piece
     * @param y the y coordinate for the piece
     * @param team the piece's team
     * @param type the piece's type
     * @param game
     */
    public Piece(String url, int x, int y, boolean team, PieceType type, Game game) {
        super(url);
        move(x, y);
        moved = false;
        this.team = team;
        this.type = type;

        setOnMousePressed(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        setOnMouseDragged(e -> {
            if (!game.gameAlive.getValue() || this.team != game.getTurn()
                    || game.getTeam() != game.getTurn()) {
                return;
            }
            if (team) {
                relocate(e.getSceneX() - mouseX + oldX, e.getSceneY() - mouseY + oldY);
            } else {
                relocate(mouseX + oldX - e.getSceneX(), mouseY + oldY - e.getSceneY());
            }
        });

        setOnMouseReleased(e -> {
            move(e.getSceneX() - mouseX + oldX, e.getSceneY() - mouseY + oldY);
        });
    }

    /**
     * getOldX gives the x location of this piece in pixels
     *
     * @return x location in pixels
     */
    public double getOldX() {
        return oldX;
    }

    /**
     * getOldY gives the y location of this piece in pixels
     *
     * @return y location in pixels
     */
    public double getOldY() {
        return oldY;
    }

    /**
     * getTeam gets the current piece's team
     *
     * @return the piece's team
     */
    public boolean getTeam() {
        return team;
    }

    /**
     * move takes in an x y location for the piece and moves the image to that
     * location
     *
     * @param x the new x location for the piece
     * @param y the new y location for the piece
     */
    public void move(double x, double y) {
        moved = true;
        oldX = x * Game.TILE_SIZE + Game.OFFSET;
        oldY = y * Game.TILE_SIZE + Game.OFFSET;
        relocate(oldX, oldY);
    }

    /**
     * abortMove recenters the piece on the square it came from
     */
    public void abortMove() {
        relocate(oldX, oldY);
    }

    /**
     * getType returns the pieces type
     *
     * @return the piece's type
     */
    public PieceType getType() {
        return type;
    }

    /**
     * setType sets the piece type
     *
     * @param type the new piece type
     */
    public void setType(PieceType type) {
        this.type = type;
    }

    /**
     * hasMoved returns whether or not the piece has moved
     *
     * @return whether or not the piece has moved
     */
    public boolean hasMoved() {
        return moved;
    }

    /**
     * setMoved sets whether or not the piece has moved.
     *
     * @param moved the new moved value
     */
    public void setMoved(boolean moved) {
        this.moved = moved;
    }
}
