package chess;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import networking.Messages;
import networking.ServerInfo;
import networking.User;
import networking.Player;
import networking.HostConnection;

/**
 * Used as a factory for building a new Game game. That can be single player or
 * multiplayer. If it is multiplayer it deals with hooking up the two players.
 *
 * @author Ben Clark
 */
public class NewGame extends Pane {

    private final Stage primaryStage;
    private final VBox vbox;
    private final User user;

    /**
     * Constructor that takes in the Stage we are operating on, the VBox where
     * the board will be placed and the main user of the game.
     *
     * @param primaryStage The Stage we are operating on
     * @param vbox The VBox that will store the board.
     * @param user The user of the game.
     */
    public NewGame(Stage primaryStage, VBox vbox, User user) {
        this.primaryStage = primaryStage;
        this.vbox = vbox;
        this.user = user;
    }

    /**
     * Makes a new game for the chess program. Prompts the user with alerts to
     * make decisions about what game they want to play.
     */
    public void makeGame() {
        BorderPane game;

        try {
            ButtonType single = new ButtonType("SinglePlayer");
            ButtonType multi = new ButtonType("Multiplayer");

            Alert gameType = new Alert(Alert.AlertType.CONFIRMATION);
            gameType.setTitle("Type of Game");
            gameType.setGraphic(null);
            gameType.setHeaderText(null);
            gameType.setContentText("What type of game do you want to play?");
            gameType.getButtonTypes().setAll(single, multi, ButtonType.CLOSE);
            gameType.initOwner(primaryStage);
            Optional<ButtonType> result = gameType.showAndWait();

            if (result.get() == single) {
                game = new SingleplayerChess();
            } else if (result.get() == multi) {
                game = newMultiplayerGame();
                if (game == null) {
                    return;
                }
            } else {
                return;
            }
        } catch (IOException ex) {
            System.out.println("IOException happened in NewGame");
            System.out.println(ex.getMessage());
            game = new SingleplayerChess();
        }

        if (vbox.getChildren().size() > 1) {
            vbox.getChildren().remove(1);
        }
        vbox.getChildren().add(game);
        primaryStage.sizeToScene();
    }

    /**
     * Given a username prompts the host of the game if they want to play with
     * the person.
     *
     * @param name username of the person who wants to play.
     * @return Whether or not you want to play with them.
     */
    public boolean hostPlayerAccept(String name) {
        Alert gameRequest = new Alert(Alert.AlertType.CONFIRMATION);
        setAlertSettings(gameRequest, "Join Request", "Do you want " + name
                + " to join your game?");
        Optional<ButtonType> result = gameRequest.showAndWait();

        return result.get() == ButtonType.OK;
    }

    /**
     * When you accept a player to join your game, the current singleplayer game
     * must be removed and swapped out with the multiplayer game. The parameters
     * passed are the one necessary for making the multiplayer game.
     *
     * @param s Socket connection to the player that you are playing with.
     * @param you
     * @param oppo
     */
    public void acceptHostGame(Socket s, Player you, User oppo) {
        try {
            Scanner response = Messages.sendMessage(ServerInfo.SERVER_IP,
                    ServerInfo.SERVER_PORT,
                    Messages.join(user.getUsername(), you.getGameName(),
                            you.getColor(), "host"));

            Messages.printResponse(response);
            Player opponent = new Player(oppo, you.getGameName(),
                    s.getInetAddress().getHostAddress(), !you.getColor());

            MultiplayerChess game = new MultiplayerChess(s, you, opponent);

            if (vbox.getChildren().size() > 1) {
                vbox.getChildren().remove(1);
            }
            vbox.getChildren().add(game);
            primaryStage.sizeToScene();
        } catch (IOException ex) {
            System.out.println("IOException happened in acceptHostGame");
            System.out.println(ex.getMessage());
        }
    }

