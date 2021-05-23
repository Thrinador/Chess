package networking;

import static game.Game.HEIGHT;
import static game.Game.TILE_SIZE;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Chat represents the bar on the right side of a multiplayer game. It formats
 * the messages and manages the textAreas.
 *
 * @author Ben Clark
 */
public class Chat extends Pane {

    private final TextArea pastMessages;
    private final TextField newMessage;
    private final DataOutputStream outMessages;
    private final User user;

    /**
     * Constructor that takes in the socket that we are communicating with the
     * opponent on, and information about the user.
     *
     * @param sock The socket we are communicating with the opponent on.
     * @param user The user for the program.
     * @throws IOException If there is an issue with the socket
     */
    public Chat(Socket sock, User user) throws IOException {
        this.user = user;
        this.setPrefSize(TILE_SIZE * 3, HEIGHT * TILE_SIZE);
        this.setStyle("-fx-content-background: #bfbfbf;");
        this.setStyle("-fx-border-color: black");
        VBox vbox = new VBox();
        outMessages = new DataOutputStream(sock.getOutputStream());
        Label title = new Label();
        title.setPrefSize(this.getPrefWidth(), .5 * TILE_SIZE);
        title.setText(" Chat");
        title.setStyle("-fx-font: 18px");
        title.setStyle("-fx-border-color: black");

        pastMessages = new TextArea();
        pastMessages.setPrefSize(this.getPrefWidth(), 
                (HEIGHT - 1.5) * TILE_SIZE);
        pastMessages.setEditable(false);
        pastMessages.setStyle("-fx-background: #bfbfbf;");
        pastMessages.setStyle("-fx-border-color: black");
        pastMessages.setWrapText(true);

        newMessage = new TextField();
        newMessage.setPrefSize(this.getPrefWidth(), (HEIGHT - 7.5) * TILE_SIZE);
        newMessage.setStyle("-fx-border-color: black");

        Button send = new Button();
        send.setPrefSize(this.getPrefWidth(), (HEIGHT - 7.5) * TILE_SIZE);
        send.setText("Send");
        send.setDefaultButton(true);

        send.setOnAction(e -> this.sendPressed());
        vbox.getChildren().addAll(title, pastMessages, newMessage, send);
        this.getChildren().add(vbox);
    }

    private void sendPressed() {
        String message = newMessage.getText();
        if (message.length() == 0) {
            return;
        }
        message = message.replace('\n', ' ');
        newMessage.clear();
        addText(message, user.getUsername());

        try {
            outMessages.writeBytes("CHAT\n");
            outMessages.writeBytes(user.getUsername() + "\n");
            outMessages.writeBytes(message + "\n");
            outMessages.writeBytes("END\n");

        } catch (IOException ex) {
            System.out.println("IOEXception in chat");
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Given a message and username, formats the message and adds the username
     * so that it is displayed properly in the text area.
     *
     * @param message The message that needs to be displayed
     * @param username The person who sent the message.
     */
    public void addText(String message, String username) {
        message = pastMessages.getText() + username + ": " + message + "\n";
        pastMessages.setText(message);
    }

}
