package chess;

import GameStart.EntryController;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import networking.Messages;
import networking.ServerInfo;
import networking.User;
import javafx.scene.Parent;
import javafx.scene.control.TextInputDialog;

/**
 * Driver is the base of the Game Application. It initializes the stage and
 * places the menu bar and Game into a vbox. Deals with the board when an
 * endgame happens.
 *
 * @author Ben Clark
 */
public class Driver extends Parent {

    private Stage primaryStage;
    private VBox vbox;
    private NewGame setup;
    private User user;

    public Driver(Stage s) {
        this.primaryStage = s;
    }

    private void exitGame() {
        if (vbox.getChildren().get(1) instanceof MultiplayerChess) {
            MultiplayerChess chess
                    = (MultiplayerChess) vbox.getChildren().get(1);

            if (chess.isAlive()) {
                chess.sendOnSocket(Messages.gameOver());
                chess.endGame(false);
            }
        }

        if (user.hasUsername()) {
            try {
                Messages.sendMessage(ServerInfo.SERVER_IP,
                        ServerInfo.SERVER_PORT,
                        Messages.unregister(user.getUsername()));
            } catch (IOException ex) {

            }
        }

        System.exit(0);
    }

    private String getNewServerIp() {
        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("Change Server IP");
        dialog.setHeaderText(null);
        dialog.setGraphic(null);
        dialog.initOwner(primaryStage);
        dialog.setContentText("Please Enter a new IP address for the server");

        Optional<String> message = dialog.showAndWait();
        if (message.isPresent()) {
            String serverIp = message.get();
            if (serverIp.length() <= 15 && serverIp.length() >= 1) {
                return serverIp;
            }
        }
        return null;
    }

    private void changeServerIp() {
        String serverIp = getNewServerIp();
        if (serverIp != null) {
            ServerInfo.SERVER_IP = serverIp;
        }
    }

    private void newGame() {
        try {
            System.out.println("NewGame");
            Parent multi = FXMLLoader.load(getClass().getResource("/GameStart/EntryFXML.fxml"));
            Scene scene = new Scene(multi);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException ex) {
            Logger.getLogger(EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This function starts the application. It is given a Stage to add the
     * chess elements to.
     *
     * @return
     */
    public Scene start() {
        vbox = new VBox(0);
        user = new User();
        setup = new NewGame(primaryStage, vbox, user);

        MenuBar menuBar = new MenuBar();

        vbox.getChildren().add(menuBar);
        Scene scene = new Scene(vbox);

        Menu menuFile = new Menu("File");

        MenuItem newMenuItem = new MenuItem("New");
        menuFile.getItems().add(newMenuItem);
        newMenuItem.setOnAction(ex -> this.newGame());

        MenuItem changeMenuItem = new MenuItem("Change Server Ip");
        menuFile.getItems().add(changeMenuItem);
        changeMenuItem.setOnAction(actionEvent -> changeServerIp());

        MenuItem exitMenuItem = new MenuItem("Exit");
        menuFile.getItems().add(exitMenuItem);
        exitMenuItem.setOnAction(actionEvent -> exitGame());

        menuBar.getMenus().addAll(menuFile);

        primaryStage.setOnCloseRequest(actionEvent -> exitGame());

        SingleplayerChess board = new SingleplayerChess();
        vbox.getChildren().add(board);

        return scene;
    }
}
