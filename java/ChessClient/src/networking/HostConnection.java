package networking;

import chess.NewGame;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import javafx.application.Platform;

/**
 * HostConnection represents the thread that runs in the background when you
 * have successfully set up a host game.
 *
 * @author Ben Clark
 */
public class HostConnection implements Runnable {

    private final NewGame newGame;
    private boolean acceptGame;
    private ServerSocket serverSock;
    private Socket connection;
    private final Player player;

    /**
     * Constructor that takes in the player that is hosting the game and the new
     * game object that is creating the host game.
     *
     * @param player The player who is hosting the game
     * @param newGame The object making a new host game.
     */
    public HostConnection(Player player, NewGame newGame) {
        this.player = player;
        this.newGame = newGame;
    }

    /**
     * Main run function for the host connection. Waits for someone to attempt
     * to join the game. When someone wants to join it prompts the Player and
     * connects with them if the host accepts the request.
     */
    @Override
    public void run() {
        try {
            serverSock = new ServerSocket(ServerInfo.GAME_PORT);
            connection = serverSock.accept();
            while (!serverSock.isClosed()) {
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                DataOutputStream output = new DataOutputStream(
                        connection.getOutputStream());

                input.readLine();
                String name = input.readLine();
                int wins = Integer.parseInt(input.readLine());
                User user = new User(name, wins);
                input.readLine();

                Platform.runLater(() -> {
                    try {
                        acceptGame = newGame.hostPlayerAccept(name);
                        output.writeBytes("PROMPTRESPONSE\n");
                        if (acceptGame) {
                            output.writeBytes("yes\n");
                            newGame.acceptHostGame(connection, player, user);
                        } else {
                            output.writeBytes("no\n");
                        }
                        output.writeBytes("END\n");
                    } catch (IOException ex) {

                    }
                });
                connection = serverSock.accept();
            }

            serverSock.close();

        } catch (IOException ex) {
            System.out.println("IOException happened in HostConnection");
            System.out.println(ex.getMessage());
        }
    }
}