    private BorderPane newMultiplayerGame() throws IOException {
        if (!user.hasUsername()) {
            String name = getUsername();
            if (name == null) {
                return null;
            }

            String message = Messages.register(name);
            Scanner response = Messages.sendMessage(ServerInfo.SERVER_IP,
                    ServerInfo.SERVER_PORT, message);

            if (errorHandleRegisterUsername(response)) {
                displayErrorMessage(response);
                return null;
            }

            user.setUsername(name);
        }

        //HostOrJoin
        Boolean hostJoin = hostOrJoin();
        if (hostJoin == null) {
            return null;
        } else if (hostJoin) {
            return newHostGame();
        } else {
            return newJoinGame();
        }
    }

    private BorderPane newHostGame() throws IOException {
        String gameName = getGameName();
        if (gameName == null) {
            return null;
        }

        Boolean colorChoice = getColorChoiceHost();
        if (colorChoice == null) {
            return null;
        }

        //Build and send create message
        Scanner response = Messages.sendMessage(ServerInfo.SERVER_IP,
                ServerInfo.SERVER_PORT,
                Messages.create(user.getUsername(), gameName, colorChoice));

        String createResponse = response.nextLine();
        String gameNameResponse = response.nextLine();
        user.setWins(response.nextInt());
        response.nextLine();
        String end = response.nextLine();

        if (createResponse.equals("CREATERESPONSE")
                && gameNameResponse.equals(gameName) && end.equals("END")) {
            Player host = new Player(user, gameName, "localhost", colorChoice);
            Thread thread = new Thread(new HostConnection(host, this));
            thread.start();
            hostGameSetup();
            return new SingleplayerChess();
        }
        return null;
    }

    private MultiplayerChess newJoinGame() throws IOException {
        String colorChoice = getColorChoiceJoin();
        if (colorChoice == null) {
            return null;
        }

        //Send a List message to the server and build a host list
        Scanner response = Messages.sendMessage(ServerInfo.SERVER_IP,
                ServerInfo.SERVER_PORT,
                Messages.list(user.getUsername(), colorChoice));
        ArrayList<Player> hosts = buildHosts(response);

        if (hosts.isEmpty()) {
            noHosts();
            return null;
        }

        //Find a valid host
        Player h = getHostChoice(hosts);
        if (h == null) {
            return null;
        }

        Socket s = new Socket(h.getIp(), ServerInfo.GAME_PORT);
        BufferedReader clientInput = new BufferedReader(
                new InputStreamReader(s.getInputStream()));
        DataOutputStream clientOutput = new DataOutputStream(
                s.getOutputStream());
        clientOutput.writeBytes(Messages.prompt(user.getUsername(),
                user.getWins()));

        //get hosts response
        clientInput.readLine();
        String yesNo = clientInput.readLine();
        clientInput.readLine();

        //Does the host want to play with you?
        if (yesNo.equals("yes")) {
            String joinMessage = Messages.join(user.getUsername(),
                    h.getGameName(), h.getColor(), "member");
            Messages.sendMessage(ServerInfo.SERVER_IP,
                    ServerInfo.SERVER_PORT, joinMessage);

            Player you = new Player(user, h.getGameName(),
                    "localhost", h.getColor());

            return new MultiplayerChess(s, you, h);
        } else {
            rejectedRequest();
            return null;
        }
    }

    private String getUsername() {
        TextInputDialog dialog = new TextInputDialog();
        setAlertSettings(dialog, "Username", "Please enter a username:");
        Optional<String> message = dialog.showAndWait();
        if (message.isPresent()) {
            String username = message.get();
            if (username.length() <= 15 && username.length() >= 1) {
                return username;
            }
        }
        return null;
    }

    private Boolean hostOrJoin() {
        ButtonType host = new ButtonType("Host");
        ButtonType join = new ButtonType("Join");

        Alert hostJoin = new Alert(Alert.AlertType.CONFIRMATION);
        setAlertSettings(hostJoin, "Host or join",
                "Do you want to host the game or join a game?");
        hostJoin.getButtonTypes().setAll(host, join, ButtonType.CLOSE);

        Optional<ButtonType> result = hostJoin.showAndWait();
        if (result.get() == host) {
            return true;
        } else if (result.get() == join) {
            return false;
        } else {
            return null;
        }
    }

