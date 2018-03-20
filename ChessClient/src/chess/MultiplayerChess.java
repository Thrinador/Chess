package chess;

import game.MultiplayerGame;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import networking.Chat;
import networking.Listen;
import networking.Messages;
import networking.Player;
import networking.Score;

/**
 * MultiplayerChess represents the BorderPane that contains the Score, Chat, and
 * Game objects. MultiplayersChess' job is to act as a manager for these
 * different components during the game.
 *
 * @author Ben Clark
 */
public class MultiplayerChess extends BorderPane {

    private final Socket sock;
    private final DataOutputStream clientOutput;
    private final MultiplayerGame game;
    private final Chat chat;
    private final Score scoreInfo;
    private final Player user;

    /**
     * Constructor that takes in the two players that are playing, and a socket
     * that connects them. Using this it makes a chat object, score object and
     * game object, then starts them accordingly.
     *
     * @param sock The socket connection between the two players.
     * @param user The player on this side of the connection.
     * @param opponent The player that you are playing against.
     * @throws IOException If there is an issue with the socket.
     */
    public MultiplayerChess(Socket sock, Player user, Player opponent)
            throws IOException {
        this.user = user;
        this.sock = sock;

        this.chat = new Chat(sock, user);
        this.game = new MultiplayerGame(this, user.getColor(),
                opponent.getUsername());
        this.scoreInfo = new Score(user, opponent);
        this.clientOutput = new DataOutputStream(sock.getOutputStream());

        AnchorPane ap = new AnchorPane();
        AnchorPane.setTopAnchor(game, 0.0);
        ap.getChildren().add(game);

        this.setTop(scoreInfo);
        this.setRight(chat);
        this.setCenter(ap);

        this.setPrefSize(game.getPrefWidth() + chat.getPrefWidth(),
                game.getPrefHeight() + scoreInfo.getPrefHeight());
        this.setVisible(true);
        Listen listen = new Listen(game, chat, sock);
        Thread thread = new Thread(listen);
        thread.start();
    }

    /**
     * sendOnSocket takes in a message to send and sends it to the opponent
     * using the socket.
     *
     * @param message The message to send to the opponent.
     */
    public void sendOnSocket(String message) {
        try {
            clientOutput.writeBytes(message);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Takes in a boolean whether you won the game or not then sets the game
     * value to not running, and sends a message to the server that we are
     * ending the game.
     *
     * @param wonLost whether or not you won the game.
     */
    public void endGame(boolean wonLost) {
        game.gameAlive.setValue(false);
        Messages.exitGame(user.getUsername(), wonLost);

        try {
            clientOutput.close();
            sock.close();
        } catch (IOException ex) {
            System.out.println("Error closing multiplayer socket");
        }
    }

    /**
     * Returns whether or not the game is currently still going.
     *
     * @return whether or not the game is over.
     */
    public boolean isAlive() {
        return game.gameAlive.getValue();
    }

}
