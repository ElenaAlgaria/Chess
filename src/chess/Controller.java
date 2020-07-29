package chess;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;


public class Controller {
    private String blackPath = "file:src\\images\\black\\";
    private String whitePath = "file:src\\images\\white\\";

    File pgn = new File("src\\pgn\\Adams.lgn");

    private int index = 0;
    private int turn = 0;

    private Timer timer = new Timer();

    private Map<String, Image> imageMap = new HashMap<>();

    private ArrayList<String> fromPos = new ArrayList<String>();
    private ArrayList<String> toPos = new ArrayList<String>();
    private ArrayList<String> check = new ArrayList<>();
    private ArrayList<Image> dead = new ArrayList<>();

    FileChooser fileChooser = new FileChooser();

    @FXML
    private Label turnLbl;

    @FXML
    private Label checkLbl;

    @FXML
    private GridPane gridPane;

    @FXML
    private TextField skipTxt;

    private Image rook = new Image(blackPath + "Rook.PNG");
    private Image knight = new Image(blackPath + "Knight.PNG");
    private Image bishop = new Image(blackPath + "Bishop.PNG");
    private Image queen = new Image(blackPath + "Queen.PNG");
    private Image king = new Image(blackPath + "King.PNG");
    private Image pawn = new Image(blackPath + "Pawn.PNG");

    private Image whiteRook = new Image(whitePath + "Rook.PNG");
    private Image whiteKnight = new Image(whitePath + "Knight.PNG");
    private Image whiteBishop = new Image(whitePath + "Bishop.PNG");
    private Image whiteQueen = new Image(whitePath + "Queen.PNG");
    private Image whiteKing = new Image(whitePath + "King.PNG");
    private Image whitePawn = new Image(whitePath + "Pawn.PNG");

