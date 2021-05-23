package chessserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 * ServerConnection deals with a single exchange between server and client. The
 * Client will send a single message to the server connection. The
 * ServerConnection will then parse that message and send the appropriate
 * response.
 *
 * @author Ben Clark
 */
public class ServerConnection implements Runnable {

    Socket connectionSock;
    ArrayList<Member> members;
    ArrayList<Player> hosts;
    ArrayList<Player> players;
    BufferedReader clientInput;
    DataOutputStream clientOutput;

    /**
     * Constructor that takes the three different Lists from the Server and
     * stores them, it does not make a copy. It also takes a Socket connection
     * which is how it communicates with the client.
     *
     * @param connectionSock Socket connection with the client.
     * @param members The list of current members
     * @param hosts The list of current hosts
     * @param players The list of current players
     * @throws IOException If there was an issue with the socket
     */
    public ServerConnection(Socket connectionSock, ArrayList<Member> members,
            ArrayList<Player> hosts, ArrayList<Player> players)
            throws IOException {
        this.connectionSock = connectionSock;
        this.members = members;
        this.hosts = hosts;
        this.players = players;

        this.clientInput = new BufferedReader(
                new InputStreamReader(connectionSock.getInputStream()));

        this.clientOutput = new DataOutputStream(
                connectionSock.getOutputStream());
    }

