package Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

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
