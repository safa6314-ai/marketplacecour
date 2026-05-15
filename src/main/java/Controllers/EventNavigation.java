package controllers;

import Utils.ThemeManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

final class EventNavigation {
    private EventNavigation() {
    }

    static void loadInCenter(Node source, String fxmlPath) throws IOException {
        if (source == null || source.getScene() == null) {
            throw new IOException("Zone principale introuvable.");
        }

        StackPane centerPane = findCenterPane(source.getScene().getRoot());
        if (centerPane == null) {
            throw new IOException("centerPane introuvable.");
        }

        URL url = EventNavigation.class.getResource(fxmlPath);
        if (url == null) {
            throw new IOException("FXML introuvable : " + fxmlPath);
        }

        Parent page = FXMLLoader.load(url);
        ThemeManager.applySavedTheme(page);
        centerPane.getChildren().setAll(page);
    }

    private static StackPane findCenterPane(Node node) {
        if (node instanceof StackPane stackPane && stackPane.getStyleClass().contains("content-host")) {
            return stackPane;
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                StackPane found = findCenterPane(child);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }
}
