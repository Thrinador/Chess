/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameStart;

import chess.SinglePlayerStage;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Ben Clark
 */
public class EntryController implements Initializable {

    @FXML
    private void handleSingleplayerAction(ActionEvent event) {
        System.out.println("Singleplayer");

        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SinglePlayerStage multi = new SinglePlayerStage(appStage);
        appStage.setScene(multi.start());
        appStage.show();
    }

    @FXML
    private void handleMultiplayerAction(ActionEvent event) {
        try {
            System.out.println("Multiplayer");
            Parent multi = FXMLLoader.load(getClass().getResource("MultiFXML.fxml"));
            Scene scene = new Scene(multi);
            Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            appStage.setScene(scene);
            appStage.show();
        } catch (IOException ex) {
            Logger.getLogger(EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleHelpAction(ActionEvent event) {
        System.out.println("Help");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