    @FXML
    void chooseFile() throws IOException {
        Stage stage = Main.getStage();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null){
        this.pgn = file;
        }
        setStartingPosition();
    }

    @FXML
    void setStartingPosition() throws IOException {
        clearGame();

        imageMap.put("a8", rook);
        imageMap.put("b8", knight);
        imageMap.put("c8", bishop);
        imageMap.put("d8", queen);
        imageMap.put("e8", king);
        imageMap.put("f8", bishop);
        imageMap.put("g8", knight);
        imageMap.put("h8", rook);

        imageMap.put("a1", whiteRook);
        imageMap.put("b1", whiteKnight);
        imageMap.put("c1", whiteBishop);
        imageMap.put("d1", whiteQueen);
        imageMap.put("e1", whiteKing);
        imageMap.put("f1", whiteBishop);
        imageMap.put("g1", whiteKnight);
        imageMap.put("h1", whiteRook);

        for (int i = 0; i < 8; i++) {
            int currCharVal = 'a' + i;
            char currChar = (char) currCharVal;
            imageMap.put(currChar + "2", whitePawn);
            imageMap.put(currChar + "7", pawn);
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ImageView iv = new ImageView();
                iv.setFitWidth(43);
                iv.setFitHeight(43);
                gridPane.add(iv, i, j);
            }
        }

        updateGridPane();

        Scanner input = new Scanner(pgn);
        skipLines(input);
        while (input.hasNext()) {
            String nextToken = input.next();
            if (nextToken.indexOf('.') >= 0) {
                continue;
            }
            if (nextToken.equals("1-0") || nextToken.equals("0-1") || nextToken.equals("1/2-1/2"))
                break;
            String from = nextToken.substring(0, 2);
            fromPos.add(from);
            String to = nextToken.substring(2, 4);
            toPos.add(to);
            String checked = nextToken.substring(2);
            check.add(checked);
        }
    }

    public static void skipLines(Scanner s) {
        while (s.hasNextLine()) {
            String line = s.nextLine();
            if (!line.startsWith("[")) {
                break;
            }
        }
    }

    private void clearGridPane() {
        for (Node node : gridPane.getChildren()) {
            ((ImageView) node).setImage(null);
        }
    }

    private void clearGame() {
        clearGridPane();
        imageMap.clear();
        turnLbl.setText("");
        turn = 0;
        index = 0;
        dead.clear();
        check.clear();
        toPos.clear();
        fromPos.clear();
        skipTxt.clear();
    }

    private void updateGridPane() {
        for (Node node : gridPane.getChildren()) {
            int x = GridPane.getColumnIndex(node);
            x = x + 'a';
            char pox = (char) x;
            int y = GridPane.getRowIndex(node);
            y = 8 - y;
            if (node instanceof ImageView) {
                String concat = pox + "" + y;
                if (imageMap.containsKey(concat)) {
                    ((ImageView) node).setImage(imageMap.get(concat));
                } else {
                    ((ImageView) node).setImage(null);
                }
            } else {
                throw new ClassCastException("NodeToFind was not of type ImageView");
            }
        }
    }

    @FXML
    void nextStep() {
        if (index % 2 == 0 && index <= toPos.size() - 1) {
            turnLbl.setText(creatTurnText(turn++));
        }
        if (index <= toPos.size() - 1 && index >= 0) {
            died(toPos.get(index));
            imageMap.put(toPos.get(index), imageMap.get(fromPos.get(index)));
            imageMap.remove(fromPos.get(index));
            castling();
            check(index);
            index++;
        }
        updateGridPane();
    }

    @FXML
    void setPrevious() {
        if (index % 2 == 0 && index > 0) {
            turnLbl.setText(creatTurnText(--turn));
        }
        if (index > 0) {
            --index;
            imageMap.put(fromPos.get(index), imageMap.get(toPos.get(index)));
            imageMap.remove(toPos.get(index));
            check(index);
            castlingBackwards();
            imageMap.put(toPos.get(index), dead.get(index));
        }
        updateGridPane();
    }

    public void died(String to) {
        Node node = null;
        int row = 0;
        int col = 0;

        String letter = to.substring(0, 1);
        String num = to.substring(1, 2);

        char c = letter.charAt(0);
        row = c - 'a';
        col = 8 - Integer.valueOf(num);

        ObservableList<Node> children = gridPane.getChildren();
        for (Node n : children) {
            if (GridPane.getRowIndex(n) == col && GridPane.getColumnIndex(n) == row) {
                node = n;
                dead.add(((ImageView) node).getImage());
            }
        }
        updateGridPane();
    }


    private void check(int i) {
        if (check.get(i).contains("+")) {
            if (i == toPos.size() - 1) {
                checkLbl.setText("Checkmate");
                checkLbl.setStyle("-fx-text-fill: red");
            } else {
                checkLbl.setText("Check");
                checkLbl.setStyle("-fx-text-fill: red");
            }
        } else {
            checkLbl.setText("");
        }
    }

    private void castling() {
        if (fromPos.get(index).equals("e8") && toPos.get(index).equals("g8")) {
            imageMap.put("f8", imageMap.get("h8"));
            imageMap.remove("h8");
            updateGridPane();
        }

        if (fromPos.get(index).equals("e1") && toPos.get(index).equals("g1")) {
            imageMap.put("f1", imageMap.get("h1"));
            imageMap.remove("h1");
            updateGridPane();
        }

        if (fromPos.get(index).equals("e8") && toPos.get(index).equals("c8")) {
            imageMap.put("d8", imageMap.get("a8"));
            imageMap.remove("a8");
            updateGridPane();
        }

        if (fromPos.get(index).equals("e1") && toPos.get(index).equals("c1")) {
            imageMap.put("d1", imageMap.get("a1"));
            imageMap.remove("a1");
            updateGridPane();
        }
    }

    private void castlingBackwards() {
        if (fromPos.get(index).equals("e8") && toPos.get(index).equals("g8")) {
            imageMap.put("h8", imageMap.get("f8"));
            imageMap.remove("f8");
            updateGridPane();
        }

        if (fromPos.get(index).equals("e1") && toPos.get(index).equals("g1")) {
            imageMap.put("h1", imageMap.get("f1"));
            imageMap.remove("f1");
            updateGridPane();
        }

        if (fromPos.get(index).equals("e8") && toPos.get(index).equals("c8")) {
            imageMap.put("a8", imageMap.get("d8"));
            imageMap.remove("d8");
            updateGridPane();
        }

        if (fromPos.get(index).equals("e1") && toPos.get(index).equals("c1")) {
            imageMap.put("a1", imageMap.get("d1"));
            imageMap.remove("d1");
            updateGridPane();
        }
    }


    public String creatTurnText(int turn) {
        return (turn + 1) + ".";
    }

    @FXML
    void goThroughGame() throws InterruptedException, IOException {
        timer.schedule(new TimerTask() {
            public void run() {
                Platform.runLater(() -> nextStep());
            }
        }, 500, 600);
    }

    @FXML
    void skipTurn() {
        if (Pattern.matches("\\d+", skipTxt.getText())) {
            int number = Integer.valueOf(skipTxt.getText());
            number = Math.min(toPos.size(), number);
            if (!(number > toPos.size())) {
                if (number < index) {
                    for (int i = index; i > number; i--) {
                        setPrevious();
                    }
                } else if (number > index) {
                    for (int i = index; i < number; i++) {
                        nextStep();
                    }
                }
            }
        }
    }
}





