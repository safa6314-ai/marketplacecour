package Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final class NavigationUtil {

    private NavigationUtil() {
    }

    static void changerInterface(Node source, String fileName) throws IOException {
        Parent root = FXMLLoader.load(getFxmlUrl(fileName));
        StackPane contentHost = findContentHost(source);
        if (contentHost != null && isQuizPage(fileName)) {
            contentHost.getChildren().setAll(root);
            return;
        }
        source.getScene().setRoot(root);
    }

    private static StackPane findContentHost(Node source) {
        Node current = source;
        while (current != null) {
            if (current instanceof StackPane stackPane && stackPane.getStyleClass().contains("content-host")) {
                return stackPane;
            }
            current = current.getParent();
        }
        return null;
    }

    private static boolean isQuizPage(String fileName) {
        String cleanFileName = fileName.replace("/", "");
        return cleanFileName.equals("Question.fxml")
                || cleanFileName.equals("Reponse.fxml")
                || cleanFileName.equals("QuizUtilisateur.fxml");
    }

    static URL getFxmlUrl(String fileName) throws IOException {
        String cleanFileName = fileName.replace("/", "");
        URL url = NavigationUtil.class.getResource("/" + cleanFileName);
        if (url != null) {
            return url;
        }

        Path path = Paths.get("src", "main", "resources", cleanFileName).toAbsolutePath();
        if (Files.exists(path)) {
            return path.toUri().toURL();
        }

        throw new IOException("Fichier FXML introuvable: " + cleanFileName);
    }
}
