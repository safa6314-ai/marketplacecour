package Tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getFxmlUrl("QuizUtilisateur.fxml"));
            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Artevia Quiz");
            primaryStage.show();
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de lancer l'application JavaFX.", e);
        }
    }

    private URL getFxmlUrl(String fileName) throws Exception {
        URL url = getClass().getResource("/" + fileName);

        if (url != null) {
            return url;
        }

        Path path = Paths.get(
                "src",
                "main",
                "resources",
                fileName
        ).toAbsolutePath();

        if (Files.exists(path)) {
            return path.toUri().toURL();
        }

        throw new IllegalStateException(
                "Fichier FXML introuvable : " + fileName
        );
    }
}
