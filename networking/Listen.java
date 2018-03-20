package networking;

import game.MultiplayerGame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import javafx.application.Platform;

/**
 * Listen is a thread that listens for incoming messages from the opponent. When
 * a message comes in the listen object handles it and calls the appropriate
 * methods to operate on it.
 *
 * @author Ben Clark
 */
public class Listen implements Runnable {

    private final MultiplayerGame game;
    private final Chat chat;
    private final BufferedReader clientInput;

    /**
     * Constructor that takes in the game that we are operating on, the chat we
     * are operating on, and the socket that we are communicating with.
     *
     * @param game The game we are operating on.
     * @param chat The chat we are working with.
     * @param sock The socket we are communicating with.
     * @throws IOException If there is an issue with the socket.
     */
    public Listen(MultiplayerGame game, Chat chat, Socket sock)
            throws IOException {
        this.game = game;
        this.chat = chat;
        this.clientInput = new BufferedReader(
                new InputStreamReader(sock.getInputStream()));
    }

    /**
     * Main function of the listen object, listens for incoming message from the
     * opponent, when a message comes in the the function parses it, and then
     * sends it to the appropriate operation.
     */
    @Override
    public void run() {
        try {
            while (this.game.gameAlive.get()) {
                if (clientInput.ready()) {
                    String command = clientInput.readLine();
                    if (command.equals("CHAT")) {
                        chatMessage();
                    } else if (command.equals("MOVE")) {
                        move();
                    } else if (command.equals("GAMEOVER")) {
                        gameOver();
                        break;
                    }
                }
            }
            clientInput.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void move() throws IOException {
        String line = clientInput.readLine();
        clientInput.readLine();

        int x1 = line.charAt(0) - '0';
        int y1 = line.charAt(3) - '0';
        int x2 = line.charAt(6) - '0';
        int y2 = line.charAt(9) - '0';
        char pp = line.charAt(12);
        game.checkMove(x1, y1, x2, y2, pp, !game.getTeam());
    }

    private void chatMessage() throws IOException {
        String username = clientInput.readLine();
        String line = clientInput.readLine();
        clientInput.readLine();
        chat.addText(line, username);
    }

    private void gameOver() throws IOException {
        clientInput.readLine();
        Platform.runLater(() -> game.leftGame());
    }

}
