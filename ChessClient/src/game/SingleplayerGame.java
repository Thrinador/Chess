package game;

import javafx.scene.control.Alert;
import javafx.stage.Window;

/**
 * SingleplayerGame extends Game by adding functionality to both the makeMove
 * and changeTurn methods.
 *
 * @author Ben Clark
 */
public class SingleplayerGame extends Game {

    /**
     * Makes move takes in two positions, a character of what piece to promote
     * to if it is a pawn move and the team of the movement.
     *
     * @param oldX Starting x position for the piece that is trying to be moved
     * @param oldY Starting y position for the piece that is trying to be moved
     * @param newX Ending x position for the piece that is trying to be moved
     * @param newY Ending y position for the piece that is trying to be moved
     * @param pp Piece to promote the pawn to.
     * @param team Team of the player that is moving.
     * @return Whether or not that was a valid move.
     */
    @Override
    public boolean makeMove(int oldX, int oldY, int newX, int newY, char pp, boolean team) {
        //Not your turn or no piece in starting location
        if (team != getTurn() || !board[oldX][oldY].hasPiece()) {
            return false;
        }

        Piece piece = board[oldX][oldY].getPiece();

        //is that a valied move?
        if (!pieceCanMove(oldX, oldY, newX, newY)) {
            piece.abortMove();
            return false;
        }

        if (typeOfMove == null) {
            typeOfMove = MoveType.MOVE;
        }

        //Piece to Kill
        if (board[newX][newY].hasPiece()) {
            resetDrawConditions();
            Piece killedPiece = board[newX][newY].getPiece();
            pieceGroup.getChildren().remove(killedPiece);
            typeOfMove = MoveType.CAPTURE;
        }
        board[newX][newY].setPiece(piece);
        board[oldX][oldY].setPiece(null);

        switch (piece.getType()) {
            case PAWN:
                resetDrawConditions();
                if (newY == 0 || newY == 7) {

                    pawnPromotion(newX, newY, new CharacterHolder(pp));
                } else if (Math.abs(newY - oldY) == 2) {
                    doubleMovePawn = piece;
                } else {
                    doubleMovePawn = null;
                }
                break;
            case KING:
                kingLoc.setKingLocation(getTurn(), newX, newY);
            default:
                doubleMovePawn = null;
        }

        addBoardToMap();
        piece.move(newX, newY);
        changeTurn();
        if (inCheck(kingLoc.getKingX(getTurn()), kingLoc.getKingY(getTurn()))) {
            typeOfMove = MoveType.CHECK;
            if (checkMate()) {
                addMoveToHistory(oldX, oldY, newX, newY);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Game Over");
                Window window = this.getScene().getWindow();
                alert.initOwner(window);
                alert.setHeaderText(null);
                if (getTurn()) {
                    alert.setContentText("Black has Won the Game!");
                } else {
                    alert.setContentText("White has Won the Game!");
                }
                alert.showAndWait();
                gameAlive.setValue(false);
            }
        }
        addMoveToHistory(oldX, oldY, newX, newY);
        typeOfMove = null;
        checkForDraw();
        return true;
    }

    /**
     * Changes the turn, and flips the board so the other player can move.
     */
    @Override
    public void changeTurn() {
        super.changeTurn();
        if (!getTurn()) {
            this.setRotate(180);
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    if (board[x][y].hasPiece()) {
                        board[x][y].getPiece().setRotate(180);
                    }
                }
            }
        } else {
            this.setRotate(0);
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    if (board[x][y].hasPiece()) {
                        board[x][y].getPiece().setRotate(0);
                    }
                }
            }
        }
        setTeam(!getTeam());
    }
}
