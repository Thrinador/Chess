package GameStart;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Ben Clark
 */
public class HostController implements Initializable {
    @FXML private TextField gameName;
    
    @FXML
    private void handleBackAction(ActionEvent event) {
        try {
            System.out.println("Back");
            Parent blah = FXMLLoader.load(getClass().getResource("EntryFXML.fxml"));
            Scene scene = new Scene(blah);
            Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            appStage.setScene(scene);
            appStage.show();
        } catch (IOException ex) {
            Logger.getLogger(EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void handleHostAction(ActionEvent event) {
        try {
            System.out.println("Host");
            if(gameName.getText().equals("")){
                gameName.getStyleClass().add("error");
                return;
            }
            gameName.getStyleClass().remove("error");
            
            Parent blah = FXMLLoader.load(getClass().getResource("EntryFXML.fxml"));
            Scene scene = new Scene(blah);
            Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            appStage.setScene(scene);
            appStage.show();
        } catch (IOException ex) {
            Logger.getLogger(EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
