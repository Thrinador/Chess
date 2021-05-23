package networking;

import static game.Game.TILE_SIZE;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 * Score represents the Labels on the bar at the top of the GUI.
 *
 * @author Ben Clark
 */
public class Score extends Pane {

    private final Player opponent;
    private final Player user;

    private final Label info;
    private final Label opponentInfo;
    private final Pane spacer;
    private final HBox hbox;

    /**
     * Constructor that takes in two players that are playing, and builds the
     * labels based off of those players.
     *
     * @param user The user on this side of the connection.
     * @param opponent The opponent that you are playing.
     */
    public Score(Player user, Player opponent) {
        this.setStyle("-fx-content-background: #bfbfbf;");
        this.setStyle("-fx-border-color: black");

        this.user = user;
        this.opponent = opponent;

        info = new Label(user.labelDisplay());
        info.setPrefSize(this.getPrefWidth(), .5 * TILE_SIZE);

        opponentInfo = new Label(opponent.labelDisplay());
        opponentInfo.setPrefSize(this.getPrefWidth(), .5 * TILE_SIZE);

        spacer = new Pane();
        spacer.setPrefWidth(TILE_SIZE);

        hbox = new HBox();
        hbox.getChildren().addAll(info, spacer, opponentInfo);
        this.getChildren().add(hbox);
        this.setVisible(true);
    }

    /**
     * Setter for the opponents score
     *
     * @param score The new score for the opponent, must be larger than previous
     * wins.
     */
    public void setOpponentScore(int score) {
        if (score > opponent.getWins()) {
            opponent.setWins(score);
            opponentInfo.setText(opponent.labelDisplay());
        }

    }

    /**
     * Setter for the users score.
     *
     * @param score The new score for the user, must be larger than previous
     * wins.
     */
    public void setUserScore(int score) {
        if (score > user.getWins()) {
            user.setWins(score);
            info.setText(user.labelDisplay());
        }

    }
}
