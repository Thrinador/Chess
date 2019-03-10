package chess;

import GameStart.EntryController;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author benji
 */
public class SinglePlayerStage extends Parent {

    private Stage primaryStage;
    private VBox vbox;
    private NewGame setup;

    public SinglePlayerStage(Stage s) {
        this.primaryStage = s;
    }

    private void exitGame() {
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

    private void newGame() {
        primaryStage.setScene(start());
        primaryStage.show();
    }

    public Scene start() {
        vbox = new VBox(0);
        //setup = new NewGame(primaryStage, vbox, user);

        MenuBar menuBar = new MenuBar();

        vbox.getChildren().add(menuBar);
        Scene scene = new Scene(vbox);

        Menu menuFile = new Menu("File");

        MenuItem newMenuItem = new MenuItem("New game");
        menuFile.getItems().add(newMenuItem);
        newMenuItem.setOnAction(ex -> this.newGame());

        MenuItem exitMenuItem = new MenuItem("Exit to menu");
        menuFile.getItems().add(exitMenuItem);
        exitMenuItem.setOnAction(actionEvent -> exitGame());

        menuBar.getMenus().addAll(menuFile);

        primaryStage.setOnCloseRequest(actionEvent -> exitGame());

        SingleplayerChess board = new SingleplayerChess();
        vbox.getChildren().add(board);

        return scene;
    }
}
