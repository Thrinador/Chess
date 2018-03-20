package game;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Window;
import chess.MultiplayerChess;
import java.util.concurrent.CountDownLatch;

/**
 * MultiplayerGame extends Game, by changing some features to fit a multiplayer
 * environment. After successful moves the game will send messages to the
 * opponent about what move they made.
 *
 * @author Ben Clark
 */
public class MultiplayerGame extends Game {

    private final MultiplayerChess connection;
    private final String opponentUsername;
    private CharacterHolder pawnPromotion;

    /**
     * Constructor that takes in the Chess object we are operating on, the team
     * we are playing and the username of the opponent.
     *
     * @param mul The Chess object we are sending messages through.
     * @param team The team we are playing
     * @param opponentUsername The username of the opponent we are playing
     * against.
     */
    public MultiplayerGame(MultiplayerChess mul, boolean team, String opponentUsername) {
        super();
        connection = mul;
        setTeam(team);
        this.opponentUsername = opponentUsername;
        pawnPromotion = new CharacterHolder();

        //If we are playing black rotate the board
        if (!team) {
            this.setRotate(180);
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    if (board[x][y].hasPiece()) {
                        board[x][y].getPiece().setRotate(180);
                    }
                }
            }
        }
    }

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
        if (checkMove(oldX, oldY, newX, newY, pp, team)) {
            String message = networking.Messages.move(oldX, oldY, newX, newY, pawnPromotion.getValue());
            connection.sendOnSocket(message);
            return true;
        }
        return false;
    }

    /**
     * Checks to see if a given move is valid taking in parameters of what piece
     * is trying to be moved and what team the player is playing. Returns
     * whether or not it was a valid move.
     *
     * @param oldX Starting x position for the piece that is trying to be moved
     * @param oldY Starting y position for the piece that is trying to be moved
     * @param newX Ending x position for the piece that is trying to be moved
     * @param newY Ending y position for the piece that is trying to be moved
     * @param team Team of the player that is moving.
     * @return Whether or not that was a valid move.
     */
    public boolean checkMove(int oldX, int oldY, int newX, int newY, char pp, boolean team) {
        pawnPromotion.setValue(pp);
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

            Platform.runLater(() -> {
                pieceGroup.getChildren().remove(killedPiece);
            });

            typeOfMove = MoveType.CAPTURE;
        }
        board[newX][newY].setPiece(piece);
        board[oldX][oldY].setPiece(null);

        switch (piece.getType()) {
            case PAWN:
                resetDrawConditions();
                if (newY == 0 || newY == 7) {
                    runAndWait(() -> this.pawnPromotion(newX, newY, pawnPromotion));
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
                Platform.runLater(() -> this.endGame(oldX, oldY, newX, newY));
            }
        }
        addMoveToHistory(oldX, oldY, newX, newY);
        typeOfMove = null;
        checkForDraw();
        return true;
    }

    /**
     * Runs the specified {@link Runnable} on the JavaFX application thread and
     * waits for completion.
     *
     * @param action the {@link Runnable} to run
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public static void runAndWait(Runnable action) {
        if (action == null) {
            throw new NullPointerException("action");
        }

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        // queue on JavaFX thread and wait for completion
        final CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                doneLatch.countDown();
            }
        });

        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            // ignore exception
        }
    }

    private void endGame(int oldX, int oldY, int newX, int newY) {
        System.out.println("We are ending the game");

        connection.endGame(getTurn() != getTeam());
        addMoveToHistory(oldX, oldY, newX, newY);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        Window window = this.getScene().getWindow();
        alert.initOwner(window);
        alert.setHeaderText(null);
        if (getTurn() != getTeam()) {
            alert.setContentText("You have Won the Game!");
        } else {
            alert.setContentText(opponentUsername + " has Won the Game!");
        }
        alert.showAndWait();
        gameAlive.setValue(false);

    }

    /**
     * If the opponent leaves the game early this will pop-up a message to let
     * you know that the game is now over and you have won.
     */
    public void leftGame() {
        System.out.println("The opponent has left the game");

        connection.endGame(true);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        Window window = this.getScene().getWindow();
        alert.initOwner(window);
        alert.setHeaderText(null);
        alert.setContentText(opponentUsername + " has left the game, you win!");

        alert.showAndWait();
        gameAlive.setValue(false);
    }
}
