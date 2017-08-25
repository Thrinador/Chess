package chess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * ChessIO deals with the file input and output. It deals with saving the game
 * and loading the game.
 *
 * @author Ben Clark
 */
public class ChessIO {

    /**
     * checkForNewGame takes in a gameHistory and if it is empty sends an alert
     * about saving a new game.
     *
     * @param history checked to see if empty
     * @return whether or not the history is empty
     */
    private static boolean checkForNewGame(ArrayList<String> history) {
        if (history.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Save");
            alert.setHeaderText(null);
            alert.setContentText("Can't save a new game.");
            alert.showAndWait();
            return true;
        }
        return false;
    }

    /**
     * getSaveFileName takes in the stage that we are acting on and prompts the
     * user for a saveLocation.
     *
     * @param primaryStage the stage in use
     * @return location of file in string form
     */
    private static String getSaveFileName(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Game");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CHESS Files", "*.chess"));

        File selectedFile = fileChooser.showSaveDialog(primaryStage);
        if (selectedFile == null) {
            return "";
        }
        return selectedFile.getAbsolutePath();
    }

    /**
     * addSpaces takes in a string and formats it to length 9 with spaces
     *
     * @param s the string to be formatted
     * @return the formatted string
     */
    private static String addSpaces(String s) {
        for (int i = s.length(); i < 9; i++) {
            s += " ";
        }
        return s;
    }

    /**
     * printToFile takes in a fileName and the current history /
     * notationHistory, then prints them to files based on the fileName
     *
     * @param fileName the name of the .chess file to be saved.
     * @param history the current game history in the easy to parse form
     * @param notationHistory the algebraic notation of the game
     * @return the fileName used or blank if the file failed
     */
    private static String printToFile(String fileName,
            ArrayList<String> history, ArrayList<String> notationHistory) {
        try {
            PrintWriter pw = new PrintWriter(fileName);
            //history.deleteCharAt(history.length() - 1);
            for (int i = 0; i < history.size() - 1; i++) {
                pw.write(history.get(i));
                pw.write('\n');
            }
            pw.write(history.get(history.size() - 1));

            pw.flush();
            pw.close();
            pw = new PrintWriter((fileName.substring(0, fileName.length() - 5) + "pgn"));
            boolean turn = true;
            for (int i = 0; i < notationHistory.size(); i++, turn = !turn) {
                if (turn) {
                    if ((i / 2 + 1) < 10) {
                        pw.write((i / 2 + 1) + ".  ");
                    } else {
                        pw.write((i / 2 + 1) + ". ");
                    }
                }
                pw.write(addSpaces(notationHistory.get(i) + " "));
                if (!turn) {
                    pw.write('\n');
                }
            }
            pw.flush();
            pw.close();

            return fileName;
        } catch (FileNotFoundException ex) {
            return "";
        }
    }

    /**
     * saveGame takes in the currently used stage, the location to save, the
     * history and the notation of the game. Using those it attempts to save the
     * game. History must not be empty if saveLocation is null prompt the user
     * for one.
     *
     * @param primaryStage The currently in use stage
     * @param saveLocation the location we are saving to or null if need to get
     * one
     * @param history the current game history
     * @param notationHistory the algebraic notation for the game
     * @return save location or blank.
     */
    public static String saveGame(Stage primaryStage, String saveLocation, ArrayList<String> history, ArrayList<String> notationHistory) {
        if (checkForNewGame(history)) {
            return "";
        }
        String fileName;
        if (saveLocation != null) {
            fileName = saveLocation;
        } else {
            fileName = getSaveFileName(primaryStage);
            if (fileName.equals("")) {
                return "";
            }
        }
        return printToFile(fileName, history, notationHistory);
    }

    /**
     * saveGameAs always prompts the user to give a save location then uses that
     * to save the game.
     *
     * @param primaryStage the currently in use stage
     * @param history the current games history
     * @param notationHistory the algebraic notation for the game
     */
    public static void saveGameAs(Stage primaryStage, ArrayList<String> history,
            ArrayList<String> notationHistory) {
        if (checkForNewGame(history)) {
            return;
        }
        String fileName = getSaveFileName(primaryStage);
        if (fileName.equals("")) {
            return;
        }
        printToFile(fileName, history, notationHistory);
    }

    /**
     * loadGame takes in the stage the vbox used in the stage and the game, then
     * it gets the file to load from and if the file looks good attempts to
     * import the game.
     *
     * @param primaryStage the currently in use stage
     * @param vbox the vbox that holds the game
     * @param game the game
     * @return the location of the file that they uploaded from or ""
     */
    public static String loadGame(Stage primaryStage, VBox vbox, Chess game) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Game");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CHESS Files", "*.chess"));

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile == null || !selectedFile.exists()) {
            return "";
        }
        return importGame(vbox, selectedFile, game);
    }

    /**
     * importGame takes in a vbox, a file and the game. It removes the game from
     * the vbox resets the game and attempts to use the file to rebuild the
     * imported game. Then it adds the game back to the vbox and returns.
     *
     * @param vbox the vbox that contains the game
     * @param file the file to be imported.
     * @param game the current chess game
     * @return the location of the file that was imported.
     */
    public static String importGame(VBox vbox, File file, Chess game) {
        vbox.getChildren().remove(1);
        game = new Chess();
        vbox.getChildren().add(game);
        Scanner fileIn;
        try {
            fileIn = new Scanner(file);
        } catch (FileNotFoundException ex) {
            return "";
        }
        while (fileIn.hasNextLine()) {
            String line = fileIn.nextLine();
            Scanner lineIn = new Scanner(line);
            int oldX = lineIn.nextInt();
            int oldY = lineIn.nextInt();
            int newX = lineIn.nextInt();
            int newY = lineIn.nextInt();
            char pp = ' ';
            if (lineIn.hasNext()) {
                pp = lineIn.next().charAt(0);
            }
            game.makeMove(oldX, oldY, newX, newY, pp);
        }
        return file.getAbsolutePath();
    }

    /**
     * getNotation takes in the notation history and prints it to the screen.
     *
     * @param notationHistory the current algebraic notation for the game
     */
    public static void getNotation(ArrayList<String> notationHistory) {
        boolean turn = true;
        for (int i = 0; i < notationHistory.size(); i++, turn = !turn) {
            if (turn) {
                if ((i / 2 + 1) < 10) {
                    System.out.print((i / 2 + 1) + ".  ");
                } else {
                    System.out.print((i / 2 + 1) + ". ");
                }
            }
            System.out.print(addSpaces(notationHistory.get(i) + " "));
            if (!turn) {
                System.out.println();
            }
        }
        System.out.println("");
    }
}