    private String getGameName() {
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            setAlertSettings(dialog, "Game name", "Please enter a game name:");
            Optional<String> message = dialog.showAndWait();
            if (message.isPresent()) {
                String username = message.get();
                if (username.length() <= 15 && username.length() >= 1) {
                    return username;
                }
            } else {
                return null;
            }
        }
    }

    private Boolean getColorChoiceHost() {
        ButtonType white = new ButtonType("White");
        ButtonType black = new ButtonType("Black");

        Alert colorChoice = new Alert(Alert.AlertType.CONFIRMATION);
        setAlertSettings(colorChoice, "Color Choice",
                "What color of pieces do you want to play?");

        colorChoice.getButtonTypes().setAll(white, black, ButtonType.CLOSE);
        Optional<ButtonType> result = colorChoice.showAndWait();

        if (result.get() == white) {
            return true;
        } else if (result.get() == black) {
            return false;
        } else {
            return null;
        }
    }

    private String getColorChoiceJoin() {
        ButtonType white = new ButtonType("White");
        ButtonType black = new ButtonType("Black");
        ButtonType either = new ButtonType("Either");

        Alert colorChoice = new Alert(Alert.AlertType.CONFIRMATION);
        setAlertSettings(colorChoice, "Color Choice",
                "What color of pieces do you want to play?");
        colorChoice.getButtonTypes().setAll(
                white, black, either, ButtonType.CLOSE);

        Optional<ButtonType> result = colorChoice.showAndWait();
        if (result.get() == white) {
            return "white";
        } else if (result.get() == black) {
            return "black";
        } else if (result.get() == either) {
            return "either";
        } else {
            return null;
        }
    }

    private ArrayList<Player> buildHosts(Scanner response) {
        ArrayList<Player> hosts = new ArrayList<>();

        response.nextLine();
        user.setWins(response.nextInt());
        response.nextLine();
        int size = response.nextInt();
        response.nextLine();
        for (int i = 0; i < size; i++) {
            String opponentUsername = response.nextLine();
            String gameName = response.nextLine();
            String hostip = response.nextLine();
            boolean colorToPlay = !response.nextLine().equals("white");
            int wins = Integer.parseInt(response.nextLine());
            hosts.add(new Player(opponentUsername, gameName, hostip,
                    colorToPlay, wins));
        }
        response.nextLine();
        return hosts;
    }

    private Player getHostChoice(ArrayList<Player> hosts) {
        ChoiceDialog<Player> dialog = new ChoiceDialog<>(hosts.get(0), hosts);
        setAlertSettings(dialog, "Hosts to join", "Choose a host:");
        Optional<Player> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        }

        return null;
    }

    private void noHosts() {
        errorAlert("No Hosts", "There are no players hosting a game of "
                + "that type right now.");
    }

    private void rejectedRequest() {
        errorAlert("Host Rejected Request", "The Host of the game "
                + "rejected your request to play");
    }

    private void errorAlert(String title, String context) {
        Alert errorMessage = new Alert(Alert.AlertType.ERROR);
        setAlertSettings(errorMessage, title, context);
        errorMessage.showAndWait();
    }

    private void setAlertSettings(Dialog alert, String title, String context) {
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.initOwner(primaryStage);
        alert.setContentText(context);
    }

    private void hostGameSetup() {
        errorAlert("Host Game setup", "The game has been setup correctly, "
                + "single player game started while waiting for requests");
    }

    private boolean errorHandleRegisterUsername(Scanner response) {
        String header = response.nextLine();
        return !header.equals("REGISTERRESPONSE");
    }

    private void displayErrorMessage(Scanner response) {
        String errorMessage = response.nextLine()
                + "\nreturning to singleplayer.";
        response.nextLine();

        errorAlert("ERROR", errorMessage);
    }
}
