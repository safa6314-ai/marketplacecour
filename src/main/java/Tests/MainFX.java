package Tests;

import Utils.DataSeeder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        DataSeeder.seed();

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/shared/AppShell.fxml")));
        Scene scene = new Scene(root, 1400, 860);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("ForumInArtevia");
        primaryStage.show();
    }
}
