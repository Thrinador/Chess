package networking;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Ben Clark
 */
public class Messages {

    private static String getStringColor(boolean color) {
        return color ? "white" : "black";
    }

    private Messages() {

    }

    /**
     * Attempts to send a message to the given server, returns the string
     * response from the server.
     *
     * @param ip The Ip we are communicating to
     * @param port The port we are sending to
     * @param message The message we want to send
     * @return The response that server gives
     * @throws IOException If there was an issue setting up the socket.
     */
    public static Scanner sendMessage(String ip, int port, String message)
            throws IOException {
        Socket s = new Socket(ip, port);

        BufferedReader clientInput = new BufferedReader(
                new InputStreamReader(s.getInputStream()));

        DataOutputStream clientOutput = new DataOutputStream(
                s.getOutputStream());
        clientOutput.writeBytes(message);

        return new Scanner(clientInput);
    }

    /**
     * Build and return a register message based off the username provided.
     *
     * @param username Username to register
     * @return The built register message
     */
    public static String register(String username) {
        StringBuilder clientOutput = new StringBuilder();
        clientOutput.append("REGISTER\n");
        clientOutput.append(username).append("\n");
        clientOutput.append("END\n");
        return clientOutput.toString();
    }

    /**
     * Build and return a unregister message based off the username provided.
     *
     * @param username Username to unregister
     * @return The built unregister message
     */
    public static String unregister(String username) {
        StringBuilder clientOutput = new StringBuilder();
        clientOutput.append("UNREGISTER\n");
        clientOutput.append(username).append("\n");
        clientOutput.append("END\n");
        return clientOutput.toString();
    }

    /**
     * Build and return a list message based off the username and color
     * provided.
     *
     * @param username Username to register
     * @param color Color to play
     * @return The built register message
     */
    public static String list(String username, String color) {
        StringBuilder clientOutput = new StringBuilder();
        clientOutput.append("LIST\n");
        clientOutput.append(username).append("\n");
        clientOutput.append(color).append("\n");
        clientOutput.append("END\n");
        return clientOutput.toString();
    }

    /**
     * Build and return a create message based off the username, gamename, and
     * color provided.
     *
     * @param username Username to host the game
     * @param gamename Name of the game that you want to create.
     * @param color The color of pieces you want to play.
     * @return The built create message.
     */
    public static String create(String username, String gamename, boolean color) {
        StringBuilder clientOutput = new StringBuilder();
        clientOutput.append("CREATE\n");
        clientOutput.append(username).append("\n");
        clientOutput.append(gamename).append("\n");
        clientOutput.append(getStringColor(color)).append("\n");
        clientOutput.append("END\n");
        return clientOutput.toString();
    }

    /**
     * Build and return a join message based off of the username, gamename,
     * color, and hostmember provided.
     *
     * @param username Username that is joining the game.
     * @param gamename Name of the game that you want to join.
     * @param color color that you are going to play.
     * @param hostmember Name of the host that you are going to join
     * @return The built join message.
     */
    public static String join(String username, String gamename,
            boolean color, String hostmember) {
        StringBuilder clientOutput = new StringBuilder();
        clientOutput.append("JOIN\n");
        clientOutput.append(username).append("\n");
        clientOutput.append(gamename).append("\n");
        clientOutput.append(getStringColor(color)).append("\n");
        clientOutput.append(hostmember).append("\n");
        clientOutput.append("END\n");
        return clientOutput.toString();
    }

    /**
     * Build and return a exit game message based off the username, and whether
     * or not we won the game.
     *
     * @param username The username that is exiting.
     * @param wonLost Whether or not you won the game.
     * @return The built exit message.
     */
    public static String exitGame(String username, boolean wonLost) {
        StringBuilder clientOutput = new StringBuilder();
        clientOutput.append("EXIT\n");
        clientOutput.append(username).append("\n");
        clientOutput.append(wonLost ? "won" : "lost").append("\n");
        clientOutput.append("END\n");
        return clientOutput.toString();
    }

    /**
     * Build and return a prompt message built from the username and gamesWon
     * provided.
     *
     * @param username The username that is prompting
     * @param gamesWon The number of games that you have won.
     * @return The built prompt message.
     */
    public static String prompt(String username, int gamesWon) {
        StringBuilder clientOutput = new StringBuilder();
        clientOutput.append("PROMPT\n");
        clientOutput.append(username).append("\n");
        clientOutput.append(gamesWon).append("\n");
        clientOutput.append("END\n");
        return clientOutput.toString();
    }

    /**
     * Build and return a chat message built from the message that was provided.
     *
     * @param message The message that you want to send
     * @return The built chat message.
     */
    public static String chat(String message) {
        StringBuilder clientOutput = new StringBuilder();
        clientOutput.append("CHAT\n");
        clientOutput.append(message).append("\n");
        clientOutput.append("END\n");
        return clientOutput.toString();
    }

    /**
     * Build and return a move message built from the start and end position
     * provided.
     *
     * @param x1 The starting x coordinate for the piece
     * @param y1 The starting y coordinate for the piece
     * @param x2 The ending x coordinate for the piece
     * @param y2 The ending y coordinate for the piece
     * @param pp The piece that a pawn wants to promote to
     * @return The build move message.
     */
    public static String move(int x1, int y1, int x2, int y2, char pp) {
        StringBuilder clientOutput = new StringBuilder();
        clientOutput.append("MOVE\n");
        clientOutput.append(x1).append(", ").append(y1).append(", ");
        clientOutput.append(x2).append(", ").append(y2).append(", ");
        clientOutput.append(pp).append("\n");
        clientOutput.append("END\n");
        return clientOutput.toString();
    }

    /**
     * Build and return a gameOver message.
     *
     * @return The built game over message
     */
    public static String gameOver() {
        StringBuilder clientOutput = new StringBuilder();
        clientOutput.append("GAMEOVER\n");
        clientOutput.append("END\n");
        return clientOutput.toString();
    }

    /**
     * Given a scanner prints out the message that was sent.
     *
     * @param response Where we are pulling the message from.
     */
    public static void printResponse(Scanner response) {
        String line = "";
        while (!line.equals("END")) {
            line = response.nextLine();
            System.out.println(line);
        }
    }

    /**
     * Build and return a re-register message based off of the username provided.
     * @param username The username that you are re-registering.
     * @return The built reregister message.
     */
    public static String reregister(String username) {
        StringBuilder clientOutput = new StringBuilder();
        clientOutput.append("REREGISTER\n");
        clientOutput.append(username).append("\n");
        clientOutput.append("END\n");
        return clientOutput.toString();
    }
}
