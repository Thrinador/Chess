package chess;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Driver is the base of the Chess Application. It initializes the stage and
 * places the menu bar and Chess into a vbox. Calls ChessIO for the file input
 * output and deals with the board when a checkmate or draw happens.
 *
 * @author Ben Clark
 */
public class Driver extends Application {

    /**
     * The stage used throughout the program.
     */
    private Stage primaryStage;

    /**
     * The Chess game.
     */
    private Chess game;

    /**
     * The location if specified for the saves to happen
     */
    private String saveLocation;

    /**
     * newGame takes in the vbox that contains the game it resets the game, then
     * adds it back to the vbox.
     *
     * @param vbox the vbox that contains the game
     */
    private void newGame(VBox vbox) {
        if (vbox.getChildren().size() > 1) {
            vbox.getChildren().remove(1);
        }
        game = new Chess();
        vbox.getChildren().add(game);

        game.booleanProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            //Game ended
            ArrayList<String> history = game.getHistory();
            if (!history.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Save");
                alert.setHeaderText(null);
                alert.setContentText("Do you want to save the game?");
                ButtonType buttonTypeYes = new ButtonType("Yes");
                ButtonType buttonTypeNo = new ButtonType("No");
                alert.initOwner(primaryStage);
                alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.get() == buttonTypeYes) {
                    ChessIO.saveGame(primaryStage, saveLocation, history, game.getNotationHistory());
                }
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit?");
            alert.setHeaderText(null);
            alert.setContentText("Do you want to exit the game or start "
                    + "a new game?");
            ButtonType buttonTypeNew = new ButtonType("New");
            ButtonType buttonTypeExit = new ButtonType("Exit");

            alert.getButtonTypes().setAll(buttonTypeNew, buttonTypeExit);
            alert.initOwner(primaryStage);
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == buttonTypeNew) {
                newGame(vbox);

            } else if (result.get() == buttonTypeExit) {
                System.exit(0);
            }
        });

        saveLocation = null;
    }

    /**
     * exitGame if they have moved prompts them to save the game then exits.
     */
    private void exitGame() {
        ArrayList<String> history = game.getHistory();
        if (!history.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(primaryStage);
            alert.setTitle("Save");
            alert.setHeaderText(null);
            alert.setContentText("Do you want to save the game?");
            ButtonType buttonTypeYes = new ButtonType("Yes");
            ButtonType buttonTypeNo = new ButtonType("No");

            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == buttonTypeYes) {
                ChessIO.saveGame(primaryStage, saveLocation, history, game.getNotationHistory());
            }
        }
        System.exit(0);
    }

    /**
     * undoMove takes in the vbox that contains the game. It gets the current
     * game history pulls the last move from it and reloads the game from a temp
     * file
     *
     * @param vbox the vbox that contains the game.
     */
    private void undoMove(VBox vbox) {
        ArrayList<String> history = game.getHistory();
        if (history.size() == 1) {
            newGame(vbox);
            return;
        }
        if (history.isEmpty()) {
            return;
        }
        history.remove(history.size() - 1);
        String currentSaveFile = saveLocation;
        saveLocation = "temp.chess";
        ChessIO.saveGame(primaryStage, saveLocation, history, game.getNotationHistory());
        File file = new File("temp.chess");
        ChessIO.importGame(vbox, file, game);
        saveLocation = currentSaveFile;
        file.delete();
        file = new File("temp.pgn");
        file.delete();
    }

    /**
     * start sets up the GUI. It initializes the Chess game, then makes the menu
     * bar and adds both to the vbox. Finally shows the GUI.
     *
     * @param primaryStage the stage for the game.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        VBox vbox = new VBox(0);

        MenuBar menuBar = new MenuBar();
        
        vbox.getChildren().add(menuBar);
        Scene scene = new Scene(vbox);

        Menu menuFile = new Menu("File");

        MenuItem newMenuItem = new MenuItem("New");
        menuFile.getItems().add(newMenuItem);
        newMenuItem.setOnAction(actionEvent -> newGame(vbox));

        MenuItem saveMenuItem = new MenuItem("Save");
        menuFile.getItems().add(saveMenuItem);
        saveMenuItem.setOnAction(actionEvent
                -> ChessIO.saveGame(primaryStage, saveLocation,
                        game.getHistory(), game.getNotationHistory()));

        MenuItem saveAsMenuItem = new MenuItem("SaveAs");
        menuFile.getItems().add(saveAsMenuItem);
        saveAsMenuItem.setOnAction(actionEvent
                -> ChessIO.saveGameAs(primaryStage, game.getHistory(),
                        game.getNotationHistory()));

        MenuItem loadMenuItem = new MenuItem("Load");
        menuFile.getItems().add(loadMenuItem);
        loadMenuItem.setOnAction(actionEvent
                -> {
            ChessIO.loadGame(primaryStage, vbox, game);
            game = (Chess) vbox.getChildren().get(1);
        });

        MenuItem exitMenuItem = new MenuItem("Exit");
        menuFile.getItems().add(exitMenuItem);
        exitMenuItem.setOnAction(actionEvent -> exitGame());

        Menu menuPlay = new Menu("Play");

        MenuItem undoMenuItem = new MenuItem("Undo");
        menuPlay.getItems().add(undoMenuItem);
        undoMenuItem.setOnAction(actionEvent -> undoMove(vbox));

        MenuItem notationMenuItem = new MenuItem("Notation");
        menuPlay.getItems().add(notationMenuItem);
        notationMenuItem.setOnAction(actionEvent
                -> ChessIO.getNotation(game.getNotationHistory()));

        menuBar.getMenus().addAll(menuFile, menuPlay);

        newGame(vbox);
        
        primaryStage.setTitle("Chess");
        primaryStage.getIcons().add(new Image("/Images/pawn_black.png"));
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(actionEvent -> exitGame());
        primaryStage.show();
    }

    /**
     * main launches the application
     *
     * @param args unused.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
