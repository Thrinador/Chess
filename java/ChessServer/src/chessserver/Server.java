package chessserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * Central Server for the Chess games to run off of. Stores the members, hosts,
 * and players in three different arrayLists. When a server request comes in the
 * Server gives it a thread and the ServerConnection deals with it from there.
 *
 * @author Ben Clark
 */
public class Server {

    ServerSocket serverS;
    ArrayList<Member> members;
    ArrayList<Player> hosts;
    ArrayList<Player> players;

    /**
     * Default constructor that sets up all three arrayLists and sets up the
     * server socket.
     */
    public Server() {
        members = new ArrayList<>();
        hosts = new ArrayList<>();
        players = new ArrayList<>();

        try {
            serverS = new ServerSocket(7654);
        } catch (IOException ex) {
            System.out.println("ERROR setting up server on port.");
            System.exit(1);
        }
    }

    /**
     * Main function of the server. Waits for an incoming connection, then when
     * one comes in gives it a server connection to handle the exchange. Finally
     * goes back to waiting.
     */
    public void run() {
        while (true) {
            Thread thread;
            try {
                thread = new Thread(new ServerConnection(serverS.accept(),
                        members, hosts, players));
                thread.start();
            } catch (IOException ex) {
                System.out.println("ERROR accepting connection.");
            }
        }
    }

    /**
     * Main for the Server. Sets up a new Server object, then runs the server
     *
     * @param args Not Used
     */
    public static void main(String[] args) {
        Server s = new Server();
        s.run();
    }
}
