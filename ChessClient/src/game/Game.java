package game;

import static game.PieceType.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.*;

/**
 * Game is a Pane extension that represents a chess board. Game contains all the
 * logic on how pieces can move and ends when either a checkmate or draw
 * happens. Game works by making an 8x8 board of Tiles and then assigning
 * piece's to those tiles.
 *
 * @author Ben Clark
 */
public abstract class Game extends Pane {

    /**
     * The number of pixels in a side of the tile square.
     */
    public static final int TILE_SIZE = 80;

    /**
     * How many tiles across the board is.
     */
    public static final int WIDTH = 8;

    /**
     * How many tiles till the board is.
     */
    public static final int HEIGHT = 8;

    /**
     * Number of pixels needed to center the piece on the square.
     */
    public static final int OFFSET = 10;

    private boolean turn;
    private boolean team;

    /**
     * The tiles that make up the chess board.
     */
    protected Tile[][] board;

    /**
     * A group that stores all the current in use pieces.
     */
    protected Group pieceGroup;

    /**
     * KingLocation keeps track of the current turns king.
     */
    protected KingLocation kingLoc;

    /**
     * When a pawn moves two squares forward this becomes that pawn. Used in en
     * passant.
     */
    protected Piece doubleMovePawn;

    private int stalemateCounter;

    /**
     * Keeps copies of the current board in the form of an Arraylist. If there
     * is every a value of 3 in one if the buckets the game is a draw.
     */
    protected HashMap<ArrayList<Integer>, Integer> threeRepetition;

    /**
     * The history of the game stored as 4 int's and if there was a pawn
     * promotion then also a char.
     */
    protected ArrayList<String> history;

    /**
     * notationHistory is the algebraic notation for the moves.
     */
    //protected ArrayList<String> notationHistory;
    /**
     * typeOfMove represents the last move as an enum. This helps
     * notationHistory make the algebraic notation.
     */
    protected MoveType typeOfMove;

    /**
     * booleanProperty represents the game. It is always true until either a
     * checkmate happens or some form of draw. When it changes driver deals with
     * what to do next.
     */
    public BooleanProperty gameAlive;