    /**
     * run is the main function of ServerConnection. It parses a command from
     * the client and then sends an appropriate response.
     */
    @Override
    public void run() {
        try {
            System.out.println("Connection made");
            String clientText = clientInput.readLine();
            switch (clientText) {
                case "REGISTER":
                    register();
                    break;
                case "UNREGISTER":
                    unregister();
                    break;
                case "LIST":
                    list();
                    break;
                case "JOIN":
                    join();
                    break;
                case "CREATE":
                    create();
                    break;
                case "EXIT":
                    exit();
                    break;
                default:
                    System.out.println("Client request not recognized");
                    sendMalformedRequestError();
                    break;
            }

            clientOutput.close();
            clientInput.close();
            connectionSock.close();

            System.out.println("Connection closed\n");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void register() throws IOException {
        System.out.println("Client attempting to register.");
        String username = clientInput.readLine();
        String endMessage = clientInput.readLine();

        if (checkMalformedRequest(endMessage)) {
            return;
        }

        if (findMember(username) != -1 || findHost(username) != -1
                || findPlayer(username) != -1) {
            sendUsernameNotUniqueError();
            return;
        }

        Member mem = new Member(connectionSock.getInetAddress(), username);
        members.add(mem);

        clientOutput.writeBytes("REGISTERRESPONSE\n");
        clientOutput.writeBytes(username + "\n");
        clientOutput.writeBytes("END" + "\n");
    }

    private void unregister() throws IOException {
        System.out.println("Client attempting to unregister.");
        String username = clientInput.readLine();
        String endMessage = clientInput.readLine();

        int clientIndex = findMember(username);
        if (checkMalformedRequest(endMessage)
                || checkUsernameNotFound(clientIndex)) {
            return;
        }

        members.remove(clientIndex);
        clientOutput.writeBytes("UNREGISTERRESPONSE\n");
        clientOutput.writeBytes(username + "\n");
        clientOutput.writeBytes("END" + "\n");
    }

    private void list() throws IOException {
        System.out.println("Client attempting to get the lsit of games.");
        String username = clientInput.readLine();
        String colorChoice = clientInput.readLine();
        String endMessage = clientInput.readLine();

        //Error check the message
        int clientIndex = findMember(username);
        if (checkMalformedRequest(endMessage)
                || checkUsernameNotFound(clientIndex)) {
            return;
        }

        //Ilegal color choice entered.
        if (!(colorChoice.equals("white") || colorChoice.equals("black")
                || colorChoice.equals("either"))) {
            sendBadColorChoice();
            return;
        }

        Member m = members.get(clientIndex);

        if (colorChoice.equals("either")) {
            //send all the hosts back to client
            clientOutput.writeBytes("LISTRESPONSE\n");
            clientOutput.writeBytes(m.getGamesWon() + "\n");
            clientOutput.writeBytes(hosts.size() + "\n");
            for (Player h : hosts) {
                clientOutput.writeBytes(h.getUsername() + "\n");
                clientOutput.writeBytes(h.getGameName() + "\n");
                clientOutput.writeBytes(h.getIPAddress() + "\n");
                clientOutput.writeBytes(h.getGameColor() + "\n");
                clientOutput.writeBytes(h.getGamesWon() + "\n");
            }
            clientOutput.writeBytes("END\n");
            return;
        }

        //Get all the members
        ArrayList<Player> membersToSend = new ArrayList<>();
        hosts.stream().filter((Player h)
                -> (!h.getGameColor().equals(colorChoice))).forEachOrdered((h) -> {
            membersToSend.add(h);
        });

        //send all the hosts with correct color back to client
        clientOutput.writeBytes("LISTRESPONSE\n");
        clientOutput.writeBytes(m.getGamesWon() + "\n");
        clientOutput.writeBytes(membersToSend.size() + "\n");
        for (Player h : membersToSend) {
            clientOutput.writeBytes(h.getGameName() + "\n");
            clientOutput.writeBytes(h.getIPAddress() + "\n");
            clientOutput.writeBytes(h.getGameColor() + "\n");
            clientOutput.writeBytes(h.getGamesWon() + "\n");
        }
        clientOutput.writeBytes("END\n");
    }

    private void join() throws IOException {
        System.out.println("Client attempting to join a game.");
        String username = clientInput.readLine();
        String gamename = clientInput.readLine();
        String colorChoice = clientInput.readLine();
        String hostMember = clientInput.readLine();
        String endMessage = clientInput.readLine();

        //Error check the message
        if (checkHostMember(hostMember)) {
            return;
        }

        boolean host = hostMember.equals("host");
        int index = host ? findHost(username) : findMember(username);
        if (checkMalformedRequest(endMessage)
                || checkUsernameNotFound(index)) {
            return;
        }

        //Ilegal color choice entered.
        if (!(colorChoice.equals("white") || colorChoice.equals("black"))) {
            sendBadColorChoice();
            return;
        }

        //change from member to player.
        Member m = host ? hosts.remove(index) : members.remove(index);
        Player newPlayer = new Player(m, gamename, colorChoice);
        players.add(newPlayer);

        clientOutput.writeBytes("JOINRESPONSE\n");
        clientOutput.writeBytes(gamename + "\n");
        clientOutput.writeBytes("END\n");
    }

    private void create() throws IOException {
        System.out.println("Client attempting to create a game.");
        String username = clientInput.readLine();
        String gameName = clientInput.readLine();
        String colorChoice = clientInput.readLine();
        String endMessage = clientInput.readLine();

        int clientIndex = findMember(username);
        if (checkMalformedRequest(endMessage)
                || checkUsernameNotFound(clientIndex)) {
            return;
        }

        if (!(colorChoice.equals("white") || colorChoice.equals("black"))) {
            sendBadColorChoice();
            return;
        }

        int gameNameIndex = findGameName(gameName);
        if (gameNameIndex != -1) {
            sendGamenameNotUniqueError();
            return;
        }

        Member m = members.remove(clientIndex);
        hosts.add(new Player(m, gameName, colorChoice));

        clientOutput.writeBytes("CREATERESPONSE\n");
        clientOutput.writeBytes(gameName + "\n");
        clientOutput.writeBytes(m.getGamesWon() + "\n");
        clientOutput.writeBytes("END\n");
    }

    private void exit() throws IOException {
        System.out.println("Client has finished their game");

        String username = clientInput.readLine();
        String wonLost = clientInput.readLine();
        String endMessage = clientInput.readLine();

        int clientIndex = findPlayer(username);
        if (checkMalformedRequest(endMessage)
                || checkUsernameNotFound(clientIndex)) {
            return;
        }

        if (!(wonLost.equals("won") || wonLost.equals("lost"))) {
            return;
        }

        //change from member to player.
        Player p = players.remove(clientIndex);
        if (wonLost.equals("won")) {
            p.wonGame();
        }

        members.add(new Member(p));

        clientOutput.writeBytes("EXITRESPONSE\n");
        clientOutput.writeBytes(username + "\n");
        clientOutput.writeBytes("END\n");
    }

    private int findMember(String username) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getUsername().equals(username)) {
                return i;
            }
        }
        return -1;
    }

    private int findPlayer(String username) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUsername().equals(username)) {
                return i;
            }
        }
        return -1;
    }

    private int findHost(String username) {
        for (int i = 0; i < hosts.size(); i++) {
            if (hosts.get(i).getUsername().equals(username)) {
                return i;
            }
        }
        return -1;
    }

    private int findGameName(String gamename) {
        for (int i = 0; i < hosts.size(); i++) {
            if (hosts.get(i).getGameName().equals(gamename)) {
                return i;
            }
        }
        return -1;
    }

    private void sendUsernameNotFoundError() throws IOException {
        sendError("usernameNotFound");
    }

    private void sendMalformedRequestError() throws IOException {
        sendError("malformedRequest");
    }

    private void sendUsernameNotUniqueError() throws IOException {
        sendError("usernameNotUnique");
    }

    private void sendBadColorChoice() throws IOException {
        sendError("badColorChoice");
    }

    private void sendGamenameNotUniqueError() throws IOException {
        sendError("gamenameNotUnique");
    }

    private void sendError(String errorType) throws IOException {
        clientOutput.writeBytes("ERROR\n");
        clientOutput.writeBytes(errorType + "\n");
        clientOutput.writeBytes("END\n");
    }

    private boolean checkMalformedRequest(String endMes) throws IOException {
        if (!endMes.equals("END")) {
            sendMalformedRequestError();
            return true;
        }
        return false;
    }

    private boolean checkUsernameNotFound(int clientIndex) throws IOException {
        //Find and error check the sent username.
        if (clientIndex == -1) {
            sendUsernameNotFoundError();
            return true;
        }
        return false;
    }

    private boolean checkHostMember(String hostMember) throws IOException {
        if (hostMember.equals("host") || hostMember.equals("member")) {
            return false;
        }
        sendMalformedRequestError();
        return true;
    }
}
