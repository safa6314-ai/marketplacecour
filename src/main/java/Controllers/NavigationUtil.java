package Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

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
        source.getScene().setRoot(root);
    }

    static URL getFxmlUrl(String fileName) throws IOException {
        String normalized = fileName.replace("\\", "/").trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        URL url = NavigationUtil.class.getResource("/" + normalized);
        if (url != null) {
            return url;
        }

        Path path = Paths.get("src", "main", "resources").resolve(normalized).toAbsolutePath();
        if (Files.exists(path)) {
            return path.toUri().toURL();
        }

        throw new IOException("Fichier FXML introuvable: " + normalized);
    }

    /** Drop the inner quiz module sidebar when the FXML is embedded in Admin or Client shell. */
    static void stripEmbeddedQuizSidebar(Parent root) {
        if (root instanceof BorderPane bp) {
            bp.setLeft(null);
        }
    }
}