    /**
     * The constructor for chess initilizes all the global variables and sets
     * the starting location for all the pieces on the board. It also makes the
     * tiles for the chess board and adds the appropriate pieces to those tiles.
     */
    public Game() {
        gameAlive = new SimpleBooleanProperty(true);
        Group tileGroup = new Group();
        pieceGroup = new Group();
        board = new Tile[WIDTH][HEIGHT];
        doubleMovePawn = null;
        kingLoc = new KingLocation(4, 7, 4, 0);
        threeRepetition = new HashMap<>();
        turn = true;
        team = true;
        history = new ArrayList<>();

        this.setPrefSize((WIDTH) * TILE_SIZE, HEIGHT * TILE_SIZE);
        this.getChildren().addAll(tileGroup, pieceGroup);

        //Tile init
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile((x + y) % 2 == 0, x, y);
                board[x][y] = tile;
                tileGroup.getChildren().add(tile);
            }
        }

        //Black's back line
        initPiece("/Images/rook_black.png", 0, 0, false, ROOK);
        initPiece("/Images/knight_black.png", 1, 0, false, KNIGHT);
        initPiece("/Images/bishop_black.png", 2, 0, false, BISHOP);
        initPiece("/Images/queen_black.png", 3, 0, false, QUEEN);
        initPiece("/Images/king_black.png", 4, 0, false, KING);
        initPiece("/Images/bishop_black.png", 5, 0, false, BISHOP);
        initPiece("/Images/knight_black.png", 6, 0, false, KNIGHT);
        initPiece("/Images/rook_black.png", 7, 0, false, ROOK);

        //White's back line
        initPiece("/Images/rook_white.png", 0, 7, true, ROOK);
        initPiece("/Images/knight_white.png", 1, 7, true, KNIGHT);
        initPiece("/Images/bishop_white.png", 2, 7, true, BISHOP);
        initPiece("/Images/queen_white.png", 3, 7, true, QUEEN);
        initPiece("/Images/king_white.png", 4, 7, true, KING);
        initPiece("/Images/bishop_white.png", 5, 7, true, BISHOP);
        initPiece("/Images/knight_white.png", 6, 7, true, KNIGHT);
        initPiece("/Images/rook_white.png", 7, 7, true, ROOK);

        //Pawns
        for (int x = 0; x < 8; x++) {
            initPiece("/Images/pawn_white.png", x, 6, true, PAWN);
            initPiece("/Images/pawn_black.png", x, 1, false, PAWN);
        }
        addBoardToMap();
    }

    /**
     * Converts the board into an ArrayList of ints based on their enum value.
     * Then checks to see if that board is currently stored if it is increment
     * the value. if not then add the board with value to the map.
     */
    protected final void addBoardToMap() {
        ArrayList<Integer> currentBoard = new ArrayList<>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (board[x][y].hasPiece()) {
                    Piece p = board[x][y].getPiece();
                    int pieceValue = p.getType().type;
                    if (!p.getTeam()) {
                        pieceValue *= -1;
                    }
                    currentBoard.add(pieceValue);
                } else {
                    currentBoard.add(0);
                }
            }
        }
        if (threeRepetition.containsKey(currentBoard)) {
            Integer val = threeRepetition.get(currentBoard);
            threeRepetition.replace(currentBoard, ++val);
        } else {
            threeRepetition.put(currentBoard, 1);
        }
    }

    /**
     * Takes in a pixel location on the board for either x or y. Then converts
     * that to a tile location.
     *
     * @param pixel double representing some pixel location
     * @return the tile associated with that pixel location.
     */
    private static int toBoard(double pixel) {
        return (int) (pixel + TILE_SIZE / 2) / TILE_SIZE;
    }

    /**
     * initPiece makes a Piece based off the image given the starting location
     * the team the piece belongs to and the type of piece. Also makes the mouse
     * listener for when the mouse is released.
     *
     * @param url The location of the image for the piece.
     * @param x The starting x location for the piece
     * @param y the starting y location for the piece
     * @param team the team that the piece belongs to
     * @param type the type of piece this is.
     */
    private void initPiece(String url, int x, int y,
            boolean team, PieceType type) {
        Piece piece = new Piece(url, x, y, team, type, this);
        pieceGroup.getChildren().add(piece);
        board[x][y].setPiece(piece);
        piece.setOnMouseReleased(e -> {
            //Get the piece's current location, then center it to a square
            int newX = toBoard(piece.getLayoutX());
            int newY = toBoard(piece.getLayoutY());

            //Get the piece's old location, then center it to a square
            int oldX = toBoard(piece.getOldX());
            int oldY = toBoard(piece.getOldY());

            makeMove(oldX, oldY, newX, newY, ' ', turn);
        });
    }

    /**
     * makeMove attempts to move the piece at oldX oldY to newX newY. pp is used
     * when there is a pawn promotion.
     *
     * @param oldX the starting x for the piece
     * @param oldY the starting y for the piece
     * @param newX the ending x for the piece
     * @param newY the ending y for the piece
     * @param pp type of piece to promote to if pawn promotion
     * @param team
     * @return
     */
    public abstract boolean makeMove(int oldX, int oldY, int newX,
            int newY, char pp, boolean team);

    /**
     * addMoveToHistory takes in a starting location and ending location for a
     * piece then adds that move to history and notationHistory.
     *
     * @param oldX starting x for the move
     * @param oldY starting y for the move
     * @param newX ending x for the move
     * @param newY ending y for the move
     */
    protected void addMoveToHistory(int oldX, int oldY, int newX, int newY) {
        if (typeOfMove == MoveType.PAWN_PROMOTION) {
            int pieceNum = board[newX][newY].getPiece().getType().type - 1;
            history.add(oldX + " " + oldY + " " + newX + " " + newY + " "
                    + pieceNum);
        } else {
            history.add(oldX + " " + oldY + " " + newX + " " + newY);
        }
    }

    /**
     * resetDrawConditions clears out the HashMap and counter that are tracking
     * draw conditions.
     */
    protected void resetDrawConditions() {
        threeRepetition.clear();
        stalemateCounter = 0;
    }

    /**
     * checkForDraw checks the four ways you can draw in chess. If any are true
     * then it sends an alert and ends the game.
     */
    protected void checkForDraw() {
        if (checkInsufficientMatingMaterial()) {
            drawAlert(" insufficent mating material");
        } else if (stalemate()) {
            drawAlert(" stalemate");
        } else if (fiftyMoveRule()) {
            drawAlert(" 50 moves");
        } else if (threeFoldRepetition()) {
            drawAlert(" three fold repetition");
        }
    }

    /**
     * drawAlert takes in the message and ends the game from a draw.
     *
     * @param message the message associated with the type of draw that happened
     */
    private void drawAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("Game is drawn by" + message);
        Window window = this.getScene().getWindow();
        alert.initOwner(window);
        alert.showAndWait();
        gameAlive.setValue(false);
    }

    /**
     * stalemate checks to see if the game is drawn by stalemate. Stalemate
     * happens when no piece is able to move and the king is not in check.
     *
     * @return boolean representing whether the game has been drawn
     */
    private boolean stalemate() {
        if (inCheck(kingLoc.getKingX(turn), kingLoc.getKingY(turn))) {
            return false;
        }
        for (int pieceX = 0; pieceX < WIDTH; pieceX++) {
            for (int pieceY = 0; pieceY < HEIGHT; pieceY++) {
                if (board[pieceX][pieceY].hasPiece()
                        && turn == board[pieceX][pieceY].getPiece().getTeam()) {
                    for (int locX = 0; locX < WIDTH; locX++) {
                        for (int locY = 0; locY < HEIGHT; locY++) {
                            if (pieceCanMove(pieceX, pieceY, locX, locY)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * threeFoldRepetition goes through the hashMap of boards and checks to see
     * if any contain the value of three if they do then the game is drawn
     *
     * @return whether or not the game has been drawn
     */
    private boolean threeFoldRepetition() {
        return threeRepetition.values().stream().anyMatch((val) -> (val > 2));
    }

    /**
     * fiftyMoveRule checks to see if the game has been drawn from the fifty
     * move rule. A move in chess is considered both white and black moving a
     * piece. I am incrementing the counter every time the piece is moved. so I
     * need to check to see if it is greater than 100 instead of 50.
     *
     * @return whether or not the game has been drawn
     */
    private boolean fiftyMoveRule() {
        if (stalemateCounter > 100) {
            return true;
        } else {
            stalemateCounter++;
        }
        return false;
    }

    /**
     * checkInsufficientMatingMaterial checks to see if both sides don't have
     * the necessary material to mate the other side. Insufficient mating
     * material happens when neither side has a pawn, rook, queen, or less than
     * two minor pieces.
     *
     * @return whether or not the game has been drawn
     */
    private boolean checkInsufficientMatingMaterial() {
        ArrayList<PieceType> whitePieces = new ArrayList<>();
        ArrayList<PieceType> blackPieces = new ArrayList<>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (board[x][y].hasPiece()) {
                    Piece p = board[x][y].getPiece();
                    if (p.getType() == PAWN
                            || p.getType() == QUEEN
                            || p.getType() == ROOK) {
                        return false;
                    }

                    if (p.getTeam()) {
                        whitePieces.add(p.getType());
                    } else {
                        blackPieces.add(p.getType());
                    }
                }
            }
        }
        return !(whitePieces.size() > 2 || blackPieces.size() > 2);
    }

    /**
     * pawnPromotion takes in the pawns location and maybe the piece it is
     * promoting to and promotes the pawn. If a piece is not specified for the
     * pawn to promote to then an alert box pops up and the user is prompted to
     * choose a piece.
     *
     * @param x the x location for the piece
     * @param y the y location for the piece
     * @param pawnPromotion
     */
    protected void pawnPromotion(int x, int y, char pawnPromotion) {
        Piece piece = board[x][y].getPiece();
        String color = turn ? "white" : "black";
        if (pawnPromotion != ' ') {
            switch (pawnPromotion) {
                case 'Q':
                case 'q':
                    piece.setType(QUEEN);
                    piece.setImage(
                            new Image("/Images/queen_" + color + ".png"));
                    break;
                case 'N':
                case 'n':
                    piece.setType(KNIGHT);
                    piece.setImage(
                            new Image("/Images/knight_" + color + ".png"));
                    break;
                case 'B':
                case 'b':
                    piece.setType(BISHOP);
                    piece.setImage(
                            new Image("/Images/bishop_" + color + ".png"));
                    break;
                case 'R':
                case 'r':
                    piece.setType(ROOK);
                    piece.setImage(
                            new Image("/Images/rook_" + color + ".png"));
                    break;
                default:
                    break;
            }
            return;
        }
        
        Alert alert = new Alert(AlertType.NONE);
        alert.setTitle("Pawn Promotion");
        alert.setHeaderText(null);
        alert.setContentText("Choose a piece to promote the pawn to");
        alert.initStyle(StageStyle.UNDECORATED);
        Window window = this.getScene().getWindow();
        alert.initOwner(window);

        ButtonType queen = new ButtonType("Queen");
        ButtonType rook = new ButtonType("Rook");
        ButtonType knight = new ButtonType("Knight");
        ButtonType bishop = new ButtonType("Bishop");

        alert.getButtonTypes().setAll(queen, rook, knight, bishop);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == rook) {
            piece.setType(ROOK);
            piece.setImage(new Image("/Images/rook_" + color + ".png"));
        } else if (result.get() == bishop) {
            piece.setType(BISHOP);
            piece.setImage(new Image("/Images/bishop_" + color + ".png"));
        } else if (result.get() == knight) {
            piece.setType(KNIGHT);
            piece.setImage(new Image("/Images/knight_" + color + ".png"));
        } else {
            piece.setType(QUEEN);
            piece.setImage(new Image("/Images/queen_" + color + ".png"));
        }
        
        typeOfMove = MoveType.PAWN_PROMOTION;
    }

    /**
     * canMove takes in a pieces starting location and a location it is trying
     * to move to. Checks to see if that piece is capable of moving there.
     *
     * @param oldX starting x for the move
     * @param oldY starting y for the move
     * @param newX ending x for the move
     * @param newY ending y for the move
     * @return whether or not the piece can move to that location
     */
    private boolean canMove(int oldX, int oldY, int newX, int newY) {
        Piece start = board[oldX][oldY].getPiece();
        Piece end = board[newX][newY].getPiece();

        //Make sure the start piece exists and cant take own pieces
        if (start == null || start.getTeam() != turn
                || (end != null && start.getTeam() == end.getTeam())) {
            return false;
        }

        switch (start.getType()) {
            case PAWN:
                return pawnMovementCheck(oldX, oldY, newX, newY);
            case ROOK:
                return horizontalMovementCheck(oldX, oldY, newX, newY);
            case KNIGHT:
                return knightMovementCheck(oldX, oldY, newX, newY);
            case BISHOP:
                return diagonalMovementCheck(oldX, oldY, newX, newY);
            case QUEEN:
                return diagonalMovementCheck(oldX, oldY, newX, newY)
                        || horizontalMovementCheck(oldX, oldY, newX, newY);
            case KING:
                return kingMovementCheck(oldX, oldY, newX, newY);
            default:
                return false;
        }
    }

    /**
     * checkMate checks to see if the king has been checkMated. This happens in
     * two steps first it checks to see if the king can move anywhere. Then it
     * checks to see if any piece is capable of blocking the check or taking the
     * piece that is attacking.
     *
     * @return whether or not the king has been checkmated.
     */
    protected boolean checkMate() {
        int kingX = kingLoc.getKingX(turn);
        int kingY = kingLoc.getKingY(turn);

        //can king move
        if (pieceCanMove(kingX, kingY, kingX, kingY + 1)
                || pieceCanMove(kingX, kingY, kingX, kingY - 1)
                || pieceCanMove(kingX, kingY, kingX + 1, kingY + 1)
                || pieceCanMove(kingX, kingY, kingX + 1, kingY - 1)
                || pieceCanMove(kingX, kingY, kingX + 1, kingY)
                || pieceCanMove(kingX, kingY, kingX - 1, kingY + 1)
                || pieceCanMove(kingX, kingY, kingX - 1, kingY - 1)
                || pieceCanMove(kingX, kingY, kingX - 1, kingY)) {
            return false;
        }

        //can piece block or take
        for (int pieceX = 0; pieceX < WIDTH; pieceX++) {
            for (int pieceY = 0; pieceY < HEIGHT; pieceY++) {
                if (board[pieceX][pieceY].hasPiece()
                        && turn == board[pieceX][pieceY].getPiece().getTeam()) {
                    for (int locX = 0; locX < WIDTH; locX++) {
                        for (int locY = 0; locY < HEIGHT; locY++) {
                            if (pieceCanMove(pieceX, pieceY, locX, locY)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        typeOfMove = MoveType.CHECK_MATE;
        return true;
    }

    /**
     * pieceCanMove adds in an extra layer of checks for the piece canMove
     * method. It first checks that the locations given are valid. then makes
     * sure that the piece exists. Then it checks to see if that wont cause an
     * issue with putting the king in check.
     *
     * @param oldX starting x for the move
     * @param oldY starting y for the move
     * @param newX ending x for the move
     * @param newY ending y for the move
     * @return whether or not the piece can move.
     */
    protected boolean pieceCanMove(int oldX, int oldY, int newX, int newY) {
        if (newX < 0 || newX > 7 || newY < 0 || newY > 7
                || !board[oldX][oldY].hasPiece()) {
            return false;
        }
        return canMove(oldX, oldY, newX, newY)
                && !checkPieceBlock(oldX, oldY, newX, newY);
    }

    /**
     * inCheck takes in the current king's location then checks to see if any
     * piece on the opposing team can move to that location. If they can then
     * the king is in check
     *
     * @param kingX x location of the king
     * @param kingY y location of the king
     * @return whether or not the king is in check
     */
    protected boolean inCheck(int kingX, int kingY) {
        changeTurn();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (canMove(x, y, kingX, kingY)) {
                    changeTurn();
                    return true;
                }
            }
        }
        changeTurn();
        return false;
    }

    /**
     * checkPieceBlock takes in a starting and ending location for a piece then
     * moves that piece to the location. Records whether or not the king is in
     * check, moves the piece back to the starting location and returns the
     * result.
     *
     * @param oldX starting x for the move
     * @param oldY starting y for the move
     * @param newX ending x for the move
     * @param newY ending y for the move
     * @return whether or not this move will leave the king in check.
     */
    private boolean checkPieceBlock(int oldX, int oldY, int newX, int newY) {
        //Move piece from old xy to new xy
        Piece piece = board[oldX][oldY].getPiece();
        Piece locationPiece = board[newX][newY].getPiece();
        board[newX][newY].setPiece(piece);
        if (piece.getType() == KING) {
            kingLoc.setKingLocation(turn, newX, newY);
        }
        board[oldX][oldY].setPiece(null);

        int kingX = kingLoc.getKingX(turn);
        int kingY = kingLoc.getKingY(turn);

        boolean inCheck = inCheck(kingX, kingY);

        //move the piece back
        board[newX][newY].setPiece(locationPiece);
        board[oldX][oldY].setPiece(piece);
        if (piece.getType() == KING) {
            kingLoc.setKingLocation(turn, oldX, oldY);
        }

        return inCheck;
    }

    /**
     * kingMovementCheck takes in a starting location and ending location for a
     * king piece and checks to see if a king would be able to make that move.
     *
     * @param oldX starting x for the move
     * @param oldY starting y for the move
     * @param newX ending x for the move
     * @param newY ending y for the move
     * @return whether or not this is a valid king move
     */
    private boolean kingMovementCheck(int oldX, int oldY, int newX, int newY) {
        //Castling
        if (Math.abs(oldX - newX) == 2 && Math.abs(oldY - newY) == 0) {
            //Make sure the king has not moved and that it is not in check
            if (board[oldX][oldY].getPiece().hasMoved()
                    || inCheck(oldX, oldY)) {
                return false;
            }
            //King side
            if (oldX < newX) {
                //Check to see if there is a piece in the way.
                if (board[oldX + 1][oldY].hasPiece()
                        || board[oldX + 2][oldY].hasPiece()) {
                    return false;
                }

                //check to make sure the king wont go through inCheck
                if (checkPieceBlock(oldX, oldY, oldX + 1, oldY)
                        || checkPieceBlock(oldX, oldY, oldX + 2, oldY)) {
                    return false;
                }

                //Make sure the piece exists
                if (board[oldX + 3][oldY].hasPiece()) {
                    Piece rook = board[oldX + 3][oldY].getPiece();
                    if (rook.getType() == ROOK && !rook.hasMoved()) {
                        board[newX - 1][newY].setPiece(rook);
                        board[oldX + 3][oldY].setPiece(null);
                        rook.move(newX - 1, newY);
                        typeOfMove = MoveType.KING_CASTLE;
                        return true;
                    }
                }
                //Queen side
            } else {
                //Check to see if there is a piece in the way.
                if (board[oldX - 1][oldY].hasPiece()
                        || board[oldX - 2][oldY].hasPiece()) {
                    return false;
                }

                //check to make sure the king wont go through inCheck
                if (checkPieceBlock(oldX, oldY, oldX - 1, oldY)
                        || checkPieceBlock(oldX, oldY, oldX - 2, oldY)) {
                    return false;
                }

                //Make sure the piece exists
                if (board[oldX - 4][oldY].hasPiece()) {
                    Piece rook = board[oldX - 4][oldY].getPiece();

                    //Make sure the piece is a rook then make sure it has
                    //not moved
                    if (rook.getType() == ROOK && !rook.hasMoved()) {
                        board[newX + 1][newY].setPiece(rook);
                        board[oldX - 4][oldY].setPiece(null);
                        rook.move(newX + 1, newY);
                        typeOfMove = MoveType.QUEEN_CASTLE;
                        return true;
                    }
                }
            }
        }
        return (Math.abs(oldX - newX) < 2 && Math.abs(oldY - newY) < 2);
    }

    /**
     * pawnMovementCheck takes in a starting location and a ending location and
     * then checks to see if this is a valid move for a pawn to make.
     *
     * @param oldX starting x for the move
     * @param oldY starting y for the move
     * @param newX ending x for the move
     * @param newY ending y for the move
     * @return whether or not this is a valid pawn movement
     */
    private boolean pawnMovementCheck(int oldX, int oldY, int newX, int newY) {
        boolean pieceTeam = board[oldX][oldY].getPiece().getTeam();
        //Make sure the pawns are moving in the right direction
        if ((pieceTeam && oldY <= newY) || (!pieceTeam && oldY >= newY)) {
            return false;
        }
        //Movement
        if (newX == oldX) {
            //Can't take a piece from normal movement
            if (board[newX][newY].hasPiece()) {
                return false;
            }
            //Normal one space move forward
            if (Math.abs(newY - oldY) == 1) {
                return true;
            }
            //Double move forward if on starting line
            if (Math.abs(newY - oldY) == 2) {
                if ((pieceTeam && !board[newX][newY + 1].hasPiece()
                        && oldY == 6) || (!pieceTeam
                        && !board[newX][newY - 1].hasPiece() && oldY == 1)) {
                    return true;
                }
            }
        }
        //Taking a Piece
        if (Math.abs(newX - oldX) == 1 && board[newX][newY].hasPiece()
                && (Math.abs(newY - oldY) == 1)) {
            return true;
        }
        return enpassantCheck(oldX, oldY, newX, newY);
    }

    /**
     * enpassantCheck takes in a starting location and ending location and check
     * to see if this is a valid en passant move.
     *
     * @param oldX starting x for the move
     * @param oldY starting y for the move
     * @param newX ending x for the move
     * @param newY ending y for the move
     * @return whether or not this is a valid en passant move.
     */
    private boolean enpassantCheck(int oldX, int oldY, int newX, int newY) {
        //can enpassant if the last move was not a double pawn move.
        if (doubleMovePawn == null) {
            return false;
        }
        int otherPawnX = toBoard(doubleMovePawn.getOldX());
        int otherPawnY = toBoard(doubleMovePawn.getOldY());

        //check that the pawn moved in the appropriate direction only one square
        if (Math.abs(oldX - otherPawnX) == 1 && newX == otherPawnX) {
            //Check that the pawn started nex to the other pawn and that it
            //moved forward only one square.
            if (oldY == otherPawnY && Math.abs(newY - otherPawnY) == 1) {
                Piece killedPiece = board[otherPawnX][otherPawnY].getPiece();

                Platform.runLater(() -> {
                    pieceGroup.getChildren().remove(killedPiece);
                });

                //pieceGroup.getChildren().remove(killedPiece);
                board[otherPawnX][otherPawnY].setPiece(null);
                return true;
            }
        }

        return false;
    }

    /**
     * knightMovementCheck takes in a starting location and ending location then
     * checks to see if this is a valid movement for a knight.
     *
     * @param oldX starting x for the move
     * @param oldY starting y for the move
     * @param newX ending x for the move
     * @param newY ending y for the move
     * @return whether or not this is a valid movement for a knight
     */
    private boolean knightMovementCheck(int oldX, int oldY, int newX,
            int newY) {
        return (((Math.abs(oldX - newX) == 1)
                && (Math.abs(oldY - newY) == 2))
                || ((Math.abs(oldX - newX) == 2)
                && (Math.abs(oldY - newY) == 1)));
    }

    /**
     * horizontalMovementCheck takes in a starting location and an ending
     * location, then checks to see if this is a valid movement for either a
     * rook or a queen.
     *
     * @param oldX starting x for the move
     * @param oldY starting y for the move
     * @param newX ending x for the move
     * @param newY ending y for the move
     * @return whether or not this is a valid straight line movement.
     */
    private boolean horizontalMovementCheck(int oldX, int oldY,
            int newX, int newY) {
        return ((oldX != newX && oldY == newY)
                || (oldY != newY && oldX == newX))
                && horizontalCheckForPieces(oldX, oldY, newX, newY);
    }

    /**
     * horizontalCheckForPieces takes in a starting location and ending location
     * and checks to see if there are no pieces in between them. Only for
     * straight line movements.
     *
     * @param oldX starting x for the move
     * @param oldY starting y for the move
     * @param newX ending x for the move
     * @param newY ending y for the move
     * @return true if there are no pieces in the way
     */
    private boolean horizontalCheckForPieces(int oldX, int oldY,
            int newX, int newY) {
        if (oldX == newX) {
            //Movement in the Y direction
            int minY = Math.min(newY, oldY);
            int maxY = Math.max(newY, oldY);
            for (int i = minY + 1; i < maxY; i++) {
                if (board[oldX][i].hasPiece()) {
                    return false;
                }
            }
        } else {
            //Movement in the X direction
            int minX = Math.min(newX, oldX);
            int maxX = Math.max(oldX, newX);
            for (int i = minX + 1; i < maxX; i++) {
                if (board[i][oldY].hasPiece()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * diagonalMovementCheck takes in a starting location and ending location,
     * then checks if this is a valid diagonal movement for a piece.
     *
     * @param oldX starting x for the move
     * @param oldY starting y for the move
     * @param newX ending x for the move
     * @param newY ending y for the move
     * @return whether or not this is a valid diagonal movement
     */
    private boolean diagonalMovementCheck(int oldX, int oldY,
            int newX, int newY) {
        return (Math.abs(oldX - newX) == Math.abs(oldY - newY))
                && diagonalCheckForPieces(oldX, oldY, newX, newY);
    }

    /**
     * diagonalCheckForPieces checks to see if there are any pieces in the way
     * for a diagonal movement.
     *
     * @param oldX starting x for the move
     * @param oldY starting y for the move
     * @param newX ending x for the move
     * @param newY ending y for the move
     * @return true if there are no pieces in the way of the movement
     */
    private boolean diagonalCheckForPieces(int oldX, int oldY,
            int newX, int newY) {
        int minX = Math.min(oldX, newX);
        int minY = Math.min(oldY, newY);
        int maxX = Math.max(oldX, newX);
        int maxY = Math.max(oldY, newY);
        if (oldY - oldX == newY - newX) {
            for (int x = minX + 1, y = minY + 1; x < maxX; x++, y++) {
                if (board[x][y].hasPiece()) {
                    return false;
                }
            }
        } else {
            for (int x = minX + 1, y = maxY - 1; x < maxX; x++, y--) {
                if (board[x][y].hasPiece()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * changeTurn flips the value of turn and flips the board.
     */
    public void changeTurn() {
        turn = !turn;
    }

    /**
     * Getter of the current turn state.
     *
     * @return turn it is.
     */
    public boolean getTurn() {
        return turn;
    }

    /**
     * Getter of the current Team
     *
     * @return the team.
     */
    public boolean getTeam() {
        return team;
    }

    /**
     * Sets the value of team to the boolean sent
     *
     * @param team The new team state.
     */
    public final void setTeam(boolean team) {
        this.team = team;
    }

    /**
     * returns the current game's history.
     *
     * @return game's history
     */
    public ArrayList<String> getHistory() {
        return history;
    }

}
