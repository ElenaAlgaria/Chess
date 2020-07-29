package chess;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;


public class Main extends Application {

    private static Stage stage;

    @Override
    public void start(Stage stage) throws Exception{
        this.stage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("chess.fxml"));
        stage.setTitle("chess");
        stage.setScene(new Scene(root, 500,  550));
        stage.setResizable(false);
        stage.show();
    }

    public static Stage getStage(){
        return stage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
