package chess;

import game.SingleplayerGame;
import javafx.scene.layout.BorderPane;

/**
 * SingleplayerChess represents a singleplayer chess board
 *
 * @author Ben Clark
 */
public class SingleplayerChess extends BorderPane {

    private final SingleplayerGame game;

    /**
     * Default constructor that initializes a singleplayer game and sets it to
     * the center
     */
    public SingleplayerChess() {
        this.game = new SingleplayerGame();
        this.setCenter(game);
    }
}
