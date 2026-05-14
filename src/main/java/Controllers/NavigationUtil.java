package Controllers;

import Controllers.shared.NavigationService;
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
        String cleanFileName = fileName.replace("/", "");
        if ("Question.fxml".equals(cleanFileName)) {
            Controllers.shared.AppState.setCurrentSection("questions");
        } else if ("Reponse.fxml".equals(cleanFileName)) {
            Controllers.shared.AppState.setCurrentSection("reponses");
        } else {
            Controllers.shared.AppState.setCurrentSection("dashboard");
        }
        NavigationService.navigate("/" + cleanFileName, "quiz");
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
